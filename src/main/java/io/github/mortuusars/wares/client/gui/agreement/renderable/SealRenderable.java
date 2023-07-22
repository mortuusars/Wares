package io.github.mortuusars.wares.client.gui.agreement.renderable;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.mpfui.renderable.TextureRenderable;
import io.github.mortuusars.wares.client.gui.agreement.element.Seal;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class SealRenderable extends TextureRenderable {
    private final ResourceLocation texture;
    private final int shadowHeight;

    public SealRenderable(int x, int y, Seal seal, int shadowHeight) {
        super(x, y, Seal.WIDTH, Seal.HEIGHT, 0, 0, seal.getTexturePath());
        this.texture = seal.getTexturePath();
        this.shadowHeight = shadowHeight;
    }

    @Override
    protected void renderBg(@NotNull PoseStack poseStack, @NotNull Minecraft minecraft, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1, 1,1, 0.5f);
        graphics.blit(texture, getX(), getY(), uOffset, Seal.Element.SHADOW.getVOffset(), width, shadowHeight);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1, 1,1, this.alpha);
        graphics.blit(texture, getX(), getY(), uOffset, Seal.Element.STRING.getVOffset(), width, height);
        graphics.blit(texture, getX(), getY(), uOffset, Seal.Element.BASE.getVOffset(), width, height);
        graphics.blit(texture, getX(), getY(), uOffset, Seal.Element.LOGO.getVOffset(), width, height);
    }
}
