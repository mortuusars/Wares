package io.github.mortuusars.wares.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.mpfui.helper.HorizontalAlignment;
import io.github.mortuusars.mpfui.widget.TextBlockWidget;
import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.data.LangKeys;
import io.github.mortuusars.wares.data.agreement.DeliveryAgreement;
import io.github.mortuusars.wares.menu.AgreementMenu;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

public class AgreementScreen extends AbstractContainerScreen<AgreementMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Wares.ID, "textures/gui/agreement.png");
    private static final int FONT_COLOR = 0xff886447;

    private final DeliveryAgreement agreement;

    public AgreementScreen(AgreementMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.agreement = menu.agreement;
    }

    @Override
    protected void init() {
        this.imageWidth = 186;
        this.imageHeight = 248;
        super.init();
        inventoryLabelY = -1000;
        titleLabelY = -1000;

        TextBlockWidget title = new TextBlockWidget(this, agreement.getTitle().orElse(new TranslatableComponent(LangKeys.GUI_DELIVERY_AGREEMENT_TITLE)),
                 getGuiLeft() + 20, getGuiTop() + 18, imageWidth - 40, 9)
                .setAlignment(HorizontalAlignment.CENTER)
                .setDefaultColor(FONT_COLOR);
        addRenderableOnly(title);

        if (agreement.getMessage().isPresent()) {
            int messageWidth = imageWidth - 24;
            int messageHeight = TextBlockWidget.getDesiredHeight(agreement.getMessage().get(), messageWidth, 6);

            TextBlockWidget message = new TextBlockWidget(this, agreement.getMessage().get(),
                    getGuiLeft() + 12, getGuiTop() + 40, messageWidth, messageHeight)
                    .setAlignment(HorizontalAlignment.CENTER)
                    .setDefaultColor(FONT_COLOR);
            addRenderableOnly(message);
        }

        ImageButton arrow = new ImageButton(getGuiLeft() + 84, getGuiTop() + menu.slotsY + 18 - 6, 19, 11, 186, 0, 0, TEXTURE, 256, 256, pButton -> {
            }, ((button, poseStack, mouseX, mouseY) -> this.renderTooltip(poseStack, new TextComponent("ARROW"), mouseX, mouseY)), new TextComponent(""));
        addRenderableOnly(arrow);

        ImageButton seal = new ImageButton(getGuiLeft() + 72, getGuiTop() + 222,
                42, 42, 214, 0, 42, TEXTURE, 256, 256, pButton -> {
        }, ((button, poseStack, mouseX, mouseY) -> this.renderTooltip(poseStack, new TextComponent("SEAL"), mouseX, mouseY)), new TextComponent(""));
        addRenderableOnly(seal);
    }

    @Override
    public void renderBg(@NotNull PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        //Slot BG
        for (Slot slot : menu.slots) {
            this.blit(poseStack, getGuiLeft() + slot.x - 1, getGuiTop() + slot.y - 1, 187, 21, 18, 18);
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(PoseStack pPoseStack, int pX, int pY) {
        super.renderTooltip(pPoseStack, pX, pY);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        super.renderLabels(poseStack, mouseX, mouseY);
    }
}
