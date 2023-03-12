package io.github.mortuusars.mpfui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import io.github.mortuusars.mpfui.element.UIElement;
import io.github.mortuusars.mpfui.helper.ReverseIterator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


public abstract class MPFScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    public long gameTick;
    protected boolean drawDebugInfo = true;
    private final List<UIElement> elements = new ArrayList<>();

    public MPFScreen(T menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    // <Elements>

    public Pair<Integer, Integer> addElement(UIElement element){
        elements.add(element);
        return Pair.of(element.posX + element.width, element.posY + element.height);
    }

    public @Nullable UIElement getElement(int index){
        return (index >= 0 && elements.size() > index) ? elements.get(index) : null;
    }

    @Nullable
    public UIElement getElement(String id){
        for (var element : elements){
            if (element.id.equals(id))
                return element;
        }
        return null;
    }

    public NonNullList<UIElement> getElements(){
        NonNullList<UIElement> screenElements = NonNullList.create();
        screenElements.addAll(elements);
        return screenElements;
    }

    public void removeElement(UIElement element){
        elements.remove(element);
    }

    public void clearElements(){
        elements.clear();
    }


    // <Utils>

    public int getGuiCenterX(){
        return getGuiLeft() + imageWidth / 2;
    }

    public int getGuiCenterY(){
        return getGuiTop() + imageHeight / 2;
    }

    public int fromLeft(int x){
        return getGuiLeft() + x;
    }

    public int fromTop(int y){
        return getGuiTop() + y;
    }

    public int fromRight(int x, int width){
        return getGuiLeft() + imageWidth - (x + width);
    }

    public int fromBottom(int y, int height){
        return getGuiTop() + imageHeight - (y + height);
    }

    // <Base>

    public abstract ResourceLocation getBackgroundTexture();

    @Override
    protected void init() {
        this.inventoryLabelY = this.imageHeight - 94;
        clearElements(); // Fix doubles when resizing window;
        super.init();
    }

    @Override
    public void renderBg(@NotNull PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1, 1,1, 1);
        RenderSystem.setShaderTexture(0, getBackgroundTexture());
        blit(poseStack, getGuiLeft(), getGuiTop(), 0,0, imageWidth, imageHeight); // Gui Texture

        ClientLevel level = Minecraft.getInstance().level;
        if (level != null)
            gameTick = level.getGameTime();

        renderBgForElements(poseStack, partialTick, mouseX, mouseY);
    }

    public void renderBgForElements(@NotNull PoseStack poseStack, float partialTick, int mouseX, int mouseY){
        for (UIElement element : elements)
            element.renderBg(this, poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);

        for (UIElement element : elements){
            element.render(poseStack, mouseX, mouseY, partialTick);
            if (element.isMouseOver(mouseX, mouseY))
                element.onMouseOver(mouseX, mouseY);
        }

        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        super.renderLabels(poseStack, mouseX, mouseY);
        for (UIElement element : elements)
            element.renderLabels(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(@NotNull PoseStack poseStack, int x, int y) {
        super.renderTooltip(poseStack, x, y);
        for (UIElement element : new ReverseIterator<>(elements)){
            if (element.isMouseOver(x, y)){
                element.renderTooltip(poseStack, x ,y);
                break;
            }
        }

        if (this.drawDebugInfo && minecraft != null && minecraft.options.renderDebug)
            drawDebugInfo(poseStack, x, y, 0);
    }

//    public void renderItemStackTooltip(PoseStack poseStack, ItemStack stack, int mouseX, int mouseY){
//        this.renderTooltip(poseStack, stack, mouseX, mouseY);
//    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int pButton) {
        for (UIElement element : new ReverseIterator<>(elements)){ // catch mouse click on last added element.
            if (element.isMouseOver(mouseX, mouseY)){
                if (element.onClick(mouseX, mouseY))
                    return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, pButton);
    }

    public void drawDebugInfo(PoseStack poseStack, int mouseX, int mouseY, float partialTick){
        int xOffset = 5;
        int yOffset = 5;

        this.font.draw(poseStack, "Width: " + this.width, xOffset, yOffset += 10, 0x50eeee);
        this.font.draw(poseStack, "Height: " + this.height, xOffset, yOffset += 10, 0x50eeee);

        this.font.draw(poseStack, "Image Width: " + this.imageWidth, xOffset, yOffset += 10, 0x50eeee);
        this.font.draw(poseStack, "Image Height: " + this.imageHeight, xOffset, yOffset += 10, 0x50eeee);

        this.font.draw(poseStack, "Mouse X: " + mouseX, xOffset, yOffset += 10, 0x50eeee);
        this.font.draw(poseStack, "Mouse Y: " + mouseY, xOffset, yOffset += 10, 0x50eeee);

        yOffset += 10;

        for (UIElement element : new ReverseIterator<>(elements)){
            if (element.isMouseOver(mouseX, mouseY)){
                this.font.draw(poseStack, element.getClass().getSimpleName() + ", ID: " + element.id, xOffset, yOffset += 10, 0x50eeee);

                int borderColor = 0xFFAA2244;

                int x = element.posX -1;
                int y = element.posY -1;
                int width = element.width + 1;
                int height = element.height + 1;

                // Mouseover element border
                hLine(poseStack, x, x + width, y, borderColor);
                hLine(poseStack, x, x + width, y + height, borderColor);
                vLine(poseStack, x, y, y + height, borderColor);
                vLine(poseStack, x + width, y, y + height, borderColor);
            }
        }
    }


    // <Getters>

    public ItemRenderer getItemRenderer(){
        return this.itemRenderer;
    }

    public Font getFontRenderer(){
        return this.font;
    }
}
