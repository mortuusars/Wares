package io.github.mortuusars.wares.block;

import io.github.mortuusars.wares.Wares;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class CardboardBoxBlock extends Block {

    public static final IntegerProperty PACKAGES = IntegerProperty.create("packages", 1, 4);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape ONE_AABB = Block.box(3, 0, 3, 13, 6, 13);
    private static final VoxelShape TWO_AABB = Block.box(1, 0, 1, 15, 6, 15);
    private static final VoxelShape THREE_AABB = TWO_AABB;
    private static final VoxelShape FOUR_AABB = Shapes.or(THREE_AABB, Block.box(3, 6, 3, 13, 12, 13));

    public CardboardBoxBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(getStateDefinition().any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PACKAGES, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PACKAGES, FACING);
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        return switch (state.getValue(PACKAGES)) {
            default -> ONE_AABB;
            case 2 -> TWO_AABB;
            case 3 -> THREE_AABB;
            case 4 -> FOUR_AABB;
        };
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos());
        if (blockstate.is(this))
            return blockstate.cycle(PACKAGES);
        else
            return getStateDefinition().any().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    @SuppressWarnings("SimplifiableConditionalExpression")
    public boolean canBeReplaced(@NotNull BlockState state, BlockPlaceContext context) {
        return !context.isSecondaryUseActive()
                && context.getItemInHand().getItem() == this.asItem()
                && state.getValue(PACKAGES) < 4 ? true : super.canBeReplaced(state, context);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos pos, Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        if (player.isSecondaryUseActive()) {

            if (level.isClientSide)
                return InteractionResult.SUCCESS;

            int packages = blockState.getValue(PACKAGES) - 1;

            if (packages <= 0)
                level.removeBlock(pos, false);
            else
                level.setBlock(pos, blockState.setValue(PACKAGES, packages), Block.UPDATE_ALL);


            level.playSound(null, pos, Wares.SoundEvents.CARDBOARD_HIT.get(), SoundSource.BLOCKS, 0.9f, level.getRandom().nextFloat() * 0.1f + 0.95f);

            player.addItem(new ItemStack(this.asItem()));
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
