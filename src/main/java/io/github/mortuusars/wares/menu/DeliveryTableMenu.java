package io.github.mortuusars.wares.menu;

import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.block.entity.DeliveryTableBlockEntity;
import io.github.mortuusars.wares.config.Config;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DeliveryTableMenu extends AbstractContainerMenu {
    public static final int MANUAL_DELIVERY_BUTTON_ID = 0;

    public final DeliveryTableBlockEntity blockEntity;
    private final ContainerData data;

    public DeliveryTableMenu(int containerId, final Inventory playerInventory, final DeliveryTableBlockEntity blockEntity, ContainerData containerData) {
        super(Wares.MenuTypes.DELIVERY_TABLE.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = containerData;

        IItemHandler itemHandler = blockEntity.getInventory();
        {
            // AGREEMENT
            this.addSlot(new SlotItemHandler(itemHandler, DeliveryTableBlockEntity.AGREEMENT_SLOT, 80, 16) {
                @Override
                public boolean mayPickup(Player playerIn) {
                    return super.mayPickup(playerIn) && !blockEntity.isAgreementLocked();
                }
            });

            // PACKAGES
            if (Config.DELIVERIES_REQUIRE_BOXES.get())
                this.addSlot(new SlotItemHandler(itemHandler, DeliveryTableBlockEntity.BOX_SLOT, 80, 60));

            int index = DeliveryTableBlockEntity.BOX_SLOT + 1;

            // INPUT
            for (int row = 0; row < 2; row++) {
                for (int column = 0; column < 3; column++) {
                    this.addSlot(new SlotItemHandler(itemHandler, index++, 14 + column * 18, 29 + row * 18));
                }
            }

            // OUTPUT
            for (int row = 0; row < 2; row++) {
                for (int column = 0; column < 3; column++) {
                    this.addSlot(new SlotItemHandler(itemHandler, index++, 110 + column * 18, 29 + row * 18) {
                        @Override
                        public boolean mayPlace(@NotNull ItemStack stack) {
                            return false;
                        }
                    });
                }
            }
        }

        // Player inventory slots
        for(int row = 0; row < 3; ++row) {
            for(int column = 0; column < 9; ++column) {
                this.addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 90 + row * 18));
            }
        }

        // Player hotbar slots
        // Hotbar should go after main inventory for Shift+Click to work properly.
        for(int index = 0; index < 9; ++index) {
            this.addSlot(new Slot(playerInventory, index, 8 + index * 18, 148));
        }

        this.addDataSlots(data);
    }

    public static DeliveryTableMenu fromBuffer(int containerID, Inventory playerInventory, FriendlyByteBuf buffer) {
        return new DeliveryTableMenu(containerID, playerInventory, getBlockEntity(playerInventory, buffer),
                new SimpleContainerData(DeliveryTableBlockEntity.CONTAINER_DATA_SIZE));
    }

    public float getDeliveryProgress() {
        int progress = data.get(DeliveryTableBlockEntity.CONTAINER_DATA_PROGRESS);
        int duration = data.get(DeliveryTableBlockEntity.CONTAINER_DATA_DURATION);
        return progress != 0 && duration != 0 ? progress / (float)duration : 0;
    }

    public boolean canDeliverManually() {
        return data.get(DeliveryTableBlockEntity.CONTAINER_DATA_CAN_DELIVER_MANUALLY) == 1;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = slots.get(index);
        ItemStack clickedStack = slot.getItem();

        if (index < DeliveryTableBlockEntity.SLOTS) {
            if (!moveItemStackTo(clickedStack, DeliveryTableBlockEntity.SLOTS, slots.size(), true))
                return ItemStack.EMPTY;

            // BEs inventory onContentsChanged is not fired when removing agreement by shift clicking.
            // So we force update the slot.
            // This is needed to update agreement-related stuff. (Blockstate was not updating properly to reflect the removal).
            if (index == DeliveryTableBlockEntity.AGREEMENT_SLOT)
                blockEntity.setItem(DeliveryTableBlockEntity.AGREEMENT_SLOT, slot.getItem());
        }
        else if (index < slots.size()) {
            if (!moveItemStackTo(clickedStack, 0, DeliveryTableBlockEntity.SLOTS, false))
                return ItemStack.EMPTY;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean clickMenuButton(@NotNull Player player, int buttonId) {
        if (buttonId == MANUAL_DELIVERY_BUTTON_ID) {
            blockEntity.startManualDelivery();
            return true;
        }

        return false;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return blockEntity.stillValid(player);
    }

    private static DeliveryTableBlockEntity getBlockEntity(final Inventory playerInventory, final FriendlyByteBuf data) {
        Objects.requireNonNull(playerInventory, "playerInventory cannot be null");
        Objects.requireNonNull(data, "data cannot be null");
        final BlockEntity blockEntityAtPos = playerInventory.player.level.getBlockEntity(data.readBlockPos());
        if (blockEntityAtPos instanceof DeliveryTableBlockEntity blockEntity)
            return blockEntity;
        throw new IllegalStateException("Block entity is not correct! " + blockEntityAtPos);
    }
}
