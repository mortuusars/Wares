package io.github.mortuusars.wares.menu;

import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.block.entity.DeliveryTableBlockEntity;
import io.github.mortuusars.wares.menu.slot.AgreementSlot;
import io.github.mortuusars.wares.menu.slot.OutputSlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.Objects;

public class DeliveryTableMenu extends AbstractContainerMenu {
    private final Level level;
    private final Inventory playerInventory;
    private final DeliveryTableBlockEntity blockEntity;
    private final ContainerData data;

    public DeliveryTableMenu(int containerId, final Inventory playerInventory, final DeliveryTableBlockEntity blockEntity, ContainerData containerData) {
        super(Wares.MenuTypes.DELIVERY_TABLE.get(), containerId);
        this.level = playerInventory.player.level;
        this.playerInventory = playerInventory;
        this.blockEntity = blockEntity;
        this.data = containerData;

        IItemHandler itemHandler = blockEntity.getInventory();
        {
            int index = 0;
            this.addSlot(new AgreementSlot(itemHandler, index++, 80, 17));

            // INPUT
            for (int row = 0; row < 2; row++) {
                for (int column = 0; column < 3; column++) {
                    this.addSlot(new SlotItemHandler(itemHandler, index++, 14 + column * 18, 27 + row * 18));
                }
            }

            // OUTPUT
            for (int row = 0; row < 2; row++) {
                for (int column = 0; column < 3; column++) {
                    this.addSlot(new OutputSlot(itemHandler, index++, 110 + column * 18, 27 + row * 18));
                }
            }
        }

        // Player hotbar slots
        for(int index = 0; index < 9; ++index) {
            this.addSlot(new Slot(playerInventory, index, 8 + index * 18, 142));
        }

        // Player inventory slots
        for(int row = 0; row < 3; ++row) {
            for(int column = 0; column < 9; ++column) {
                this.addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }

        this.addDataSlots(data);
    }

    public static DeliveryTableMenu fromBuffer(int containerID, Inventory playerInventory, FriendlyByteBuf buffer) {
        return new DeliveryTableMenu(containerID, playerInventory, getBlockEntity(playerInventory, buffer), new SimpleContainerData(2));
    }

    public float getDeliveryProgress() {
        int progress = data.get(DeliveryTableBlockEntity.CONTAINER_DATA_DELIVERY_PROGRESS);
        int duration = data.get(DeliveryTableBlockEntity.CONTAINER_DATA_DELIVERY_DURATION);
        float prog = progress / (float)duration;
//        Wares.LOGGER.info(prog + "");
        return progress != 0 && duration != 0 ? prog : 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        ItemStack clickedStack = slot.getItem();

        if (index < DeliveryTableBlockEntity.SLOTS) {
            if (!moveItemStackTo(clickedStack, DeliveryTableBlockEntity.SLOTS, slots.size(), true))
                return ItemStack.EMPTY;
        }
        else if (index >= DeliveryTableBlockEntity.SLOTS && index < slots.size()) {
            if (!moveItemStackTo(clickedStack, 0, DeliveryTableBlockEntity.SLOTS - 1, false))
                return ItemStack.EMPTY;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
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
