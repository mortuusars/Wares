package io.github.mortuusars.wares.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.mpfui.component.Rectangle;
import io.github.mortuusars.mpfui.helper.HorizontalAlignment;
import io.github.mortuusars.mpfui.helper.TextBlockTooltipBehavior;
import io.github.mortuusars.mpfui.widget.CombinedContentWidget;
import io.github.mortuusars.mpfui.widget.TextBlock;
import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.client.gui.screen.agreement.AgreementLayout;
import io.github.mortuusars.wares.data.LangKeys;
import io.github.mortuusars.wares.data.agreement.Agreement;
import io.github.mortuusars.wares.menu.AgreementMenu;
import io.github.mortuusars.wares.util.TextUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class AgreementScreen extends AbstractContainerScreen<AgreementMenu> {
//    public static final int MESSAGE_MAX_LINES = 9;


    private static final ResourceLocation TEXTURE = new ResourceLocation(Wares.ID, "textures/gui/agreement.png");
    private static final int FONT_COLOR = 0xff886447;
    private Screen parentScreen;

    public AgreementScreen(AgreementMenu menu) {
        super(menu, menu.playerInventory, TextComponent.EMPTY);
        minecraft = Minecraft.getInstance(); // Minecraft is null if not updated here
    }

    protected Agreement getAgreement() {
        return menu.getAgreement();
    }

    public static void showAsOverlay(Player player, Supplier<Agreement> agreementSupplier) {
        if (!player.level.isClientSide)
            throw new IllegalStateException("Tried to open Agreement screen on the server. That's illegal.");

        int containerId = 1;
        AbstractContainerMenu containerMenu = Minecraft.getInstance().player.containerMenu;
        if (containerMenu != null)
            containerId = containerMenu.containerId + 1;

        AgreementMenu menu = new AgreementMenu(containerId, player.getInventory(), agreementSupplier);
        AgreementScreen screen = new AgreementScreen(menu);

        screen.showAsOverlay();
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
            menu.level.playSound(menu.player, pos.x, pos.y, pos.z, SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS,
                    1f, menu.level.getRandom().nextFloat() * 0.2f + 0.65f);
        }
    }

    @Override
    public void onClose() {
        if (isOpen() && minecraft != null) {
            minecraft.setScreen(parentScreen);
            parentScreen = null;

            Vec3 pos = menu.player.position();
            menu.level.playSound(menu.player, pos.x, pos.y, pos.z, SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS,
                    1f, menu.level.getRandom().nextFloat() * 0.2f + 1.1f);

            return;
        }
        super.onClose();
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

        TextBlock title = new TextBlock(menu.getTitle(), titleRect.left(), titleRect.top(), titleRect.width, titleRect.height)
                .setAlignment(HorizontalAlignment.CENTER)
                .setDefaultColor(FONT_COLOR);
        addRenderableOnly(title);


        // MESSAGE

        MutableComponent messageComponent = menu.getMessage();

//        Wares.LOGGER.debug(messageComponent.getString().getContents());

//        int messageWidth = imageWidth - SIDE_MARGIN * 2; // Side margins
//        int messageHeight = TextBlock.getDesiredHeight(messageComponent, messageWidth, MESSAGE_MAX_LINES);

        Rectangle messageRect = layout.getElement(AgreementLayout.Element.MESSAGE);
        if (messageRect != null) {
            TextBlock message = new TextBlock(messageComponent, messageRect.left(), messageRect.top(), messageRect.width, messageRect.height)
                    .setAlignment(HorizontalAlignment.CENTER)
                    .setDefaultColor(FONT_COLOR);
            addRenderableOnly(message);
        }


        // ARROW

        Rectangle slotsRect = layout.getElement(AgreementLayout.Element.SLOTS);

        int pX = slotsRect.centerX() - 9;
        int pY = slotsRect.centerY() - 6;
        ImageButton arrow = new ImageButton(pX, pY,
                19, 11, 200, 20, 0, TEXTURE, 256, 256, pButton -> {});
        addRenderableOnly(arrow);

        // ORDERS

        Rectangle orderedRect = layout.getElement(AgreementLayout.Element.ORDERED);
        if (orderedRect != null) {
            addRenderableOnly(new TextBlock(() -> new TextComponent(
                    TextUtil.shortenNumber(getAgreement().getDelivered()) + " / " +
                            TextUtil.shortenNumber(getAgreement().getOrdered())), orderedRect.left(), orderedRect.top(), orderedRect.width, orderedRect.height)
                    .setDefaultColor(FONT_COLOR)
                    .setTooltip(() -> {
                        int delivered = getAgreement().getDelivered();
                        int ordered = getAgreement().getOrdered();
                        if (delivered >= 1000 || ordered >= 1000)
                            return Wares.translate(LangKeys.GUI_DELIVERY_AGREEMENT_ORDERS_TOOLTIP, delivered, ordered);
                        else return TextComponent.EMPTY;
                    })
                    .setTooltipBehavior(TextBlockTooltipBehavior.REGULAR_ONLY)
                    .setAlignment(HorizontalAlignment.CENTER));

//            int x = getGuiLeft() + SIDE_MARGIN;
//            int y = deliveryInfoPosY;
//
//            CombinedContentWidget ordersWrapperForTooltip = new CombinedContentWidget(x, y,
//                    width, height, Wares.translate(LangKeys.GUI_DELIVERY_AGREEMENT_ORDERS), pButton -> {},
//                    (pButton, pPoseStack, pMouseX, pMouseY) -> {
//                        int delivered = getAgreement().getDelivered();
//                        int ordered = getAgreement().getOrdered();
//                        if (delivered >= 1000 || ordered >= 1000) {
//                            this.renderTooltip(pPoseStack,
//                                    Wares.translate(LangKeys.GUI_DELIVERY_AGREEMENT_ORDERS_TOOLTIP,
//                                            delivered, ordered), pMouseX, pMouseY);
//                        }
//                    });


//            ordersWrapperForTooltip.elements.add(new TextBlock(this, () ->
//                    new TextComponent(TextUtil.shortenNumber(getAgreement().getDelivered()) + " / " +
//                            TextUtil.shortenNumber(getAgreement().getOrdered())), x, y, maxElementWidth, font.lineHeight)
//                    .setDefaultColor(FONT_COLOR)
//                    .setAlignment(HorizontalAlignment.CENTER));
//
//            addRenderableOnly(ordersWrapperForTooltip);
//
//            deliveryInfoPosY += 14;
        }


        // EXPIRY

        Rectangle expiryRect = layout.getElement(AgreementLayout.Element.EXPIRY);
        if (expiryRect != null) {
            addRenderableOnly(new TextBlock(() -> TextUtil.timeFromTicks(getAgreement().getExpireTime() - menu.level.getGameTime()),
                    expiryRect.left(), expiryRect.top(), expiryRect.width, expiryRect.height)
                    .setAlignment(HorizontalAlignment.CENTER)
                    .setTooltip(Wares.translate(LangKeys.GUI_DELIVERY_AGREEMENT_EXPIRES))
                    .setTooltipBehavior(TextBlockTooltipBehavior.REGULAR_ONLY)
                    .setDefaultColor(0xad3232));


//            CombinedContentWidget expireTimeTooltipWrapper = new CombinedContentWidget(getGuiLeft() + 23, deliveryInfoPosY, 28, font.lineHeight,
//                    Wares.translate(LangKeys.GUI_DELIVERY_AGREEMENT_EXPIRES), pButton -> {},
//                    (pButton, pPoseStack, pMouseX, pMouseY) -> this.renderTooltip(pPoseStack,
//                            Wares.translate(LangKeys.GUI_DELIVERY_AGREEMENT_EXPIRES_TOOLTIP, time.getContents()), pMouseX, pMouseY));
//
//            expireTimeTooltipWrapper.elements.add(new TextBlock(this, time, getGuiLeft() + SIDE_MARGIN, deliveryInfoPosY, maxElementWidth, font.lineHeight)
//                    .setAlignment(HorizontalAlignment.CENTER)
//                    .setDefaultColor(0xad3232));
//
//            addRenderableOnly(expireTimeTooltipWrapper);
        }


        // SEAL

//        ImageButton seal = new ImageButton(getGuiLeft() + 81, getGuiTop() + 221,
//                36, 35, 200, 32, 0, TEXTURE, 256, 256, pButton -> {});
//        addRenderableOnly(seal);
    }

    @Override
    public void renderBg(@NotNull PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        boolean isSmall = true;

        if (isSmall) {
            this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, 111);
            this.blit(poseStack, this.leftPos, this.topPos + 111, 0, 155, this.imageWidth, 101);
        }
        else
            this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

//        fill(poseStack, getGuiLeft() + AgreementMenu.CONTENT_AREA.left(), getGuiTop() + AgreementMenu.CONTENT_AREA.top(),
//                getGuiLeft() + AgreementMenu.CONTENT_AREA.right(), getGuiTop() + AgreementMenu.CONTENT_AREA.bottom(), 0x33fa5f4e);

        //Slots BG
        for (Slot slot : menu.slots) {
            this.blit(poseStack, getGuiLeft() + slot.x - 1, getGuiTop() + slot.y - 1, 201, 1, 18, 18);
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
