package io.github.mortuusars.wares.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.block.entity.DeliveryTableBlockEntity;
import io.github.mortuusars.wares.config.Config;
import io.github.mortuusars.wares.menu.DeliveryTableMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeliveryTableScreen extends AbstractContainerScreen<DeliveryTableMenu> {
    public static final ResourceLocation TEXTURE = Wares.resource("textures/gui/delivery_table.png");

    private final Component manualDeliveryButtonTitle;
    private final List<Component> manualDeliveryButtonTooltip;
    private ImageButton manualDeliveryButton;

    public DeliveryTableScreen(DeliveryTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.manualDeliveryButtonTitle = Component.translatable("gui.wares.delivery_table.manual_delivery");
        manualDeliveryButtonTooltip = new ArrayList<>();
        manualDeliveryButtonTooltip.add(Component.translatable("gui.wares.delivery_table.manual_delivery.tooltip"));

        double manualDeliveryTimeModifier = Config.MANUAL_DELIVERY_TIME_MODIFIER.get();
        if (manualDeliveryTimeModifier > 1.0D) {
            String formattedModifier = manualDeliveryTimeModifier % 1 == 0 ?
                    String.format("%.0f", manualDeliveryTimeModifier) :
                    String.format("%.1f", manualDeliveryTimeModifier);
            manualDeliveryButtonTooltip.add(Component.translatable("gui.wares.delivery_table.manual_delivery.tooltip_extra_info", formattedModifier)
                    .withStyle(ChatFormatting.GRAY));
        }

        playerInventory.player.playSound(Wares.SoundEvents.DELIVERY_TABLE_OPEN.get(), 0.8f,
                playerInventory.player.level.getRandom().nextFloat() * 0.2f + 0.9f);
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
                (button, poseStack, mouseX, mouseY) -> renderTooltip(poseStack, manualDeliveryButtonTooltip, Optional.empty(), mouseX, mouseY),
                this.manualDeliveryButtonTitle);

        addRenderableWidget(manualDeliveryButton);
    }

    private void manualDeliveryButtonPressed(Button button) {
        assert this.minecraft != null;
        assert this.minecraft.gameMode != null;
        this.minecraft.gameMode.handleInventoryButtonClick(menu.containerId, DeliveryTableMenu.MANUAL_DELIVERY_BUTTON_ID);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        manualDeliveryButton.visible = menu.canDeliverManually();

        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.renderTooltip(poseStack, mouseX, mouseY);

        if (menu.getCarried().isEmpty()) {
            Slot agreementSlot = menu.slots.get(DeliveryTableBlockEntity.AGREEMENT_SLOT);
            if (!agreementSlot.hasItem() && isHovering(agreementSlot.x, agreementSlot.y, 18, 18, mouseX, mouseY))
                this.renderTooltip(poseStack, Component.translatable("gui.wares.delivery_table.no_agreement.tooltip"), mouseX, mouseY);

            if (Config.DELIVERIES_REQUIRE_BOXES.get()) {
                Slot boxSlot = menu.slots.get(DeliveryTableBlockEntity.BOX_SLOT);
                if (!boxSlot.hasItem() && isHovering(boxSlot.x, boxSlot.y, 18, 18, mouseX, mouseY))
                    this.renderTooltip(poseStack, Component.translatable("gui.wares.delivery_table.no_packages.tooltip"), mouseX, mouseY);
            }
        }
    }

    @Override
    protected void renderBg(@NotNull PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        // Agreement Slot
        if (!menu.blockEntity.isAgreementLocked())
            this.blit(poseStack, leftPos + 79, topPos + 15, 176, 16, 18, 18);

        // Agreement placeholder
        if (!menu.slots.get(DeliveryTableBlockEntity.AGREEMENT_SLOT).hasItem())
            this.blit(poseStack, leftPos + 79, topPos + 15, 176, 34, 18, 18);

        // PACKAGES SLOT
        if (Config.DELIVERIES_REQUIRE_BOXES.get()) {
            Slot packagesSlot = menu.slots.get(DeliveryTableBlockEntity.BOX_SLOT);
            this.blit(poseStack, leftPos + packagesSlot.x - 1, topPos + packagesSlot.y - 1, 176, 16, 18, 18);

            // Package placeholder
            if (!packagesSlot.hasItem())
                this.blit(poseStack, leftPos + packagesSlot.x - 1, topPos + packagesSlot.y - 1, 176, 52, 18, 18);
        }

        // ARROW
        float progress = menu.getDeliveryProgress();
        int arrowWidth = 22;
        int arrowHeight = 16;
        int progressInPixels = Mth.clamp((int)((arrowWidth + 1) * progress), 0, arrowWidth);
        this.blit(poseStack, leftPos + 77, topPos + 37, 176, 0, progressInPixels, arrowHeight);
    }

    @Override
    public void onClose() {
        super.onClose();
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.playSound(Wares.SoundEvents.DELIVERY_TABLE_CLOSE.get(), 0.8f,
                Minecraft.getInstance().player.level.getRandom().nextFloat() * 0.2f + 0.9f);
        }
    }
}


