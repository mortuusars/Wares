package io.github.mortuusars.wares.menu.slot;

import io.github.mortuusars.wares.Wares;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class BillSlot extends SlotItemHandler {
    public BillSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return stack.is(Wares.Tags.Items.BILLS) && super.mayPlace(stack);
    }
}
