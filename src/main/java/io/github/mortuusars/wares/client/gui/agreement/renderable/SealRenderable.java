package io.github.mortuusars.wares.client.gui.agreement.renderable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.mpfui.renderable.TextureRenderable;
import io.github.mortuusars.wares.client.gui.agreement.element.Seal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class SealRenderable extends TextureRenderable {
    private final ResourceLocation texture;
    private final int shadowHeight;

    public SealRenderable(int x, int y, Seal seal, int shadowHeight) {
        super(x, y, Seal.WIDTH, Seal.HEIGHT, 0, 0, seal.getTexturePath());
        this.texture = seal.getTexturePath();
        this.shadowHeight = shadowHeight;
    }

    @Override
    public TextureRenderable getThis() {
        return this;
    }

    @Override
    protected void renderBg(@NotNull PoseStack poseStack, @NotNull Minecraft minecraft, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);

        RenderSystem.enableBlend();

        RenderSystem.setShaderColor(1, 1,1, 0.5f);
        this.blit(poseStack, getX(), getY(), uOffset, Seal.Element.SHADOW.getVOffset(), width, shadowHeight);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1, 1,1, this.alpha);
        this.blit(poseStack, getX(), getY(), uOffset, Seal.Element.STRING.getVOffset(), width, height);
        this.blit(poseStack, getX(), getY(), uOffset, Seal.Element.BASE.getVOffset(), width, height);
        this.blit(poseStack, getX(), getY(), uOffset, Seal.Element.LOGO.getVOffset(), width, height);
    }
}
