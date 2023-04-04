package io.github.mortuusars.wares.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.block.entity.DeliveryTableBlockEntity;
import io.github.mortuusars.wares.config.Config;
import io.github.mortuusars.wares.data.Lang;
import io.github.mortuusars.wares.menu.DeliveryTableMenu;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

public class DeliveryTableScreen extends AbstractContainerScreen<DeliveryTableMenu> {
    public static final ResourceLocation TEXTURE = Wares.resource("textures/gui/delivery_table.png");

    public DeliveryTableScreen(DeliveryTableMenu menu, Inventory playerinventory, Component title) {
        super(menu, playerinventory, title);
    }

    @Override
    protected void init() {
        super.init();
        imageHeight = 172;
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.renderTooltip(poseStack, mouseX, mouseY);

        if (menu.getCarried().isEmpty()) {
            Slot agreementSlot = menu.slots.get(DeliveryTableBlockEntity.AGREEMENT_SLOT);
            if (!agreementSlot.hasItem() && isHovering(agreementSlot.x, agreementSlot.y, 18, 18, mouseX, mouseY))
                this.renderTooltip(poseStack, Lang.GUI_DELIVERY_TABLE_NO_AGREEMENT_TOOLTIP.translate(), mouseX, mouseY);

            if (Config.DELIVERIES_REQUIRE_PACKAGES.get()) {
                Slot packagesSlot = menu.slots.get(DeliveryTableBlockEntity.PACKAGES_SLOT);
                if (!packagesSlot.hasItem() && isHovering(packagesSlot.x, packagesSlot.y, 18, 18, mouseX, mouseY))
                    this.renderTooltip(poseStack, Lang.GUI_DELIVERY_TABLE_NO_PACKAGES_TOOLTIP.translate(), mouseX, mouseY);
            }
        }
    }

    @Override
    protected void renderBg(@NotNull PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        // PACKAGES SLOT
        if (Config.DELIVERIES_REQUIRE_PACKAGES.get()) {
            Slot packagesSlot = menu.slots.get(DeliveryTableBlockEntity.PACKAGES_SLOT);
            this.blit(poseStack, leftPos + packagesSlot.x - 1, topPos + packagesSlot.y - 1, 176, 16, 18, 18);
        }

        // ARROW
        float progress = menu.getDeliveryProgress();
        int arrowWidth = 22;
        int arrowHeight = 16;
        int progressInPixels = Mth.clamp((int)((arrowWidth + 1) * progress), 0, arrowWidth);
        this.blit(poseStack, leftPos + 77, topPos + 38, 176, 0, progressInPixels, arrowHeight);
    }
}
