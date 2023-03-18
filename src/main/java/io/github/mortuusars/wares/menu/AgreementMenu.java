package io.github.mortuusars.wares.menu;

import com.mojang.datafixers.util.Either;
import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.block.entity.DeliveryTableBlockEntity;
import io.github.mortuusars.wares.data.agreement.DeliveryAgreement;
import io.github.mortuusars.wares.menu.slot.DisplaySlot;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

public class AgreementMenu extends AbstractContainerMenu {
    public final Player player;
    public final Level level;
    public int slotsY;

    private final Either<DeliveryTableBlockEntity, DeliveryAgreement> tableOrAgreement;

    public AgreementMenu(int containerId, Inventory playerInventory, Either<DeliveryTableBlockEntity, ItemStack> source) {
//        super(Wares.MenuTypes.AGREEMENT.get(), containerId);
        super(null, containerId);
        this.player = playerInventory.player;
        this.level = playerInventory.player.level;

        tableOrAgreement = source.mapRight(stack -> DeliveryAgreement.fromItemStack(stack).orElse(DeliveryAgreement.EMPTY));

//        this.agreement = DeliveryAgreement.fromItemStack(agreementStack).orElse(DeliveryAgreement.EMPTY);

        DeliveryAgreement agreement = getAgreement();

        List<ItemStack> allStacks = new ArrayList<>();

        allStacks.addAll(agreement.getRequestedItems());
        allStacks.addAll(agreement.getPaymentItems());

        SimpleContainer container = new SimpleContainer(allStacks.toArray(new ItemStack[]{}));

        List<ItemStack> requestedItems = agreement.getRequestedItems();

        if (!playerInventory.player.level.isClientSide)
            return;

        slotsY = 40;

        if (agreement.getMessage().isPresent()) {
            int lines = Math.min(6, Minecraft.getInstance().font.split(agreement.getMessage().get(), 186 - (12 * 2)).size());
            slotsY += lines * Minecraft.getInstance().font.lineHeight + 13;
        }

        arrangeSlots(container, requestedItems.size(), 0, 23, slotsY);
        arrangeSlots(container, agreement.getPaymentItems().size(), requestedItems.size(), 109, slotsY);
    }

    public DeliveryAgreement getAgreement() {
        return tableOrAgreement.map(deliveryTableBlockEntity -> deliveryTableBlockEntity.getAgreement(), agreement -> agreement);
//        return tableOrAgreement.map(deliveryTableBlockEntity -> {
//            BlockPos pos = deliveryTableBlockEntity.getBlockPos();
//            if (level.getBlockEntity(pos) instanceof DeliveryTableBlockEntity deliveryEntity) {
//                return DeliveryAgreement.fromItemStack(deliveryEntity.getItem(DeliveryTableBlockEntity.AGREEMENT_SLOT)).orElse(DeliveryAgreement.EMPTY);
//            }
//
//            return DeliveryAgreement.EMPTY;
//            BlockEntity blockEntity = deliveryTableBlockEntity.getLevel().getBlockEntity(pos);

//            ItemStack agreementStack = deliveryTableBlockEntity.getItem(DeliveryTableBlockEntity.AGREEMENT_SLOT);
//            if (agreementStack.isEmpty())
//                return DeliveryAgreement.EMPTY;
//            else
//            return DeliveryAgreement.fromItemStack(agreementStack).orElse(DeliveryAgreement.EMPTY);
//        }, agreement -> agreement);
    }

    protected int arrangeSlots(Container container, int count, int startIndex, int startX, int startY) {

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



//        int maxWidth = 54; // 3 slots
//
//        int rowX = count >= 3 ? startX : startX + ((maxWidth / 2) - (18 * count / 2));
//        int rowStartY = count > 3 ? startY : startY + 9; // centered
//
//        int rows = count > 3 ? 2 : 1;
//
//
//        for (int row = 0; row < rows; row++) {
//            for (int column = 0; column < 3; column++) {
//
//                if (index >= startIndex + count)
//                    break;
//
//                int x = row == rows - 1 ?
//                int y =
//                this.addSlot(new DisplaySlot(container, index, ));
//                index++;
//            }
//        }
//
//        for (int i = startIndex; i < startIndex + count; i++) {
//
//            int posX = count >= 3 ?
//
////            int posX = rowX + ((i - startIndex) % 3 * 18);
////            int posY = i - startIndex >= 3 ? rowStartY + 18 : rowStartY;
//            this.addSlot(new DisplaySlot(container, i, posX, posY));
//        }

//        return count > 3 ? rowStartY + 18 + 18 : rowStartY + 18;

        return 0;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.isHolding(stack -> stack.is(Wares.Tags.Items.AGREEMENTS));
    }

    public static AgreementMenu fromBuffer(int containerID, Inventory inventory, FriendlyByteBuf buffer) {
        ItemStack itemStack = buffer.readItem();
        DeliveryAgreement deliveryAgreement = DeliveryAgreement.fromItemStack(itemStack).get();
        return new AgreementMenu(containerID, inventory, Either.right(itemStack));
    }
}
