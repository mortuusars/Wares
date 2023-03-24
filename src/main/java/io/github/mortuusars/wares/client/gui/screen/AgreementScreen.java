package io.github.mortuusars.wares.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.mpfui.helper.HorizontalAlignment;
import io.github.mortuusars.mpfui.helper.LeftoverTooltipBehavior;
import io.github.mortuusars.mpfui.widget.CombinedContentWidget;
import io.github.mortuusars.mpfui.widget.TextBlockWidget;
import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.data.LangKeys;
import io.github.mortuusars.wares.data.agreement.Agreement;
import io.github.mortuusars.wares.menu.AgreementMenu;
import io.github.mortuusars.wares.util.TextUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
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
        this.imageWidth = 200;
        this.imageHeight = 256;
        super.init();
        inventoryLabelY = -1000;
        titleLabelY = -1000;

        int posY = 18;

        if (getAgreement().getBuyerName().isPresent()) {
            addRenderableOnly(new TextBlockWidget(this, getAgreement().getBuyerName().get(),
                    getGuiLeft() + 16, getGuiTop() + posY, imageWidth - 32, 9)
                    .setDefaultColor(FONT_COLOR)
                    .setAlignment(HorizontalAlignment.RIGHT));
            posY += 9;
        }

        if (getAgreement().getBuyerAddress().isPresent()) {
            addRenderableOnly(new TextBlockWidget(this, getAgreement().getBuyerAddress().get(),
                    getGuiLeft() + 16, getGuiTop() + posY, imageWidth - 32, 9)
                    .setDefaultColor(FONT_COLOR)
                    .setAlignment(HorizontalAlignment.RIGHT));
            posY += 9;
        }

        TextBlockWidget title = new TextBlockWidget(this, menu.getTitle(),
                 getGuiLeft() + 16, getGuiTop() + 54, imageWidth - 32, 9)
                .setAlignment(HorizontalAlignment.CENTER)
                .setDefaultColor(FONT_COLOR);
        addRenderableOnly(title);

        if (menu.getMessage().isPresent()) {
            Component messageComponent = menu.getMessage().get();

            int messageWidth = imageWidth - 24; // Side margins
            int messageHeight = TextBlockWidget.getDesiredHeight(messageComponent, messageWidth, 6);

            TextBlockWidget message = new TextBlockWidget(this, messageComponent,
                    getGuiLeft() + 12, getGuiTop() + 74, messageWidth, messageHeight)
                    .setAlignment(HorizontalAlignment.CENTER)
                    .setDefaultColor(FONT_COLOR);
            addRenderableOnly(message);
        }

        ImageButton arrow = new ImageButton(getGuiLeft() + 84, getGuiTop() + menu.slotsStartPosY + 18 - 6, 19, 11, 200, 20, 0, TEXTURE, 256, 256, pButton -> {
            }, ((button, poseStack, mouseX, mouseY) -> this.renderTooltip(poseStack, new TextComponent("ARROW"), mouseX, mouseY)), new TextComponent(""));
        addRenderableOnly(arrow);

        int slotsEndPosY = menu.slotsStartPosY;
        for (Slot slot : menu.slots) {
            if (slot.y > slotsEndPosY)
                slotsEndPosY = slot.y;
        }


        int deliveryInfoPosY = getGuiTop() + slotsEndPosY + 18 + 13;


        if (!getAgreement().isInfinite()) {
            int width = 60;
            int x = getGuiLeft() + (imageWidth / 2) - width / 2;
            int y = deliveryInfoPosY;
            int height = font.lineHeight;
            CombinedContentWidget ordersWrapperForTooltip = new CombinedContentWidget(x, y,
                    width, height, Wares.translate(LangKeys.GUI_DELIVERY_AGREEMENT_ORDERS), pButton -> {},
                    (pButton, pPoseStack, pMouseX, pMouseY) -> {
                        int delivered = getAgreement().getDelivered();
                        int ordered = getAgreement().getOrdered();
                        if (delivered >= 1000 || ordered >= 1000) {
                            this.renderTooltip(pPoseStack,
                                    Wares.translate(LangKeys.GUI_DELIVERY_AGREEMENT_ORDERS_TOOLTIP,
                                            delivered, ordered), pMouseX, pMouseY);
                        }
                    });
            ordersWrapperForTooltip.elements.add(new TextBlockWidget(this, () ->
                    new TextComponent(TextUtil.shortenNumber(getAgreement().getDelivered()) + " / " +
                            TextUtil.shortenNumber(getAgreement().getOrdered())), x, y, width, height)
                    .setDefaultColor(FONT_COLOR)
                    .setAlignment(HorizontalAlignment.CENTER));

            addRenderableOnly(ordersWrapperForTooltip);
        }


        if (!getAgreement().isInfinite() && getAgreement().getExperience() > 0) {
            int width = 34;
            int x = getGuiLeft() + imageWidth - 23 - width;
            CombinedContentWidget experience = new CombinedContentWidget(
                    x, deliveryInfoPosY, width, 9, Wares.translate(LangKeys.GUI_DELIVERY_AGREEMENT_EXPERIENCE),
                    (button) -> {
                    }, (pButton, pPoseStack, pMouseX, pMouseY) ->
                    this.renderTooltip(pPoseStack, Wares.translate(LangKeys.GUI_DELIVERY_AGREEMENT_EXPERIENCE_TOOLTIP), pMouseX, pMouseY));

            String xpStr = TextUtil.shortenNumber(getAgreement().getExperience());
            int xpTextWidth = font.width(xpStr);
            experience.elements.add(new TextBlockWidget(this,
                    new TextComponent(xpStr), x + width - xpTextWidth, deliveryInfoPosY, Math.min(xpTextWidth, width - 11), 9)
                    .setDefaultColor(0x32a02c)
                    .setTooltipBehavior(LeftoverTooltipBehavior.FULL));

            experience.elements.add(new ImageButton(x + width - xpTextWidth - 11, deliveryInfoPosY - 1,
                    9, 9, 186, 11, 0, TEXTURE, pButton -> {}));

            addRenderableOnly(experience);
        }


        if (getAgreement().canExpire() && getAgreement().isNotExpired(menu.level.getGameTime())) {
            MutableComponent time = TextUtil.timeFromTicks(getAgreement().getExpireTime() - menu.level.getGameTime());
            CombinedContentWidget expireTimeTooltipWrapper = new CombinedContentWidget(getGuiLeft() + 23, deliveryInfoPosY, 28, font.lineHeight,
                    Wares.translate(LangKeys.GUI_DELIVERY_AGREEMENT_EXPIRES), pButton -> {},
                    (pButton, pPoseStack, pMouseX, pMouseY) -> this.renderTooltip(pPoseStack,
                            Wares.translate(LangKeys.GUI_DELIVERY_AGREEMENT_EXPIRES_TOOLTIP, time.getContents()), pMouseX, pMouseY));

            expireTimeTooltipWrapper.elements.add(new TextBlockWidget(this, time, getGuiLeft() +23, deliveryInfoPosY, 28, font.lineHeight)
                    .setDefaultColor(0xad3232));

            addRenderableOnly(expireTimeTooltipWrapper);
        }


        ImageButton seal = new ImageButton(getGuiLeft() + 81, getGuiTop() + 22,
                36, 35, 200, 32, 0, TEXTURE, 256, 256, pButton -> {
        }/*, ((button, poseStack, mouseX, mouseY) -> this.renderTooltip(poseStack, new TextComponent("SEAL"), mouseX, mouseY)), new TextComponent("")*/);
        addRenderableOnly(seal);
    }

    @Override
    public void renderBg(@NotNull PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

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
