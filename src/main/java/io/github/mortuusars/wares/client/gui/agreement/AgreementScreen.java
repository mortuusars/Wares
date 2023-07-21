package io.github.mortuusars.wares.client.gui.agreement;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.mpfui.component.HorizontalAlignment;
import io.github.mortuusars.mpfui.component.Rectangle;
import io.github.mortuusars.mpfui.component.TooltipBehavior;
import io.github.mortuusars.mpfui.renderable.TextBlockRenderable;
import io.github.mortuusars.mpfui.renderable.TextureRenderable;
import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.client.gui.agreement.element.Seal;
import io.github.mortuusars.wares.client.gui.agreement.renderable.SealRenderable;
import io.github.mortuusars.wares.client.gui.agreement.renderable.StampRenderable;
import io.github.mortuusars.wares.config.Config;
import io.github.mortuusars.wares.data.Lang;
import io.github.mortuusars.wares.data.agreement.DeliveryAgreement;
import io.github.mortuusars.wares.menu.ItemDisplaySlot;
import io.github.mortuusars.wares.util.TextUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AgreementScreen extends AbstractContainerScreen<AgreementMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Wares.ID, "textures/gui/agreement.png");
    private static final ResourceLocation STAMPS_TEXTURE = new ResourceLocation(Wares.ID, "textures/gui/stamps.png");
    private static final int FONT_COLOR = 0xff886447;
    private final Seal seal;
    private Screen parentScreen;
    private int displayItemCycleTimer = 0;

    public AgreementScreen(AgreementMenu menu) {
        super(menu, menu.playerInventory, Component.empty());
        minecraft = Minecraft.getInstance(); // Minecraft is null if not updated here

        seal = new Seal(getAgreement().getSeal());
    }

    public boolean isOpen() {
        return minecraft != null && minecraft.screen == this;
    }

    public void showAsOverlay() {
        if (minecraft != null) {
            if (!isOpen()) {
                parentScreen = minecraft.screen;
            }
            minecraft.setScreen(this);

            Vec3 pos = menu.player.position();
            menu.level.playSound(menu.player, pos.x, pos.y, pos.z, Wares.SoundEvents.PAPER_CRACKLE.get(), SoundSource.MASTER,
                    1f, menu.level.getRandom().nextFloat() * 0.1f + 0.9f);
        }
    }

    @Override
    public void onClose() {
        if (isOpen() && minecraft != null) {
            minecraft.setScreen(parentScreen);
            parentScreen = null;

            Vec3 pos = menu.player.position();
            menu.level.playSound(menu.player, pos.x, pos.y, pos.z, Wares.SoundEvents.PAPER_CRACKLE.get(), SoundSource.MASTER,
                    1f, menu.level.getRandom().nextFloat() * 0.1f + 1.2f);

            return;
        }
        super.onClose();
    }

    protected DeliveryAgreement getAgreement() {
        return menu.getAgreement();
    }

    @Override
    protected void init() {
        this.imageWidth = menu.getUIWidth();
        this.imageHeight = menu.getUIHeight();
        super.init();
        inventoryLabelY = -1000;
        titleLabelY = -1000;

        AgreementLayout layout = menu.getLayout().offset(getGuiLeft(), getGuiTop() + menu.posYOffset);

        // TITLE
        Rectangle titleRect = layout.getElement(AgreementLayout.Element.TITLE);
        if (titleRect != null) { // Extra safety. Title should not be null.
            addRenderableOnly(new TextBlockRenderable(menu.getTitle(), titleRect.left(), titleRect.top(), titleRect.width, titleRect.height)
                    .setAlignment(HorizontalAlignment.CENTER)
                    .setDefaultColor(FONT_COLOR));
        }

        // MESSAGE
        Rectangle messageRect = layout.getElement(AgreementLayout.Element.MESSAGE);
        if (messageRect != null) {
            addRenderableOnly(new TextBlockRenderable(menu.getMessage(), messageRect.left(), messageRect.top(), messageRect.width, messageRect.height)
                    .setAlignment(HorizontalAlignment.CENTER)
                    .setDefaultColor(FONT_COLOR));
        }

        // ARROW
        Rectangle slotsRect = layout.getElement(AgreementLayout.Element.SLOTS);
        if (slotsRect != null) {
            addRenderableOnly(new TextureRenderable(slotsRect.centerX() - 9, slotsRect.centerY() - 6, 19, 11,
                    200, 20, 0, TEXTURE));
        }

        // ORDERS
        Rectangle orderedRect = layout.getElement(AgreementLayout.Element.ORDERED);
        if (orderedRect != null) {
            addRenderableOnly(new TextBlockRenderable(() -> Component.literal(
                    TextUtil.shortenNumber(getAgreement().getDelivered()) + " / " +
                            TextUtil.shortenNumber(getAgreement().getOrdered())), orderedRect.left(), orderedRect.top(), orderedRect.width, orderedRect.height)
                    .setDefaultColor(FONT_COLOR)
                    .setTooltip(() -> {
                        int delivered = getAgreement().getDelivered();
                        int ordered = getAgreement().getOrdered();
                        if (delivered >= 1000 || ordered >= 1000)
                            return Lang.GUI_AGREEMENT_DELIVERIES_TOOLTIP.translate(delivered, ordered);
                        else return Component.empty();
                    })
                    .setTooltipBehavior(TooltipBehavior.REGULAR_ONLY)
                    .setAlignment(HorizontalAlignment.CENTER));
        }

        // EXPIRY
        Rectangle expiryRect = layout.getElement(AgreementLayout.Element.EXPIRY);
        if (expiryRect != null) {
            addRenderableOnly(new TextBlockRenderable(() -> TextUtil.timeFromTicks(getAgreement().getExpireTimestamp() - menu.level.getGameTime()),
                    expiryRect.left(), expiryRect.top(), expiryRect.width, expiryRect.height)
                    .setAlignment(HorizontalAlignment.CENTER)
                    .setTooltip(Lang.GUI_AGREEMENT_EXPIRE_TIME.translate()))
                    .setTooltipBehavior(TooltipBehavior.REGULAR_ONLY)
                    .setDefaultColor(0xad3232)
                    .visibility((renderable, poseStack, mouseX, mouseY) -> !getAgreement().isCompleted()
                            && getAgreement().getExpireTimestamp() - menu.level.getGameTime() > 0);
        }

        // COMPLETED STAMP
        addRenderableOnly(new StampRenderable(getGuiLeft() + 12, getGuiTop() + 10, 71, 23,
                1, 1, STAMPS_TEXTURE)
                .setTooltip(Lang.GUI_AGREEMENT_COMPLETED.translate()))
                .setOpacity(0.75f)
                .visibility((renderable, poseStack, mouseX, mouseY) -> getAgreement().isCompleted());

        // EXPIRED STAMP
        addRenderableOnly(new StampRenderable(getGuiLeft() + 12, getGuiTop() + 10, 71, 23,
                1, 27, STAMPS_TEXTURE)
                .setTooltip(Lang.GUI_AGREEMENT_EXPIRED.translate()))
                .setOpacity(0.75f)
                .visibility((renderable, poseStack, mouseX, mouseY) -> !getAgreement().isCompleted() && getAgreement().isExpired(menu.level.getGameTime()));

        // SEAL

        MutableComponent buyerInfoTooltip = Component.literal("");

        if (!getAgreement().getBuyerName().equals(Component.empty()))
            buyerInfoTooltip.append(getAgreement().getBuyerName());

        if (!getAgreement().getBuyerAddress().equals(Component.empty())) {
            if (!getAgreement().getBuyerName().equals(Component.empty()))
                buyerInfoTooltip.append("\n");
            buyerInfoTooltip.append(getAgreement().getBuyerAddress());
        }

        SealRenderable sealRenderable = new SealRenderable(getGuiLeft() + (imageWidth / 2) - (Seal.WIDTH / 2),
                getGuiTop() + imageHeight - 42, seal, 28);
        if (font.width(buyerInfoTooltip) > 0)
            sealRenderable.setTooltip(buyerInfoTooltip);

        addRenderableOnly(sealRenderable);
    }

    @Override
    public void renderBg(@NotNull PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        if (menu.isShort) {
            this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, 111);
            this.blit(poseStack, this.leftPos, this.topPos + 111, 0, 155, this.imageWidth, 101);
        }
        else
            this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        //Slots BG
        for (Slot slot : menu.slots) {
            this.blit(poseStack, getGuiLeft() + slot.x - 1, getGuiTop() + slot.y - 1, 201, 1, 18, 18);
        }
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(PoseStack pPoseStack, int pX, int pY) {
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            ItemStack stack = this.hoveredSlot.getItem();
            List<Component> itemTooltip = getTooltipFromItem(stack);
            Optional<TooltipComponent> imageTooltip = stack.getTooltipImage();
            if (this.hoveredSlot instanceof ItemDisplaySlot itemDisplaySlot) {
                Component additionalTooltip = itemDisplaySlot.getAdditionalTooltip();
                if (!additionalTooltip.getString().isEmpty()) {
                    itemTooltip = new ArrayList<>(itemTooltip);
                    itemTooltip.add(additionalTooltip);
                }
            }
            this.renderTooltip(pPoseStack, itemTooltip, imageTooltip, pX, pY);
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (Config.AGREEMENT_CLOSE_WITH_RMB.get() && pButton == 1) {
            onClose();
            return true;
        }
        else
            return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    protected void slotClicked(@NotNull Slot pSlot, int pSlotId, int pMouseButton, @NotNull ClickType pType) {
        // Ignored
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        displayItemCycleTimer++;

        if (displayItemCycleTimer % 20 == 1)
            cycleDisplayItems(false);
    }

    protected void cycleDisplayItems(boolean backwards) {
        for (Slot slot : menu.slots) {
            if (slot instanceof ItemDisplaySlot itemDisplaySlot)
                itemDisplaySlot.cycleItem(backwards);
        }
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        boolean handled = super.mouseScrolled(pMouseX, pMouseY, pDelta);
        if (!handled) {
            cycleDisplayItems(pDelta > 0.0);
            displayItemCycleTimer = 1;
        }
        return handled;
    }
}
