package io.github.mortuusars.wares.menu.slot;

import io.github.mortuusars.wares.Wares;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class AgreementSlot extends SlotItemHandler {
    public AgreementSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return stack.is(Wares.Items.DELIVERY_AGREEMENT.get()) && super.mayPlace(stack);
    }
}
