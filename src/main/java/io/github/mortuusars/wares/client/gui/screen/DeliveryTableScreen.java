package io.github.mortuusars.wares.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.block.entity.DeliveryTableBlockEntity;
import io.github.mortuusars.wares.config.Config;
import io.github.mortuusars.wares.data.Lang;
import io.github.mortuusars.wares.menu.DeliveryTableMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class DeliveryTableScreen extends AbstractContainerScreen<DeliveryTableMenu> {
    public static final ResourceLocation TEXTURE = Wares.resource("textures/gui/delivery_table.png");

    private final Component manualDeliveryButtonTitle;
    private final MutableComponent manualDeliveryButtonTooltip;
    private ImageButton manualDeliveryButton;

    public DeliveryTableScreen(DeliveryTableMenu menu, Inventory playerinventory, Component title) {
        super(menu, playerinventory, title);
        this.manualDeliveryButtonTitle = Lang.GUI_DELIVERY_TABLE_MANUAL_DELIVERY.translate();
        manualDeliveryButtonTooltip = Lang.GUI_DELIVERY_TABLE_MANUAL_DELIVERY_TOOLTIP.translate();

        double manualDeliveryTimeModifier = Config.MANUAL_DELIVERY_TIME_MODIFIER.get();
        if (manualDeliveryTimeModifier > 1.0D) {
            String formattedModifier = manualDeliveryTimeModifier % 1 == 0 ?
                    String.format("%.0f", manualDeliveryTimeModifier) :
                    String.format("%.1f", manualDeliveryTimeModifier);
            manualDeliveryButtonTooltip.append("\n").append(Lang.GUI_DELIVERY_TABLE_MANUAL_DELIVERY_TOOLTIP_EXTRA_INFO.translate(formattedModifier)
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    protected void init() {
        super.init();
        imageWidth = 176;
        imageHeight = 172;
        inventoryLabelY = 79;

        this.manualDeliveryButton = new ImageButton(getGuiLeft() + 74, getGuiTop() + 36, 28, 20,
                176, 70, 20, TEXTURE, 256, 256,
                this::manualDeliveryButtonPressed,
                this.manualDeliveryButtonTitle);

        this.manualDeliveryButton.setTooltip(Tooltip.create(manualDeliveryButtonTooltip));

        addRenderableWidget(manualDeliveryButton);
    }

    private void manualDeliveryButtonPressed(Button button) {
        assert this.minecraft != null;
        assert this.minecraft.gameMode != null;
        this.minecraft.gameMode.handleInventoryButtonClick(menu.containerId, DeliveryTableMenu.MANUAL_DELIVERY_BUTTON_ID);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        manualDeliveryButton.visible = menu.canDeliverManually();

        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        if (menu.getCarried().isEmpty()) {
            Slot agreementSlot = menu.slots.get(DeliveryTableBlockEntity.AGREEMENT_SLOT);
            if (!agreementSlot.hasItem() && isHovering(agreementSlot.x, agreementSlot.y, 18, 18, mouseX, mouseY))
                graphics.renderTooltip(font, Lang.GUI_DELIVERY_TABLE_NO_AGREEMENT_TOOLTIP.translate(), mouseX, mouseY);

            if (Config.DELIVERIES_REQUIRE_BOXES.get()) {
                Slot boxSlot = menu.slots.get(DeliveryTableBlockEntity.BOX_SLOT);
                if (!boxSlot.hasItem() && isHovering(boxSlot.x, boxSlot.y, 18, 18, mouseX, mouseY))
                    graphics.renderTooltip(font, Lang.GUI_DELIVERY_TABLE_NO_BOXES_TOOLTIP.translate(), mouseX, mouseY);
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        // Agreement placeholder
        if (!menu.slots.get(DeliveryTableBlockEntity.AGREEMENT_SLOT).hasItem())
            graphics.blit(TEXTURE, leftPos + 79, topPos + 15, 176, 34, 18, 18);

        // PACKAGES SLOT
        if (Config.DELIVERIES_REQUIRE_BOXES.get()) {
            Slot packagesSlot = menu.slots.get(DeliveryTableBlockEntity.BOX_SLOT);
            graphics.blit(TEXTURE, leftPos + packagesSlot.x - 1, topPos + packagesSlot.y - 1, 176, 16, 18, 18);

            // Package placeholder
            if (!packagesSlot.hasItem())
                graphics.blit(TEXTURE, leftPos + packagesSlot.x - 1, topPos + packagesSlot.y - 1, 176, 52, 18, 18);
        }

        // ARROW
        float progress = menu.getDeliveryProgress();
        int arrowWidth = 22;
        int arrowHeight = 16;
        int progressInPixels = Mth.clamp((int)((arrowWidth + 1) * progress), 0, arrowWidth);
        graphics.blit(TEXTURE, leftPos + 77, topPos + 37, 176, 0, progressInPixels, arrowHeight);
    }
}


