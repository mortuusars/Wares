package io.github.mortuusars.wares.block;

import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.block.entity.DeliveryTableBlockEntity;
import io.github.mortuusars.wares.client.gui.agreement.AgreementGUI;
import io.github.mortuusars.wares.data.agreement.AgreementType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"NullableProblems", "deprecation"})
public class DeliveryTableBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<AgreementType> AGREEMENT = EnumProperty.create("agreement", AgreementType.class);

    private static final VoxelShape TABLE_SHAPE = Shapes.or(
            Block.box(0, 12, 0, 16, 16, 16), // Tabletop
            Block.box(1, 2, 1, 15, 12, 15),  // Main
            Block.box(1, 0, 1, 4, 2, 4), // Leg Front Right
            Block.box(12, 0, 1, 15, 2, 4), // Leg Front Left
            Block.box(1, 0, 12, 4, 2, 15), // Leg Back Right
            Block.box(12, 0, 12, 15, 2, 15)); // Leg Back Left
    private static final VoxelShape TABLE_WITH_AGREEMENT_SHAPE = Shapes.or(TABLE_SHAPE, Shapes.box(0.12f, 1f, 0.12f, 0.88f, 1.04f, 0.88f));

    public DeliveryTableBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(getStateDefinition().any()
                .setValue(FACING, Direction.NORTH)
                .setValue(AGREEMENT, AgreementType.NONE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, AGREEMENT);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(AGREEMENT) == AgreementType.NONE ? TABLE_SHAPE : TABLE_WITH_AGREEMENT_SHAPE;
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return getStateDefinition().any().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState blockState) {
        return new DeliveryTableBlockEntity(pos, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return !level.isClientSide ?
                createTickerHelper(blockEntityType, Wares.BlockEntities.DELIVERY_TABLE.get(),
                        (pLevel, pPos, pState, pBlockEntity) -> pBlockEntity.serverTick())
                : null;
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof DeliveryTableBlockEntity deliveryTableBlockEntity))
            return InteractionResult.FAIL;

        ItemStack agreementStack = deliveryTableBlockEntity.getAgreementItem();
        if (hitResult.getLocation().y > pos.getY() + 1 && !agreementStack.isEmpty()) {
            if (player.isSecondaryUseActive()) {
                if (!level.isClientSide) {
                    ItemStack agreement = deliveryTableBlockEntity.extractAgreementItem();
                    ItemEntity drop = player.drop(agreement, false);
                    if (drop != null)
                        drop.setPickUpDelay(0);
                }
            }
            else {
                if (level.isClientSide)
                    AgreementGUI.showAsOverlay(player, deliveryTableBlockEntity::getAgreement);

            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (player instanceof ServerPlayer serverPlayer)
            NetworkHooks.openGui(serverPlayer, deliveryTableBlockEntity, pos);

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
