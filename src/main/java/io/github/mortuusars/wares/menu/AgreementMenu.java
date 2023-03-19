package io.github.mortuusars.wares.menu;

import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.data.LangKeys;
import io.github.mortuusars.wares.data.agreement.DeliveryAgreement;
import io.github.mortuusars.wares.menu.slot.DisplaySlot;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class AgreementMenu extends AbstractContainerMenu {
    public final Inventory playerInventory;
    public final Player player;
    public final Level level;
    protected final Supplier<DeliveryAgreement> agreementSupplier;
    public int slotsStartPosY;

    public AgreementMenu(int containerId, Inventory playerInventory, Supplier<DeliveryAgreement> agreementSupplier) {
        super(null, containerId);
        this.player = playerInventory.player;
        this.level = playerInventory.player.level;
        this.playerInventory = playerInventory;
        this.agreementSupplier = agreementSupplier;

        if (!playerInventory.player.level.isClientSide)
            throw new IllegalStateException("AgreementMenu can exist only on client-side.");

        DeliveryAgreement agreement = getAgreement();

        slotsStartPosY = 74;

        if (agreement.getMessage().isPresent()) {
            // Measures message length to shift slots down:
            int lines = Math.min(6, Minecraft.getInstance().font.split(agreement.getMessage().get(), 186 - (12 * 2)).size());
            slotsStartPosY += lines * Minecraft.getInstance().font.lineHeight + 13;
        }

        SimpleContainer container = new SimpleContainer(Stream.concat(agreement.getRequestedItems().stream(),
                agreement.getPaymentItems().stream()).toArray(ItemStack[]::new));

        int requestedSlotsCount = agreement.getRequestedItems().size();
        int paymentSlotsCount = agreement.getPaymentItems().size();

        arrangeSlotsInGrid(container, requestedSlotsCount, 0, 23, slotsStartPosY);
        arrangeSlotsInGrid(container, paymentSlotsCount, requestedSlotsCount, 109, slotsStartPosY);
    }

    public DeliveryAgreement getAgreement() {
        return agreementSupplier.get();
    }

    public Component getTitle() {
        return getAgreement().getTitle().orElse(Wares.translate(LangKeys.GUI_DELIVERY_AGREEMENT_TITLE));
    }

    public Optional<Component> getMessage() {
        return getAgreement().getMessage();
    }

    protected int arrangeSlotsInGrid(Container container, int count, int startIndex, int startX, int startY) {

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
            int y = count > 3 ? startY : startY + 9;

            for (int column = 0; column < slots; column++) {
                this.addSlot(new DisplaySlot(container, startIndex + index, x + column * 18, y + row * 18));
                index++;
            }
        }

        return 0;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.isHolding(stack -> stack.is(Wares.Tags.Items.AGREEMENTS));
    }
}
