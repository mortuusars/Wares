package io.github.mortuusars.wares.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.data.Lang;
import io.github.mortuusars.wares.menu.CardboardBoxMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CardboardBoxScreen extends AbstractContainerScreen<CardboardBoxMenu> {

    private static final ResourceLocation TEXTURE = Wares.resource("textures/gui/cardboard_box.png");

    private final ItemStack cardboardBoxStack;

    private ImageButton packButton;
    private final Component packButtonTitle;
    private final Component packButtonTooltip;

    public CardboardBoxScreen(CardboardBoxMenu menu, Inventory playerinventory, Component title) {
        super(menu, playerinventory, title);

        this.packButtonTitle = Lang.GUI_CARDBOARD_BOX_PACK.translate();
        this.packButtonTooltip = Lang.GUI_CARDBOARD_BOX_PACK_TOOLTIP.translate();

        cardboardBoxStack = playerinventory.getItem(menu.cardboardBoxSlotId);
    }

    @Override
    protected void init() {
        super.init();

        packButton = new ImageButton(getGuiLeft() + 120, getGuiTop() + 33, 26, 20,
                176, 0, 20, TEXTURE, 256, 256,
                button -> {
                    assert minecraft != null;
                    assert minecraft.gameMode != null;
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, CardboardBoxMenu.PACK_BUTTON_ID);
                }, packButtonTitle);

        packButton.setTooltip(Tooltip.create(packButtonTooltip));

        addRenderableWidget(packButton);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        packButton.visible = menu.slots.stream().limit(CardboardBoxMenu.SLOTS).anyMatch(Slot::hasItem);

        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        graphics.renderItem(cardboardBoxStack, getGuiLeft() + menu.cardboardBoxSlotPos.getFirst(),
                getGuiTop() + menu.cardboardBoxSlotPos.getSecond());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }
}
