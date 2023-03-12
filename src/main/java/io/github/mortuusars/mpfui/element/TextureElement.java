package io.github.mortuusars.mpfui.element;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.mpfui.MPFScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class TextureElement<T extends MPFScreen> extends UIElement {
    private T screen;
    private int uOffset;
    private int vOffset;
    public ResourceLocation texture;

    public TextureElement(T parentScreen, String id, int posX, int posY, int width, int height, int uOffset, int vOffset, ResourceLocation texture) {
        super(id, posX, posY, width, height);
        this.screen = parentScreen;
        this.uOffset = uOffset;
        this.vOffset = vOffset;
        this.texture = texture;
    }

    public TextureElement(T parentScreen, String id, int posX, int posY, int width, int height, int uOffset, int vOffset) {
        this(parentScreen, id, posX, posY, width, height, uOffset, vOffset, parentScreen.getBackgroundTexture());
    }

    @Override
    public void renderBg(Screen screen, @NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1, 1,1, 1);

//        boolean hasCustomTexture = !texture.equals(screen.getBackgroundTexture());
//        if (hasCustomTexture)
//            RenderSystem.setShaderTexture(0, texture);
//        else
//            RenderSystem.setShaderTexture(0, screen.getBackgroundTexture());
//
//        screen.blit(poseStack, screen.getGuiLeft() + posX, screen.getGuiTop() + posY, uOffset, vOffset, width, height);
//
//        if (hasCustomTexture) {
//            RenderSystem.setShader(GameRenderer::getPositionTexShader);
//            RenderSystem.setShaderColor(1, 1,1, 1);
//            RenderSystem.setShaderTexture(0, screen.getBackgroundTexture());
//        }
    }
}
