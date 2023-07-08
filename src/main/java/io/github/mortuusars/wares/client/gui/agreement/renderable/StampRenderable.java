package io.github.mortuusars.wares.client.gui.agreement.renderable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.mortuusars.mpfui.renderable.TextureRenderable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class StampRenderable extends TextureRenderable {
    public StampRenderable(int x, int y, int width, int height, int uOffset, int vOffset, ResourceLocation texture) {
        super(x, y, width, height, uOffset, vOffset, texture);
    }

    @Override
    public StampRenderable getThis() {
        return this;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();

        poseStack.translate(getX() + width / 2f, getY() + height / 2f, 0);
        poseStack.mulPose(Axis.ZP.rotationDegrees(-5));
        poseStack.scale(1.2f, 1.2f, 1.2f);
        poseStack.translate(-(width/2f), -(height/2f), 0);

        RenderSystem.enableBlend();
        int v = isHoveredOrFocused() ? vOffset + hoverVOffset : vOffset;
        graphics.blit(texture, 0, 0, uOffset, v, width, height);

        poseStack.popPose();

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1f);
    }
}
