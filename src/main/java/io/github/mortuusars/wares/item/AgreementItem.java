package io.github.mortuusars.wares.item;

import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.block.entity.DeliveryTableBlockEntity;
import io.github.mortuusars.wares.client.gui.agreement.AgreementGUI;
import io.github.mortuusars.wares.client.gui.screen.DeliveryTableScreen;
import io.github.mortuusars.wares.data.Lang;
import io.github.mortuusars.wares.data.agreement.Agreement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class AgreementItem extends Item {
    public AgreementItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack agreementStack, @NotNull ItemStack otherStack, @NotNull Slot slot, @NotNull ClickAction action, @NotNull Player player, @NotNull SlotAccess slotAccess) {
        // This method is called only client-side when in player inventory,
        // but both client- and server-side when in other containers.

        if (agreementStack.getItem() == this && otherStack.isEmpty() && action == ClickAction.SECONDARY) {
            Agreement agreement = Agreement.fromItemStack(agreementStack).orElse(Agreement.EMPTY);

            if (agreement == Agreement.EMPTY){
                Wares.LOGGER.error("Cannot read Delivery Agreement from stack nbt OR Agreement is empty. No UI will be shown.");
                return super.overrideOtherStackedOnMe(agreementStack, otherStack, slot, action, player, slotAccess);
            }

            if (player instanceof LocalPlayer) {
                Supplier<Agreement> agreementSupplier;

                if (Minecraft.getInstance().screen instanceof DeliveryTableScreen deliveryTableScreen &&
                    ItemStack.isSameItemSameTags(deliveryTableScreen.getMenu().blockEntity.getAgreementItem(), agreementStack)) {
                    agreementSupplier = () -> deliveryTableScreen.getMenu().blockEntity.getAgreement();
                }
                else
                    agreementSupplier = () -> agreement;

                AgreementGUI.showAsOverlay(player, agreementSupplier);
            }

            return true; // If true is returned - stack will not be picked up from a slot.
        }

        return super.overrideOtherStackedOnMe(agreementStack, otherStack, slot, action, player, slotAccess);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        String id = this.getDescriptionId(stack);

        if (Agreement.fromItemStack(stack).orElse(Agreement.EMPTY).isCompleted())
            id = id + "_completed";

        return new TranslatableComponent(id);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        Agreement.fromItemStack(stack).ifPresent(agreement -> tooltipComponents.add(Lang.AGREEMENT_VIEW_TOOLTIP.translate()));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();

        if (stack.is(this) && clickedFace == Direction.UP
                && level.getBlockEntity(clickedPos) instanceof DeliveryTableBlockEntity deliveryTableBlockEntity
                && deliveryTableBlockEntity.getAgreement() == Agreement.EMPTY) {
            deliveryTableBlockEntity.setItem(DeliveryTableBlockEntity.AGREEMENT_SLOT, stack.split(1));
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return super.onItemUseFirst(stack, context);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();

        if (stack.is(this) && clickedFace == Direction.UP
                && level.getBlockEntity(clickedPos) instanceof DeliveryTableBlockEntity deliveryTableBlockEntity
                && deliveryTableBlockEntity.getAgreement() == Agreement.EMPTY) {
            deliveryTableBlockEntity.setItem(DeliveryTableBlockEntity.AGREEMENT_SLOT, stack.split(1));
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return super.useOn(context);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack usedItemStack = player.getItemInHand(hand);

        Agreement agreement = Agreement.fromItemStack(usedItemStack).orElse(Agreement.EMPTY);

        if (agreement == Agreement.EMPTY){
            Wares.LOGGER.error("Cannot read Delivery Agreement from stack nbt OR Agreement is empty. No UI will be shown.");
            return InteractionResultHolder.pass(usedItemStack);
        }

        if (level.isClientSide)
            AgreementGUI.showAsOverlay(player, () -> agreement);

        return InteractionResultHolder.sidedSuccess(usedItemStack, level.isClientSide);
    }
}
