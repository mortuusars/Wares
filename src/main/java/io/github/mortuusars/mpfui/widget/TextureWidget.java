package io.github.mortuusars.mpfui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class TextureWidget extends AbstractWidget {
    private AbstractContainerScreen<?> screen;
    private int uOffset;
    private int vOffset;
    private @Nullable Component tooltip;
    private ResourceLocation texture;

    public TextureWidget(AbstractContainerScreen<?> screen, int x, int y, int width, int height, int uOffset, int vOffset, @Nullable Component tooltip, ResourceLocation texture) {
        super(x, y, width, height, new TextComponent(""));
        this.screen = screen;
        this.uOffset = uOffset;
        this.vOffset = vOffset;
        this.tooltip = tooltip;
        this.texture = texture;
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) { } // TODO: implement

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (this.visible) {
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            renderBg(poseStack, Minecraft.getInstance(), mouseX, mouseX);
        }
    }

    @Override
    protected void renderBg(PoseStack poseStack, Minecraft minecraft, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1, 1,1, this.alpha);
        RenderSystem.setShaderTexture(0, texture);
        this.blit(poseStack, x, y, uOffset, vOffset, width, height);
    }

    @Override
    public void renderToolTip(PoseStack poseStack, int mouseX, int mouseY) {
        if (tooltip != null)
            screen.renderTooltip(poseStack, getMessage(), mouseX, mouseY);
    }
}
