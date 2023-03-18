package io.github.mortuusars.wares.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.mpfui.helper.HorizontalAlignment;
import io.github.mortuusars.mpfui.widget.TextBlockWidget;
import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.data.agreement.DeliveryAgreement;
import io.github.mortuusars.wares.menu.AgreementMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
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

    protected DeliveryAgreement getAgreement() {
        return menu.getAgreement();
    }

    public static void showAsOverlay(Player player, Supplier<DeliveryAgreement> agreementSupplier) {
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
        this.imageWidth = 186;
        this.imageHeight = 248;
        super.init();
        inventoryLabelY = -1000;
        titleLabelY = -1000;

        TextBlockWidget title = new TextBlockWidget(this, menu.getTitle(),
                 getGuiLeft() + 20, getGuiTop() + 18, imageWidth - 40, 9)
                .setAlignment(HorizontalAlignment.CENTER)
                .setDefaultColor(FONT_COLOR);
        addRenderableOnly(title);

        if (menu.getMessage().isPresent()) {
            Component messageComponent = menu.getMessage().get();

            int messageWidth = imageWidth - 24; // Side margins
            int messageHeight = TextBlockWidget.getDesiredHeight(messageComponent, messageWidth, 6);

            TextBlockWidget message = new TextBlockWidget(this, messageComponent,
                    getGuiLeft() + 12, getGuiTop() + 40, messageWidth, messageHeight)
                    .setAlignment(HorizontalAlignment.CENTER)
                    .setDefaultColor(FONT_COLOR);
            addRenderableOnly(message);
        }

        ImageButton arrow = new ImageButton(getGuiLeft() + 84, getGuiTop() + menu.slotsStartYPos + 18 - 6, 19, 11, 186, 0, 0, TEXTURE, 256, 256, pButton -> {
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

        font.draw(poseStack, "R: " + getAgreement().getRemaining(), 140, 200, FONT_COLOR);
    }
}
