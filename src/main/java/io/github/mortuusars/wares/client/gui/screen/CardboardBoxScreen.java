package io.github.mortuusars.wares.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.menu.CardboardBoxMenu;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CardboardBoxScreen extends AbstractContainerScreen<CardboardBoxMenu> {

    private static final ResourceLocation TEXTURE = Wares.resource("textures/gui/cardboard_box.png");

    private final ItemStack cardboardBoxStack;

    private ImageButton packButton;
    private final Component packButtonTitle;
    private final Component packButtonTooltip;

    public CardboardBoxScreen(CardboardBoxMenu menu, Inventory playerinventory, Component title) {
        super(menu, playerinventory, title);

        this.packButtonTitle = Component.translatable("gui.wares.cardboard_box.pack");
        this.packButtonTooltip = Component.translatable("gui.wares.cardboard_box.pack.tooltip");

        cardboardBoxStack = playerinventory.getItem(menu.openedBoxSlotId);
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
                },
                (button, poseStack, mouseX, mouseY) -> renderTooltip(poseStack, this.packButtonTooltip, mouseX, mouseY),
                this.packButtonTitle);

        addRenderableWidget(packButton);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        packButton.visible = menu.slots.stream().limit(CardboardBoxMenu.SLOTS).anyMatch(Slot::hasItem);

        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.renderTooltip(poseStack, mouseX, mouseY);

        // Opened box
        itemRenderer.renderGuiItem(cardboardBoxStack, getGuiLeft() + menu.cardboardBoxSlotPos.getFirst(),
                getGuiTop() + menu.cardboardBoxSlotPos.getSecond());
        poseStack.translate(0, 0, 300);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 0.5f);
        blit(poseStack, getGuiLeft() + menu.cardboardBoxSlotPos.getFirst() - 1,
                getGuiTop() + menu.cardboardBoxSlotPos.getSecond() - 1, 4, 3, 18, 18);
    }

    @Override
    protected void renderBg(@NotNull PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }
}
