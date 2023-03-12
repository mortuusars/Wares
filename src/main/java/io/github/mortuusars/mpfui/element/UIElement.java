package io.github.mortuusars.mpfui.element;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UIElement {
    public String id;
    public int posX;
    public int posY;
    public int width;
    public int height;
    public boolean visible = true;
    public @Nullable Component tooltip;

    public UIElement(String id, int posX, int posY, int width, int height) {
        this.id = id;
        this.posX = posX;
        this.posY = posY;
        this.width = width;
        this.height = height;
    }

    public boolean isMouseOver(double mouseX, double mouseY){
        return (mouseX >= posX && mouseX < posX + width) && (mouseY >= posY && mouseY < posY + height);
    }

    public boolean isVisible() {
        return visible;
    }

    public void renderBg(Screen screen, @NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) { }
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) { }
    public void renderLabels(PoseStack poseStack, int mouseX, int mouseY) { }
    public void renderTooltip(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
//        if (tooltip != null)

    }
    public void onMouseOver(int mouseX, int mouseY) { }
    public boolean onClick(double mouseX, double mouseY) {
        return false;
    }
}
