package io.github.mortuusars.wares.block;

import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.block.entity.PackageBlockEntity;
import io.github.mortuusars.wares.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PackageBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public static final VoxelShape SHAPE_X = Block.box(3, 0, 4, 13, 6, 12);
    public static final VoxelShape SHAPE_Y = Block.box(4, 0, 3, 12, 6, 13);

    public PackageBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(getStateDefinition().any()
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? SHAPE_X : SHAPE_Y;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return getStateDefinition().any().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new PackageBlockEntity(pos, state);
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState pState) {
        return RenderShape.MODEL;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level instanceof ServerLevel serverLevel && level.getBlockEntity(pos) instanceof PackageBlockEntity packageBE) {
                if (packageBE.unpacksWhenBroken()) {
                    List<ItemStack> items = packageBE.getPackage().getItems(serverLevel, Vec3.atCenterOf(pos));
                    for (ItemStack item : items) {
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), item.copy());
                    }

                    level.playSound(null, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f,
                            Wares.SoundEvents.PAPER_TEAR.get(), SoundSource.PLAYERS,
                            1f, level.getRandom().nextFloat() * 0.5f + 0.8f);
                }
                else {
                    ItemStack stack = new ItemStack(Wares.Items.PACKAGE.get());
                    stack.setTag(packageBE.getPackage().toTag(new CompoundTag()));
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
                }
            }

            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean canSurvive(@NotNull BlockState state, @NotNull LevelReader level, BlockPos pos) {
        return Block.canSupportCenter(level, pos.below(), Direction.UP);
    }


    @SuppressWarnings("deprecation")
    @Override
    public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull PushReaction getPistonPushReaction(@NotNull BlockState state) {
        return Config.PACKAGE_PISTON_DESTROY.get() ? PushReaction.DESTROY : super.getPistonPushReaction(state);
    }

    @Override
    public void playerWillDestroy(Level level, @NotNull BlockPos pos, @NotNull BlockState pState, @NotNull Player player) {
        if (!level.isClientSide && player.isSecondaryUseActive()
                && Config.PACKAGE_SNEAK_PREVENTS_UNPACKING.get()
                && level.getBlockEntity(pos) instanceof PackageBlockEntity packageBlockEntity) {
            packageBlockEntity.setUnpacksWhenBroken(false);
        }

        super.playerWillDestroy(level, pos, pState, player);
    }
}
