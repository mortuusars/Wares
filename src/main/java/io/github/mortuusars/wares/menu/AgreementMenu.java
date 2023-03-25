package io.github.mortuusars.wares.menu;

import com.mojang.datafixers.util.Pair;
import io.github.mortuusars.mpfui.component.Rectangle;
import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.client.gui.screen.AgreementScreen;
import io.github.mortuusars.wares.client.gui.screen.agreement.AgreementLayout;
import io.github.mortuusars.wares.data.LangKeys;
import io.github.mortuusars.wares.data.agreement.Agreement;
import io.github.mortuusars.wares.menu.slot.DisplaySlot;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Size2i;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

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

    protected final Supplier<Agreement> agreementSupplier;
    protected AgreementLayout layout;

    public final boolean isShort;
    public int posYOffset;

    public AgreementMenu(int containerId, Inventory playerInventory, Supplier<Agreement> agreementSupplier) {
        super(null, containerId);
        this.player = playerInventory.player;
        this.level = playerInventory.player.level;
        this.playerInventory = playerInventory;
        this.agreementSupplier = agreementSupplier;

        if (!playerInventory.player.level.isClientSide)
            throw new IllegalStateException("AgreementMenu can exist only on client-side.");

        layout = layoutAgreementElements(CONTENT_AREA, ELEMENTS_SPACING);

        int height = layout.getHeight();

        if (CONTENT_AREA.height - height >= SHORT_AGREEMENT_THRESHOLD) {
            isShort = true;
            layout = layoutAgreementElements(CONTENT_AREA_SHORT, ELEMENTS_SPACING);
            int yOffsetToCenter = (CONTENT_AREA_SHORT.height - layout.getHeight()) / 2;
            posYOffset = CONTENT_AREA_SHORT.top() + yOffsetToCenter;
        }
        else {
            int yOffsetToCenter = (CONTENT_AREA_SHORT.height - height) / 2;
            posYOffset = CONTENT_AREA_SHORT.top() + yOffsetToCenter;
            isShort = false;
        }



        Agreement agreement = getAgreement();



        // Slots:

        SimpleContainer container = new SimpleContainer(Stream.concat(agreement.getRequestedItems().stream(),
                agreement.getPaymentItems().stream()).toArray(ItemStack[]::new));

        int requestedSlotsCount = agreement.getRequestedItems().size();
        int paymentSlotsCount = agreement.getPaymentItems().size();
        boolean shouldCenterY = requestedSlotsCount > 3 || paymentSlotsCount > 3;

        int slotsPosY = posYOffset + layout.getElement(AgreementLayout.Element.SLOTS).top();
        arrangeSlotsInGrid(container, requestedSlotsCount, 0, 30, slotsPosY, shouldCenterY);
        arrangeSlotsInGrid(container, paymentSlotsCount, requestedSlotsCount, 116, slotsPosY, shouldCenterY);
    }

    public int getUIWidth() {
        return isShort ? AREA_SHORT.width : AREA.width;
    }

    public int getUIHeight() {
        return isShort ? AREA_SHORT.height : AREA.height;
    }

    public AgreementLayout getLayout() {
        return layout;
    }

    public Agreement getAgreement() {
        return agreementSupplier.get();
    }

    public MutableComponent getTitle() {
        return ((MutableComponent) getAgreement().getTitle().orElse(Wares.translate(LangKeys.GUI_DELIVERY_AGREEMENT_TITLE)));
    }

    public MutableComponent getMessage() {
        MutableComponent message = getAgreement().getMessage()
                .orElse(Wares.translate(LangKeys.GUI_DELIVERY_AGREEMENT_MESSAGE)).copy();

        if (Minecraft.getInstance().font.width(message) > 0)
            message.append("\n");

        getAgreement().getBuyerName().ifPresent(nameComponent -> {
            message.append("\n");
            message.append(nameComponent);
        });

        getAgreement().getBuyerAddress().ifPresent(addressComponent -> {
            message.append("\n");
            message.append(addressComponent);
        });

        return message;
    }

    protected AgreementLayout layoutAgreementElements(Rectangle availableArea, int elementsSpacing) {
        Rectangle area = availableArea;
        int lineHeight = Minecraft.getInstance().font.lineHeight;
        int space = elementsSpacing;

        int slotsHeight = getAgreement().getRequestedItems().size() > 3 || getAgreement().getPaymentItems().size() > 3 ? 36 : 18;

        // Info
        boolean shouldDisplayOrderedCount = !getAgreement().isInfinite();
        boolean shouldDisplayExpirationTime = getAgreement().canExpire() && getAgreement().isNotExpired(level.getGameTime());

        int infoHeight = 0;

        if (shouldDisplayOrderedCount)
            infoHeight += lineHeight;

        if (shouldDisplayExpirationTime)
            infoHeight += lineHeight;


        int heightWithoutMessage = lineHeight + space + slotsHeight + (infoHeight > 0 ? space + infoHeight : 0);
        int heightForMessage = area.height - heightWithoutMessage - space;

        // Message
        List<FormattedCharSequence> messageLines = Minecraft.getInstance().font.split(getMessage(), area.width);
        int messageHeight = Math.min(heightForMessage, messageLines.size() * lineHeight);


        AgreementLayout layout = new AgreementLayout();

        layout.add(AgreementLayout.Element.TITLE, area.x, area.width, lineHeight, 0);

        if (messageHeight > 0)
            layout.add(AgreementLayout.Element.MESSAGE, area.x, area.width, messageHeight, space);

        layout.add(AgreementLayout.Element.SLOTS, area.x + 18, area.width - 18 * 2, slotsHeight, space);

        int infoSpacing = space;

        if (shouldDisplayOrderedCount) {
            layout.add(AgreementLayout.Element.ORDERED, area.x, area.width, lineHeight, infoSpacing);
            infoSpacing = 0;
        }

        if (shouldDisplayExpirationTime)
            layout.add(AgreementLayout.Element.EXPIRY, area.x, area.width, lineHeight, infoSpacing);

        return layout;
    }

    protected int arrangeSlotsInGrid(Container container, int count, int startIndex, int startX, int startY, boolean centeredY) {

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
                this.addSlot(new DisplaySlot(container, startIndex + index, x + column * 18, y + row * 18));
                index++;
            }
        }

        return 0;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
