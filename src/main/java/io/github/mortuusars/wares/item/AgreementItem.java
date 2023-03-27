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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
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
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        Agreement.fromItemStack(stack)
                .ifPresent(agreement -> tooltipComponents.add(Lang.AGREEMENT_VIEW_TOOLTIP.translate()
                        .withStyle(Style.EMPTY.withColor(0xd6b589))));
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack agreementStack, @NotNull ItemStack otherStack, @NotNull Slot slot, @NotNull ClickAction action, @NotNull Player player, @NotNull SlotAccess slotAccess) {
        // This method is called only client-side when in creative inventory,

        if (agreementStack.getItem() == this && otherStack.isEmpty() && action == ClickAction.SECONDARY) {
            Agreement agreement = Agreement.fromItemStack(agreementStack).orElse(Agreement.EMPTY);

            if (agreement == Agreement.EMPTY){
                Wares.LOGGER.error("Cannot read Delivery Agreement from stack nbt OR Agreement is empty. No UI will be shown.");
                return super.overrideOtherStackedOnMe(agreementStack, otherStack, slot, action, player, slotAccess);
            }

            if (agreementStack.is(Wares.Items.DELIVERY_AGREEMENT.get())) {
                if (agreement.isCompleted())
                    slot.set(convertToCompletedItem(agreementStack));
                else if (agreement.isExpired(player.level.getGameTime()))
                    slot.set(convertToExpiredItem(agreementStack));
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

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void inventoryTick(ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        if (stack.is(Wares.Items.DELIVERY_AGREEMENT.get()) && entity instanceof ServerPlayer serverPlayer
                && stack.hasTag() && stack.getTag().contains("Agreement", Tag.TAG_COMPOUND)) {

            CompoundTag agreementTag = stack.getTag().getCompound("Agreement");
            if (agreementTag.contains("expireTime")) {
                long expireTime = agreementTag.getLong("expireTime");
                if (expireTime > 0 && expireTime <= level.getGameTime()) {
                    ItemStack expiredStack = convertToExpiredItem(stack);
                    serverPlayer.getInventory().setItem(slotId, expiredStack);
                }
            }
            else if (agreementTag.contains("ordered") && agreementTag.contains("remaining")) {
                int ordered = agreementTag.getInt("ordered");
                int remaining = agreementTag.getInt("remaining");
                if (ordered > 0 && remaining <= 0) {
                    ItemStack completedStack = convertToCompletedItem(stack);
                    serverPlayer.getInventory().setItem(slotId, completedStack);
                }
            }
        }
    }

    public static @NotNull ItemStack convertToExpiredItem(ItemStack stack) {
        if (stack.isEmpty())
            throw new IllegalStateException("Tried to convert an empty ItemStack to Expired Delivery Agreement.");
        else if (stack.is(Wares.Items.COMPLETED_DELIVERY_AGREEMENT.get()))
            throw new IllegalStateException("Tried to convert Completed Delivery Agreement to Expired Delivery Agreement. Stack: '" + stack + '.');
        else if (stack.is(Wares.Items.EXPIRED_DELIVERY_AGREEMENT.get()))
            return stack;

        ItemStack expiredStack = new ItemStack(Wares.Items.EXPIRED_DELIVERY_AGREEMENT.get());

        CompoundTag agreementTag = stack.getTagElement("Agreement");
        if (agreementTag != null)
            expiredStack.getOrCreateTag().put("Agreement", agreementTag);

        return expiredStack;
    }

    public static @NotNull ItemStack convertToCompletedItem(ItemStack stack) {
        if (stack.isEmpty())
            throw new IllegalStateException("Tried to convert an empty ItemStack to Completed Delivery Agreement.");
        else if (stack.is(Wares.Items.EXPIRED_DELIVERY_AGREEMENT.get()))
            throw new IllegalStateException("Tried to convert Expired Delivery Agreement to Completed Delivery Agreement. Stack: '" + stack + '.');
        else if (stack.is(Wares.Items.COMPLETED_DELIVERY_AGREEMENT.get()))
            return stack;

        ItemStack completedStack = new ItemStack(Wares.Items.COMPLETED_DELIVERY_AGREEMENT.get());

        CompoundTag agreementTag = stack.getTagElement("Agreement");
        if (agreementTag != null)
            completedStack.getOrCreateTag().put("Agreement", agreementTag);

        return completedStack;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();

        if (stack.is(Wares.Items.DELIVERY_AGREEMENT.get())
                && clickedFace == Direction.UP
                && level.getBlockEntity(clickedPos) instanceof DeliveryTableBlockEntity deliveryTableBlockEntity
                && deliveryTableBlockEntity.getAgreement() == Agreement.EMPTY) {
            deliveryTableBlockEntity.setAgreementItem(stack.split(1));
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return super.onItemUseFirst(stack, context);
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
