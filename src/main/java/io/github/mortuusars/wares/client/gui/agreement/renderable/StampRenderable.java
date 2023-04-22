package io.github.mortuusars.wares.client.gui.agreement.renderable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.mortuusars.mpfui.renderable.TextureRenderable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class StampRenderable extends TextureRenderable {
    public StampRenderable(int x, int y, int width, int height, int uOffset, int vOffset, ResourceLocation texture) {
        super(x, y, width, height, uOffset, vOffset, texture);
    }

    @Override
    public StampRenderable getThis() {
        return this;
    }

    @Override
    protected void renderBg(@NotNull PoseStack poseStack, @NotNull Minecraft minecraft, int mouseX, int mouseY) {
        poseStack.pushPose();

        poseStack.translate(getX() + width / 2f, getY() + height / 2f, 0);
        poseStack.mulPose(Axis.ZP.rotationDegrees(-5));
        poseStack.scale(1.2f, 1.2f, 1.2f);
        poseStack.translate(-(width/2f), -(height/2f), 0);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1, 1,1, this.alpha);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.enableBlend();
        int v = isHoveredOrFocused() ? vOffset + hoverVOffset : vOffset;
        this.blit(poseStack, 0, 0, uOffset, v, width, height);

        poseStack.popPose();

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1f);
    }
}
