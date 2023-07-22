package io.github.mortuusars.wares.menu;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.data.Package;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CardboardBoxMenu extends AbstractContainerMenu {

    public static final int PACK_BUTTON_ID = 0;

    public static final int SLOTS = 6;

    public final int cardboardBoxSlotId;
    public Pair<Integer, Integer> cardboardBoxSlotPos = Pair.of(Integer.MIN_VALUE, Integer.MIN_VALUE);

    private final IItemHandler cardboardBoxItemHandler;
    private boolean itemsPacked = false;

    public CardboardBoxMenu(int containerId, final Inventory playerInventory, ItemStack cardboardBoxStack) {
        super(Wares.MenuTypes.CARDBOARD_BOX.get(), containerId);

        cardboardBoxItemHandler = new ItemStackHandler(SLOTS) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return !stack.is(Wares.Tags.Items.CARDBOARD_BOX_BLACKLISTED);
            }
        };

        int boxSlotsX = 62;
        int boxSlotsY = 26;

        int index = 0;
        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(new SlotItemHandler(cardboardBoxItemHandler, index,
                        boxSlotsX + column * 18, boxSlotsY + row * 18));
                index++;
            }
        }

        cardboardBoxSlotId = playerInventory.findSlotMatchingItem(cardboardBoxStack);

        //Player Inventory
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {

                if (index == cardboardBoxSlotId) {
                    cardboardBoxSlotPos = Pair.of(column * 18 + 8, row * 18 + 84);
                    continue;
                }

                addSlot(new Slot(playerInventory, (column + row * 9) + 9, column * 18 + 8, 84 + row * 18));
            }
        }

        //Hotbar
        for (int slot = 0; slot < 9; slot++) {
            if (slot == cardboardBoxSlotId) {
                cardboardBoxSlotPos = Pair.of(slot * 18 + 8, 142);
                continue;
            }

            addSlot(new Slot(playerInventory, slot, slot * 18 + 8, 142));
        }
    }

    public static CardboardBoxMenu fromBuffer(int containerID, Inventory playerInventory, FriendlyByteBuf buffer) {
        return new CardboardBoxMenu(containerID, playerInventory, buffer.readItem());
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = slots.get(index);
        ItemStack clickedStack = slot.getItem();

        if (index < SLOTS) {
            if (!moveItemStackTo(clickedStack, SLOTS, slots.size(), true))
                return ItemStack.EMPTY;
        }
        else if (index < slots.size()) {
            if (!moveItemStackTo(clickedStack, 0, SLOTS, false))
                return ItemStack.EMPTY;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean clickMenuButton(@NotNull Player player, int buttonId) {
        assert player instanceof ServerPlayer;

        if (buttonId == PACK_BUTTON_ID) {

            ItemStack boxStack = ItemStack.EMPTY;

            if (player.getInventory().getSelected().is(Wares.Items.CARDBOARD_BOX.get()))
                boxStack = player.getInventory().getSelected();
            else {
                for (ItemStack item : player.getInventory().items) {
                    if (item.is(Wares.Items.CARDBOARD_BOX.get())) {
                        boxStack = item;
                        break;
                    }
                }
            }

            if (boxStack.isEmpty()) {
                throw new IllegalStateException("If player doesn't have Cardboard Box in inventory this far - something went wrong.");
            }

            List<ItemStack> packedItems = new ArrayList<>();

            for (int slot = 0; slot < cardboardBoxItemHandler.getSlots(); slot++) {
                ItemStack stackInSlot = cardboardBoxItemHandler.getStackInSlot(slot);
                if (!stackInSlot.isEmpty())
                    packedItems.add(stackInSlot);
            }

            ItemStack packageStack = new ItemStack(Wares.Items.PACKAGE.get());
            new Package(Either.right(packedItems), player.getScoreboardName()).toItemStack(packageStack);

            boxStack.shrink(1);

            if (!player.addItem(packageStack))
                player.drop(packageStack, false);

            player.level.playSound(null, player, Wares.SoundEvents.CARDBOARD_BOX_USE.get(), SoundSource.PLAYERS,
                    1f, player.level.getRandom().nextFloat() * 0.3f + 0.85f);

            this.itemsPacked = true;
            player.closeContainer();

            return true;
        }

        return false;
    }

    @Override
    public void removed(@NotNull Player player) {
        if (player instanceof ServerPlayer serverPlayer && !itemsPacked) {
            for (int slot = 0; slot < SLOTS; slot++) {
                ItemStack stackInSlot = this.cardboardBoxItemHandler.getStackInSlot(slot);
                if (!stackInSlot.isEmpty())
                    serverPlayer.drop(stackInSlot, true);
            }
        }

        super.removed(player);
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getInventory().getItem(cardboardBoxSlotId).is(Wares.Items.CARDBOARD_BOX.get());
    }
}
