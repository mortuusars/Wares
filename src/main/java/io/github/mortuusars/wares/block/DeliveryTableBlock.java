package io.github.mortuusars.wares.block;

import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.block.entity.DeliveryTableBlockEntity;
import io.github.mortuusars.wares.client.gui.agreement.AgreementGUI;
import io.github.mortuusars.wares.client.gui.agreement.AgreementScreen;
import io.github.mortuusars.wares.data.agreement.AgreementStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
    public static final EnumProperty<AgreementStatus> AGREEMENT_STATUS = EnumProperty.create("agreement_status", AgreementStatus.class);

    private static final VoxelShape TABLE_SHAPE = Shapes.box(0f, 0f, 0f, 1f, 1f, 1f);
    private static final VoxelShape TABLE_WITH_AGREEMENT_SHAPE = Shapes.or(TABLE_SHAPE, Shapes.box(0.15f, 1f, 0.15f, 0.85f, 1.07f, 0.85f));

    public DeliveryTableBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(getStateDefinition().any().setValue(AGREEMENT_STATUS, AgreementStatus.NONE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGREEMENT_STATUS);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!state.hasProperty(AGREEMENT_STATUS))
            return TABLE_SHAPE;
        return state.getValue(AGREEMENT_STATUS) == AgreementStatus.NONE ? TABLE_SHAPE : TABLE_WITH_AGREEMENT_SHAPE;
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
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
                    ItemStack agreement = deliveryTableBlockEntity.removeItem(DeliveryTableBlockEntity.AGREEMENT_SLOT, 1);
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
