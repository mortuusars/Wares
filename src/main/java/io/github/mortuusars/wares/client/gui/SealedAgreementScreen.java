package io.github.mortuusars.wares.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class SealedAgreementScreen extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Wares.ID, "textures/gui/sealed_agreement.png");
    private static final int IMAGE_WIDTH = 196;
    private static final int IMAGE_HEIGHT = 120;

    private static final int MAX_ROTATION_HORIZONTAL = 200;
    private static final int MAX_ROTATION_VERTICAL = 45;

//    private final Component buyerName;
//    private final Component buyerAddress;
    private Screen parentScreen;
    private boolean isFlipped = false;
    public SealedAgreementScreen(/*Component buyerName, Component buyerAddress*/) {
        super(TextComponent.EMPTY);
//        this.buyerName = buyerName;
//        this.buyerAddress = buyerAddress;
        this.minecraft = Minecraft.getInstance();
    }

    public boolean isOpen() {
        return minecraft != null && minecraft.screen == this;
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

        float yBright = ((-rotY + 60) / 90f) * -0.2f;
        float brightness = 1 - (0.2f + yBright);

        float r = brightness;
        float g = brightness;
        float b = brightness /*+ (1 - yBright * 2) * 0.1f*/;

        float xRotatedBy = (rotX + 90) % 360;
        boolean flipped = xRotatedBy > 180 || xRotatedBy < 0;
        flip(flipped);

        if (flipped) {
            rotX += 180;
            rotX *= -1;
        }

        poseStack.mulPose(Vector3f.YP.rotationDegrees(rotX * -1));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(rotY));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(rotY * 0.05f));
        poseStack.scale(1.25f, 1.25f, 1.25f);

        // Shift letter to the center
        poseStack.translate(-(IMAGE_WIDTH / 2f), -(IMAGE_HEIGHT / 2f), 0);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(r, g, b, 1F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        float x = rotX * 0.2f;
        float y = rotY * 0.2f;

        this.blit(poseStack, 0, 0, 0,  isFlipped ? 122 : 0, 196, 120);
        poseStack.translate(x * 0.05f, y * 0.05f, 0);
        this.blit(poseStack, 2, 2, 2, isFlipped ? 124 : 2, 192, 116);

        if (!isFlipped) {
            // SHADOW
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(r, g, b, Mth.map(Math.abs(yBright), 0f, 0.2f, 0f, 0.75f));
            renderSealWithParallax(poseStack, 0, x * 0.02f, (Math.abs(rotY - 45)) * 0.01f, 0);
            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(r, g, b, 1F);

            // STRING
            renderSealWithParallax(poseStack, 48, x * 0.5f, y * 0.5f, 0);

            // BASE
            renderSealWithParallax(poseStack, 96, x * 0.25f, y * 0.25f, 0);

            // LOGO
            renderSealWithParallax(poseStack, 144, x * 0.15f, y * 0.15f, 0);
        }

        poseStack.popPose();

        //TODO: Seal mouseover tooltip.
//        if (Math.abs(fromCenterX) < 25 && Math.abs(fromCenterY) < 25) {
//            renderTooltip(poseStack, new TextComponent("Buyer"), mouseX, mouseY);
//        }
    }

    private void flip(final boolean value) {
        if (this.isFlipped != value) {
            isFlipped = value;

            assert Minecraft.getInstance().level != null;
            Random random = Minecraft.getInstance().level.random;
            float pitch = random.nextFloat() * 0.2f + (isFlipped ? 1.1f : 1.5f);
            assert Minecraft.getInstance().player != null;
            Minecraft.getInstance().player.playSound(Wares.SoundEvents.PAPER_CRACKLE.get(), 0.65f, pitch);
        }
    }

    private void renderSealWithParallax(PoseStack poseStack, final int vOffset, final float x, final float y, final float z) {
        poseStack.translate(x, y, z);
        this.blit(poseStack, IMAGE_WIDTH / 2 - 24, IMAGE_HEIGHT / 2 - 24, 196, vOffset, 48, 48);
        poseStack.translate(-x, -y, -z);
    }
}
