package io.github.mortuusars.wares.block;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.wares.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.block.DispenserBlock;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class PackageDispenseBehavior extends OptionalDispenseItemBehavior {
    private static final Logger LOGGER = LogUtils.getLogger();

    protected @NotNull ItemStack execute(@NotNull BlockSource source, @NotNull ItemStack stack) {
        if (!Config.PACKAGE_DISPENSER_PLACE.get())
            return super.execute(source, stack);

        this.setSuccess(false);
        Item item = stack.getItem();
        if (item instanceof BlockItem) {
            Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
            BlockPos blockpos = source.getPos().relative(direction);
            Direction direction1 = source.getLevel().isEmptyBlock(blockpos.below()) ? direction : Direction.UP;

            try {
                this.setSuccess(((BlockItem)item).place(new DirectionalPlaceContext(source.getLevel(), blockpos, direction, stack, direction1)).consumesAction());
            } catch (Exception exception) {
                LOGGER.error("Error trying to place package at {}", blockpos, exception);
            }
        }

        return stack;
    }
}
