package io.github.mortuusars.wares.menu;

import com.google.common.base.Preconditions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemDisplaySlot extends Slot {
    private final List<ItemStack> stacks;
    private final Component additionalTooltip;
    private int index = 0;

    public ItemDisplaySlot(List<ItemStack> stacks, Component additionalTooltip, int slot, int x, int y) {
        super(new SimpleContainer(slot), slot, x, y);
        Preconditions.checkState(stacks.size() > 0, "No items to display.");
        this.stacks = stacks;
        this.additionalTooltip = additionalTooltip;
    }

    @Override
    public @NotNull ItemStack getItem() {
        return stacks.get(index);
    }

    public Component getAdditionalTooltip() {
        return additionalTooltip;
    }

    public void cycleItem(boolean backwards) {
        if (stacks.size() == 0) {
            index = 0;
            return;
        }

        index = backwards ? index - 1 : index + 1;
        if (index < 0)
            index = stacks.size() - 1;
        else if (index > stacks.size() - 1)
            index = 0;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public boolean mayPickup(@NotNull Player pPlayer) {
        return false;
    }
}
