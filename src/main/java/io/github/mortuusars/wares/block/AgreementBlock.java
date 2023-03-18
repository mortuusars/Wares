package io.github.mortuusars.wares.block;

import com.mojang.datafixers.util.Either;
import io.github.mortuusars.wares.block.entity.DeliveryTableBlockEntity;
import io.github.mortuusars.wares.item.AgreementItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AgreementBlock extends Block {

    private static final VoxelShape SHAPE = Shapes.box(0.2f, 0f, 0.2f, 0.8f, 0.1f, 0.8f);

    public AgreementBlock(Properties properties) {
        super(properties);
    }


//    @Override
//    public void destroy(LevelAccessor level, BlockPos pPos, BlockState pState) {
//        if (level.getBlockEntity(pPos.below()) instanceof DeliveryTableBlockEntity deliveryTableBlockEntity) {
//            deliveryTableBlockEntity.removeItem(DeliveryTableBlockEntity.AGREEMENT_SLOT, 1)
//            Containers.dropItemStack(level, 0, 0, 0 );
//        }
//    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
//        return SHAPE;
        return Shapes.box(0.2f, 0f, 0.2f, 0.8f, 0.065f, 0.8f);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos blockpos = pos.below();
        return canSupportRigidBlock(level, blockpos) || canSupportCenter(level, blockpos, Direction.UP);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos.below()) instanceof DeliveryTableBlockEntity deliveryTableBlockEntity) {
            ItemStack agreementStack = deliveryTableBlockEntity.getItem(DeliveryTableBlockEntity.AGREEMENT_SLOT);
            if (agreementStack.getItem() instanceof AgreementItem agreementItem) {

                if (level.isClientSide)
                    agreementItem.openClientAgreementGui(Either.right(agreementStack), player);

                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return InteractionResult.PASS;
    }
}
