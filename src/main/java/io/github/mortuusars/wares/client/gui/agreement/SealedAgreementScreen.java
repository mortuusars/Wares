package io.github.mortuusars.wares.client.gui.agreement;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.client.gui.agreement.element.Seal;
import io.github.mortuusars.wares.config.Config;
import io.github.mortuusars.wares.data.Lang;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SealedAgreementScreen extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Wares.ID, "textures/gui/sealed_agreement.png");
    private static final int IMAGE_WIDTH = 196;
    private static final int IMAGE_HEIGHT = 120;
    private static final float SCALE = 1.25f;
    private static final int MAX_ROTATION_HORIZONTAL = 200;
    private static final int MAX_ROTATION_VERTICAL = 45;
    private static final int BACKSIDE_HORIZONTAL_MARGIN = 16;
    private static final int BACKSIDE_VERTICAL_MARGIN = 16;
    private static final int FONT_COLOR = 0xff886447;

    private final Seal seal;
    private final Component sealTooltip;
    private final Component backsideMessage;
    private final Component showRemainingTextMessage;

    private Screen parentScreen;
    private List<FormattedCharSequence> backsideMessageVisibleLines = Collections.emptyList();
    private List<FormattedCharSequence> backsideMessageLeftoverLines = Collections.emptyList();
    private boolean isFlipped = false;
    private int messagePosY;

    private long flippedAt = 0;
    private boolean shouldDrawShowRemainingTextMessage = true;
    private int showRemainingTextMessageOpacity = 19;

    private long prevGameTime = 0;

    public SealedAgreementScreen(String seal, Component sealTooltip, Component backsideMessage) {
        super(Component.empty());

        this.seal = new Seal(seal).printErrorAndFallbackToDefaultIfNotFound();
        this.sealTooltip = sealTooltip;
        this.backsideMessage = backsideMessage;
        this.showRemainingTextMessage = Lang.GUI_SEALED_AGREEMENT_SHOW_REMAINING_TEXT_MESSAGE.translate();

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

        if (!backsideMessage.equals(Component.empty())) {
            List<FormattedCharSequence> messageLines = new ArrayList<>(font.split(backsideMessage, IMAGE_WIDTH - BACKSIDE_HORIZONTAL_MARGIN * 2));
            backsideMessageVisibleLines = messageLines.subList(0, Math.min(messageLines.size(), (IMAGE_HEIGHT - BACKSIDE_VERTICAL_MARGIN * 2) / font.lineHeight));
            messagePosY = (IMAGE_HEIGHT - backsideMessageVisibleLines.size() * font.lineHeight) / 2;

            if (messageLines.size() > backsideMessageVisibleLines.size()) {
                final int lastLineIndex = backsideMessageVisibleLines.size() - 1;
                FormattedCharSequence lastLine = backsideMessageVisibleLines.get(lastLineIndex);
                lastLine = FormattedCharSequence.composite(lastLine, FormattedCharSequence.forward("...", backsideMessage.getStyle()));
                backsideMessageVisibleLines.set(lastLineIndex, lastLine);

                backsideMessageLeftoverLines = messageLines.subList(backsideMessageVisibleLines.size(), messageLines.size());
                backsideMessageLeftoverLines.set(0, FormattedCharSequence.composite(FormattedCharSequence.forward("...", backsideMessage.getStyle()), backsideMessageLeftoverLines.get(0)));
            }
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

        this.blit(poseStack, 0, 0, 0,  isFlipped ? 121 : 0, 196, 120);

        if (!isFlipped) {
            renderSeal(poseStack, rotX, rotY, brightness);
        }
        else if (!backsideMessageVisibleLines.isEmpty()) {
            for (int i = 0; i < backsideMessageVisibleLines.size(); i++) {
                FormattedCharSequence line = backsideMessageVisibleLines.get(i);
                int mX = IMAGE_WIDTH / 2 - font.width(line) / 2;
                font.draw(poseStack, line, mX, messagePosY + font.lineHeight * i, FONT_COLOR);
            }
        }

        poseStack.popPose();

        // SEAL TOOLTIP
        if (Math.abs(fromCenterX) < 20 && Math.abs(fromCenterY) < 20) {
            if (!isFlipped) {
                if (!sealTooltip.equals(Component.empty()))
                    renderTooltip(poseStack, sealTooltip, mouseX, mouseY);
            }
        }

        // BACK MESSAGE TOOLTIP
        if (isFlipped && Screen.hasShiftDown() &&
                !backsideMessage.equals(Component.empty()) && !backsideMessageLeftoverLines.isEmpty()) {
            renderTooltip(poseStack, backsideMessageLeftoverLines, mouseX, mouseY);
        }

        if (Screen.hasShiftDown())
            shouldDrawShowRemainingTextMessage = false;

        if (!backsideMessageLeftoverLines.isEmpty()) {
            drawShowRemainingTextMessage(poseStack);
        }

        assert Minecraft.getInstance().level != null;
        prevGameTime = Minecraft.getInstance().level.getGameTime();
    }

    private void drawShowRemainingTextMessage(@NotNull PoseStack poseStack) {
        assert Minecraft.getInstance().level != null;
        long gameTime = Minecraft.getInstance().level.getGameTime();

        float fadeIn = 60;

        if (shouldDrawShowRemainingTextMessage && isFlipped && gameTime - flippedAt > 20) {
            assert minecraft != null;
            double time = gameTime  + minecraft.getFrameTime() - flippedAt - 20;
            double range = Mth.clamp(time / fadeIn, 0.001f, 1f);
            double rangeEaseInOut = range < 0.5 ? 4 * range * range * range : 1 - Math.pow(-2 * range + 2, 3) / 2;
            showRemainingTextMessageOpacity = Math.max((int)Math.floor((rangeEaseInOut * 255f)), 20);
        }
        else if (showRemainingTextMessageOpacity > 20 && gameTime != prevGameTime)
            showRemainingTextMessageOpacity -= 16;

        if (showRemainingTextMessageOpacity > 19)
            font.draw(poseStack, showRemainingTextMessage, width / 2f - font.width(showRemainingTextMessage) / 2f, height - 50, 0x555555 | showRemainingTextMessageOpacity << 24);
    }

    private void flip(final boolean value) {
        if (this.isFlipped != value) {
            isFlipped = value;

            assert Minecraft.getInstance().level != null;
            flippedAt = Minecraft.getInstance().level.getGameTime();

            shouldDrawShowRemainingTextMessage = true;

            assert Minecraft.getInstance().level != null;
            assert Minecraft.getInstance().player != null;
            RandomSource random = Minecraft.getInstance().level.random;
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
