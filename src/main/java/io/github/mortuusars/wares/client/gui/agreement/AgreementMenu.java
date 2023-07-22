package io.github.mortuusars.wares.client.gui.agreement;

import io.github.mortuusars.mpfui.component.Rectangle;
import io.github.mortuusars.wares.config.Config;
import io.github.mortuusars.wares.data.agreement.DeliveryAgreement;
import io.github.mortuusars.wares.data.agreement.component.RequestedItem;
import io.github.mortuusars.wares.menu.ItemDisplaySlot;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class AgreementMenu extends AbstractContainerMenu {
    public static final Rectangle AREA = new Rectangle(0, 0, 200, 256);
    public static final Rectangle CONTENT_AREA = AREA.shrink(12, 21, 12, 46);
    public static final Rectangle AREA_SHORT = new Rectangle(0, 0, 200, 212);
    public static final Rectangle CONTENT_AREA_SHORT = AREA_SHORT.shrink(12, 21, 12, 46);
    public static final int SHORT_AGREEMENT_THRESHOLD = 45;
    public static final int ELEMENTS_SPACING = 14;

    public final Inventory playerInventory;
    public final Player player;
    public final Level level;

    protected final Supplier<DeliveryAgreement> agreementSupplier;
    protected AgreementLayout layout;

    public final boolean isShort;
    public int posYOffset;

    public AgreementMenu(int containerId, Inventory playerInventory, Supplier<DeliveryAgreement> agreementSupplier) {
        super(null, containerId);
        this.player = playerInventory.player;
        this.level = playerInventory.player.level;
        this.playerInventory = playerInventory;
        this.agreementSupplier = agreementSupplier;

        if (!playerInventory.player.level.isClientSide)
            throw new IllegalStateException("AgreementMenu can exist only on client-side.");

        layout = layoutAgreementElements(CONTENT_AREA, ELEMENTS_SPACING);

        int height = layout.getHeight();

        if (CONTENT_AREA.height() - height >= SHORT_AGREEMENT_THRESHOLD) {
            isShort = true;
            layout = layoutAgreementElements(CONTENT_AREA_SHORT, ELEMENTS_SPACING);
            int yOffsetToCenter = (CONTENT_AREA_SHORT.height() - layout.getHeight()) / 2;
            posYOffset = CONTENT_AREA_SHORT.top() + yOffsetToCenter;
        }
        else {
            int yOffsetToCenter = (CONTENT_AREA.height() - height) / 2;
            posYOffset = CONTENT_AREA.top() + yOffsetToCenter;
            isShort = false;
        }

        DeliveryAgreement agreement = getAgreement();


        // Slots:
        int requestedSlotsCount = agreement.getRequested().size();
        int paymentSlotsCount = agreement.getPayment().size();
        boolean shouldCenterY = requestedSlotsCount > 3 || paymentSlotsCount > 3;

        Function<Integer, List<ItemStack>> requestedStacksForSlot = integer -> agreement.getRequested().get(integer).getStacks();
        Function<Integer, Component> requestedTooltipForSlot = integer -> {
            RequestedItem requestedItem = agreement.getRequested().get(integer);
            return requestedItem.getTagOrItem()
                    .map(tag -> Component.translatable("gui.wares.agreement.tag_slot_tooltip", "#" + tag.location()),
                         item -> Component.empty());
        };

        Function<Integer, List<ItemStack>> paymentStacksForSlot = integer -> List.of(agreement.getPayment().get(integer));
        Function<Integer, Component> paymentTooltipForSlot = integer -> Component.empty();


        Rectangle slotsRect = layout.getElement(AgreementLayout.Element.SLOTS);
        if (slotsRect != null) { // Extra safety. Should not be null here.
            int slotsPosY = posYOffset + slotsRect.top();
            arrangeDisplaySlotsInGrid(requestedSlotsCount, 0, 30, slotsPosY, shouldCenterY, requestedStacksForSlot, requestedTooltipForSlot);
            arrangeDisplaySlotsInGrid(paymentSlotsCount, requestedSlotsCount, 116, slotsPosY, shouldCenterY, paymentStacksForSlot, paymentTooltipForSlot);
        }
    }

    public int getUIWidth() {
        return isShort ? AREA_SHORT.width() : AREA.width();
    }

    public int getUIHeight() {
        return isShort ? AREA_SHORT.height() : AREA.height();
    }

    public AgreementLayout getLayout() {
        return layout;
    }

    public DeliveryAgreement getAgreement() {
        return agreementSupplier.get();
    }

    public MutableComponent getTitle() {
        Component title = getAgreement().getTitle();
        return title.equals(Component.empty()) ? Component.translatable("gui.wares.agreement.title") : title.copy();
    }

    public MutableComponent getMessage() {
        // Copying component here because on every consecutive call unwanted appends will be made.
        MutableComponent message = (MutableComponent) getAgreement().getMessage();
        message = message.equals(Component.empty()) ? Component.translatable("gui.wares.agreement.message") : message.copy();

        if (Config.AGREEMENT_APPEND_BUYER_INFO_TO_MESSAGE.get()) {
            boolean hasBuyerName = Minecraft.getInstance().font.width(getAgreement().getBuyerName()) != 0;
            boolean hasBuyerAddress = Minecraft.getInstance().font.width(getAgreement().getBuyerAddress()) != 0;

            if (hasBuyerName || hasBuyerAddress)
                message.append("\n");

            if (hasBuyerName) {
                message.append("\n");
                message.append(getAgreement().getBuyerName());
            }

            if (hasBuyerAddress) {
                message.append("\n");
                message.append(getAgreement().getBuyerAddress());
            }
        }

        return message;
    }

    @SuppressWarnings("SameParameterValue")
    protected AgreementLayout layoutAgreementElements(Rectangle availableArea, int elementsSpacing) {
        int lineHeight = Minecraft.getInstance().font.lineHeight;

        int slotsHeight = getAgreement().getRequested().size() > 3 || getAgreement().getPayment().size() > 3 ? 36 : 18;

        // Info
        boolean shouldDisplayOrderedCount = !getAgreement().isInfinite();
        boolean shouldDisplayExpirationTime = !getAgreement().isExpired(level.getGameTime());

        int infoHeight = 0;

        if (shouldDisplayOrderedCount)
            infoHeight += lineHeight;

        if (shouldDisplayExpirationTime)
            infoHeight += lineHeight;


        int heightWithoutMessage = lineHeight + elementsSpacing + slotsHeight + (infoHeight > 0 ? elementsSpacing + infoHeight : 0);
        int heightForMessage = availableArea.height() - heightWithoutMessage - elementsSpacing;

        // Message
        List<FormattedCharSequence> messageLines = Minecraft.getInstance().font.split(getMessage(), availableArea.width());
        int messageHeight = Math.min(heightForMessage, messageLines.size() * lineHeight);


        AgreementLayout layout = new AgreementLayout();

        layout.append(AgreementLayout.Element.TITLE, availableArea.x(), availableArea.width(), lineHeight, 0);

        if (messageHeight > 0)
            layout.append(AgreementLayout.Element.MESSAGE, availableArea.x(), availableArea.width(), messageHeight, elementsSpacing);

        layout.append(AgreementLayout.Element.SLOTS, availableArea.x() + 18, availableArea.width() - 18 * 2, slotsHeight, elementsSpacing);

        int infoSpacing = elementsSpacing;

        if (shouldDisplayOrderedCount) {
            layout.append(AgreementLayout.Element.ORDERED, availableArea.x(), availableArea.width(), lineHeight, infoSpacing);
            infoSpacing = 0;
        }

        if (shouldDisplayExpirationTime)
            layout.append(AgreementLayout.Element.EXPIRY, availableArea.x(), availableArea.width(), lineHeight, infoSpacing);

        return layout;
    }

    protected void arrangeDisplaySlotsInGrid(int count, int startIndex, int startX, int startY, boolean centeredY,
                                             Function<Integer, List<ItemStack>> stacksForSlotIndex, Function<Integer, Component> tooltipsForSlotIndex) {
        List<Integer> rows = new ArrayList<>();

        if (count == 4) {
            rows.add(2);
            rows.add(2);
        }
        else {
            for (int i = 0; i < count / 3; i++) {
                rows.add(3);
            }

            int partialRow = count % 3;
            if (partialRow != 0)
                rows.add(partialRow);
        }

        int index = 0;

        for (int row = 0; row < rows.size(); row++) {
            int slots = rows.get(row);

            int x = startX + (27 - (18 * slots / 2));
            int y = count <= 3 && centeredY ? startY + 9 : startY;

            for (int column = 0; column < slots; column++) {
                List<ItemStack> stacks = stacksForSlotIndex.apply(index);
                Component tooltip = tooltipsForSlotIndex.apply(index);
                this.addSlot(new ItemDisplaySlot(stacks, tooltip,startIndex + index, x + column * 18, y + row * 18));
                index++;
            }
        }

    }



    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public void clicked(int pSlotId, int pButton, @NotNull ClickType pClickType, @NotNull Player pPlayer) {
        super.clicked(pSlotId, pButton, pClickType, pPlayer);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return getAgreement() != DeliveryAgreement.EMPTY;
    }
}
