package io.github.mortuusars.wares.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.menu.DeliveryTableMenu;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class DeliveryTableScreen extends AbstractContainerScreen<DeliveryTableMenu> {
    public static final ResourceLocation TEXTURE = Wares.resource("textures/gui/delivery_table.png");

    public DeliveryTableScreen(DeliveryTableMenu menu, Inventory playerinventory, Component title) {
        super(menu, playerinventory, title);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        float progress = menu.getDeliveryProgress();
        this.blit(poseStack, leftPos + 76, topPos + 37, 176, 0, Mth.clamp((int)(24 * progress), 1, 24), 17);
//        this.blit(poseStack, leftPos + 76, topPos + 37, 176, 0, 16, 17);
    }
}
