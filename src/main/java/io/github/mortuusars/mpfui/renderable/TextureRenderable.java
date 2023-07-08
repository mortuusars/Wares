package io.github.mortuusars.mpfui.renderable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isVisible(mouseX, mouseY)) {
            this.isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + this.width && mouseY < getY() + this.height;
            renderWidget(graphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float pPartialTick) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1, 1,1, this.alpha);
        RenderSystem.setShaderTexture(0, texture);
        int v = isHoveredOrFocused() ? vOffset + hoverVOffset : vOffset;
        graphics.blit(texture, getX(), getY(), uOffset, v, width, height);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {

    }
}
