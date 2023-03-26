package io.github.mortuusars.mpfui.renderable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class TextureRenderable extends MPFRenderable<TextureRenderable> {
    protected int uOffset;
    protected int vOffset;
    protected int hoverVOffset;
    protected ResourceLocation texture;

    public TextureRenderable(int x, int y, int width, int height, int uOffset, int vOffset, ResourceLocation texture) {
        super(x, y, width, height);
        this.uOffset = uOffset;
        this.vOffset = vOffset;
        this.hoverVOffset = 0;
        this.texture = texture;
    }

    public TextureRenderable(int x, int y, int width, int height, int uOffset, int vOffset, int hoverVOffset, ResourceLocation texture) {
        super(x, y, width, height);
        this.uOffset = uOffset;
        this.vOffset = vOffset;
        this.hoverVOffset = hoverVOffset;
        this.texture = texture;
    }

    @Override
    public TextureRenderable getThis() {
        return this;
    }

    public TextureRenderable setOpacity(float alpha) {
        this.alpha = alpha;
        return this;
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (isVisible(poseStack, mouseX, mouseY)) {
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            renderBg(poseStack, Minecraft.getInstance(), mouseX, mouseY);

            if (isHoveredOrFocused())
                renderToolTip(poseStack, mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(@NotNull PoseStack poseStack, @NotNull Minecraft minecraft, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1, 1,1, this.alpha);
        RenderSystem.setShaderTexture(0, texture);
        int v = isHoveredOrFocused() ? vOffset + hoverVOffset : vOffset;
        this.blit(poseStack, x, y, uOffset, v, width, height);
    }
}
