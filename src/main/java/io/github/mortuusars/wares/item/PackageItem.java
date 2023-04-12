package io.github.mortuusars.wares.item;

import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.block.entity.PackageBlockEntity;
import io.github.mortuusars.wares.data.Package;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PackageItem extends BlockItem {
    public PackageItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public @NotNull SoundEvent getEatingSound() {
        return Wares.SoundEvents.PAPER_TEAR.get();
    }

    @Override
    public int getUseDuration(@NotNull ItemStack pStack) {
        return 30;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack pStack) {
        return UseAnim.EAT;
    }
    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        if (!context.isSecondaryUseActive()) {
            Objects.requireNonNull(context.getPlayer()).startUsingItem(context.getHand());
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }

        return super.useOn(context);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack usedStack = player.getItemInHand(hand);
        if (!(usedStack.getItem() instanceof PackageItem))
            return InteractionResultHolder.pass(usedStack);

        player.startUsingItem(hand);

        return InteractionResultHolder.success(usedStack);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity livingEntity) {
        if (level instanceof ServerLevel serverLevel) {
            Package pack = Package.fromItemStack(stack).orElse(Package.DEFAULT);
            Vec3 pos = livingEntity.position();
            for (ItemStack itemStack : pack.getItems(serverLevel)) {
                Containers.dropItemStack(level, pos.x, pos.y, pos.z, itemStack);
            }
            level.playSound(null, pos.x, pos.y, pos.z, Wares.SoundEvents.PAPER_TEAR.get(), SoundSource.PLAYERS,
                    1f, level.getRandom().nextFloat() * 0.2f + 0.9f);

            if (livingEntity instanceof ServerPlayer serverPlayer)
                serverPlayer.awardStat(Wares.Stats.PACKAGES_OPENED);
        }

        return ItemStack.EMPTY;
    }

    @Override
    protected boolean updateCustomBlockEntityTag(@NotNull BlockPos pos, Level level, @Nullable Player player, @NotNull ItemStack stack, @NotNull BlockState state) {
        if (level.getBlockEntity(pos) instanceof PackageBlockEntity packageBlockEntity)
            packageBlockEntity.setPackage(Package.fromItemStack(stack).orElse(Package.DEFAULT));

        return super.updateCustomBlockEntityTag(pos, level, player, stack, state);
    }
}
