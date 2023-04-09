package io.github.mortuusars.wares.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.config.Config;
import io.github.mortuusars.wares.data.agreement.Seal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SealedAgreementScreen extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Wares.ID, "textures/gui/sealed_agreement.png");
    private static final int IMAGE_WIDTH = 196;
    private static final int IMAGE_HEIGHT = 120;
    private static final float SCALE = 1.25f;
    private static final int MAX_ROTATION_HORIZONTAL = 200;
    private static final int MAX_ROTATION_VERTICAL = 45;
    private static final int BACKSIDE_HORIZONTAL_MARGIN = 16;
    private static final int BACKSIDE_VERTICAL_MARGIN = 16;

    private final Seal seal;
    private final Component sealTooltip;
    private final Component backsideMessage;

    private Screen parentScreen;
    private List<FormattedCharSequence> backsideMessageLines = Collections.emptyList();
    private int backsideVisibleLines = 0;
    private boolean isFlipped = false;

    public SealedAgreementScreen(String seal, Component sealTooltip, Component backsideMessage) {
        super(TextComponent.EMPTY);

        this.seal = new Seal(seal);
        this.sealTooltip = sealTooltip;
        this.backsideMessage = backsideMessage;

        this.minecraft = Minecraft.getInstance();
    }

    public boolean isOpen() {
        return minecraft != null && minecraft.screen == this;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public void showAsOverlay() {
        if (minecraft != null) {
            if (!isOpen()) {
                parentScreen = minecraft.screen;
            }
            minecraft.setScreen(this);

            assert minecraft.player != null;
            minecraft.player.playSound(Wares.SoundEvents.PAPER_CRACKLE.get(), 1f, minecraft.player.level.getRandom().nextFloat() * 0.1f + 0.9f);
        }
    }

    @Override
    public void onClose() {
        if (isOpen() && minecraft != null) {
            minecraft.setScreen(parentScreen);
            parentScreen = null;

            assert minecraft.player != null;
            minecraft.player.playSound(Wares.SoundEvents.PAPER_CRACKLE.get(), 1f, minecraft.player.level.getRandom().nextFloat() * 0.1f + 1.2f);

            return;
        }
        super.onClose();
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (Config.AGREEMENT_CLOSE_WITH_RMB.get() && pButton == 1) {
            onClose();
            return true;
        }
        else
            return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode == InputConstants.KEY_E) {
            this.onClose();
            return true;
        }

        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    protected void init() {
        super.init();

        if (!backsideMessage.equals(TextComponent.EMPTY)) {
            backsideMessageLines = font.split(backsideMessage, IMAGE_WIDTH - BACKSIDE_HORIZONTAL_MARGIN * 2);
            backsideVisibleLines = Math.min(backsideMessageLines.size(), (IMAGE_HEIGHT - BACKSIDE_VERTICAL_MARGIN * 2) / font.lineHeight);
        }
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);

        poseStack.pushPose();

        // Move origin to screen center
        poseStack.translate(width / 2f, height / 2f, 0);

        float fromCenterX = (mouseX - width / 2f);
        float fromCenterY = (mouseY - height / 2f);

        float rotX = (fromCenterX + width / 2f) / (width / 2f) * MAX_ROTATION_HORIZONTAL;
        rotX -= MAX_ROTATION_HORIZONTAL;
        float rotY = (fromCenterY + height / 2f) / (height / 2f) * MAX_ROTATION_VERTICAL;
        rotY -= MAX_ROTATION_VERTICAL;

        float yBright = ((-rotY + 45) / 90f) * -0.3f;
        float brightness = 1 - (0.15f + yBright);

        float xRotatedBy = (rotX + 90) % 360;
        boolean flipped = xRotatedBy > 180 || xRotatedBy < 0;
        flip(flipped);

        if (flipped) {
            rotX -= 180;
            rotX *= -1;
        }

        poseStack.mulPose(Vector3f.YP.rotationDegrees(rotX * -1));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(rotY));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(rotX * rotY * 0.001f));
        poseStack.scale(SCALE, SCALE, SCALE);

        // Shift letter to the center
        poseStack.translate(-(IMAGE_WIDTH / 2f), -(IMAGE_HEIGHT / 2f), 0);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(brightness, brightness, brightness, 1F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        this.blit(poseStack, 0, 0, 0,  isFlipped ? 122 : 0, 196, 120);

        if (!isFlipped) {
            renderSeal(poseStack, rotX, rotY, brightness);
        }
        else if (backsideVisibleLines > 0) {
            // MESSAGE ON THE BACK SIDE:
            int messageHeight = backsideVisibleLines * font.lineHeight;
            int lineStart = (IMAGE_HEIGHT - messageHeight) / 2;

            for (int i = 0; i < backsideVisibleLines; i++) {
                font.draw(poseStack, backsideMessageLines.get(i), 16, lineStart + font.lineHeight * i, 0xff886447);
            }
        }

        poseStack.popPose();

        // SEAL TOOLTIP
        if (Math.abs(fromCenterX) < 20 && Math.abs(fromCenterY) < 20) {
            if (!isFlipped) {
                if (!sealTooltip.equals(TextComponent.EMPTY))
                    renderTooltip(poseStack, sealTooltip, mouseX, mouseY);
            }
        }

        // BACK MESSAGE TOOLTIP
        if (isFlipped && Screen.hasShiftDown() &&
                !backsideMessage.equals(TextComponent.EMPTY) && backsideVisibleLines < backsideMessageLines.size()) {
            renderTooltip(poseStack, font.split(backsideMessage, 320), mouseX, mouseY);
        }
    }

    private void flip(final boolean value) {
        if (this.isFlipped != value) {
            isFlipped = value;

            assert Minecraft.getInstance().level != null;
            assert Minecraft.getInstance().player != null;
            Random random = Minecraft.getInstance().level.random;
            float pitch = random.nextFloat() * 0.2f + (isFlipped ? 1.1f : 1.5f);
            Minecraft.getInstance().player.playSound(Wares.SoundEvents.PAPER_CRACKLE.get(), 0.65f, pitch);
        }
    }

    private void renderSeal(@NotNull PoseStack poseStack, float rotX, float rotY, float brightness) {
        RenderSystem.setShaderTexture(0, seal.getTexturePath());

        final int sealX = IMAGE_WIDTH / 2 - Seal.WIDTH / 2;
        final int sealY = IMAGE_HEIGHT / 2 - Seal.HEIGHT / 2;

        float x = rotX * 0.2f;
        float y = rotY * 0.2f;

        // SHADOW
        float shadowAlpha = Mth.map(Math.max((rotY * -1), 0), -10, MAX_ROTATION_VERTICAL, 0.1f, 0.65f) + 0.1f;
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(brightness, brightness, brightness, shadowAlpha);
        renderSealElementWithParallax(poseStack, sealX, sealY, 0, shadowAlpha * 3f, 0, Seal.Element.SHADOW);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(brightness, brightness, brightness, 1F);

        // STRING
        renderSealElementWithParallax(poseStack, sealX, sealY, x * 0.45f, y * 0.45f, -0.05f, Seal.Element.STRING);
        renderSealElementWithParallax(poseStack, sealX, sealY, x * 0.50f, y * 0.50f, -0.1f, Seal.Element.STRING);
        renderSealElementWithParallax(poseStack, sealX, sealY, x * 0.55f, y * 0.55f, -0.15f, Seal.Element.STRING);
        renderSealElementWithParallax(poseStack, sealX, sealY, x * 0.60f, y * 0.60f, -0.2f, Seal.Element.STRING);

        // BASE
        renderSealElementWithParallax(poseStack, sealX, sealY, x * 0.15f, y * 0.15f, -0.1f, Seal.Element.BASE);
        renderSealElementWithParallax(poseStack, sealX, sealY, x * 0.20f, y * 0.20f, -0.2f, Seal.Element.BASE);
        renderSealElementWithParallax(poseStack, sealX, sealY, x * 0.25f, y * 0.25f, -0.3f, Seal.Element.BASE);
        renderSealElementWithParallax(poseStack, sealX, sealY, x * 0.30f, y * 0.30f, -0.4f, Seal.Element.BASE);
        renderSealElementWithParallax(poseStack, sealX, sealY, x * 0.35f, y * 0.35f, -0.5f, Seal.Element.BASE);

        // LOGO
        renderSealElementWithParallax(poseStack, sealX, sealY, x * 0.1f, y * 0.1f, -0.05f, Seal.Element.LOGO);
        renderSealElementWithParallax(poseStack, sealX, sealY, x * 0.15f, y * 0.15f, -0.1f, Seal.Element.LOGO);

        RenderSystem.setShaderTexture(0, TEXTURE);
    }

    @SuppressWarnings("SameParameterValue")
    private void renderSealElementWithParallax(PoseStack poseStack, int x, int y, float pX, float pY, float pZ, Seal.Element element) {
        poseStack.translate(pX, pY, pZ);
        this.blit(poseStack, x, y, 0, element.getVOffset(), Seal.WIDTH, Seal.HEIGHT);
        poseStack.translate(-pX, -pY, -pZ);
    }
}
