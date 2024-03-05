package io.github.mortuusars.wares.item;

import com.mojang.datafixers.util.Either;
import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.client.gui.agreement.AgreementGUI;
import io.github.mortuusars.wares.data.agreement.DeliveryAgreement;
import io.github.mortuusars.wares.util.ClientHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class DeliveryAgreementItem extends Item {

    public DeliveryAgreementItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        getAgreementFromStack(stack).ifLeft(agreement ->
                tooltipComponents.add(Component.translatable("item.wares.agreement.view.tooltip").withStyle(Style.EMPTY.withColor(0xd6b589))));
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack agreementStack, @NotNull ItemStack otherStack, @NotNull Slot slot, @NotNull ClickAction action, @NotNull Player player, @NotNull SlotAccess slotAccess) {
        // This method is called only client-side when in creative inventory,

        if (agreementStack.getItem() == this && otherStack.isEmpty() && action == ClickAction.SECONDARY) {
            Optional<DeliveryAgreement> optionalAgreement = getAgreementFromStack(agreementStack).left();
            if (optionalAgreement.isPresent()) {
                DeliveryAgreement agreement = optionalAgreement.get();

                if (agreementStack.is(Wares.Items.DELIVERY_AGREEMENT.get())) {
                    if (agreement.isCompleted())
                        slot.set(convertToCompleted(agreementStack));
                    else if (agreement.isExpired(player.level.getGameTime()))
                        slot.set(convertToExpired(agreementStack));
                }

                if (player.getLevel().isClientSide) {
                    Supplier<DeliveryAgreement> agreementSupplier = ClientHelper.isViewingInDeliveryTableScreen(agreementStack) ?
                        ClientHelper.getDeliveryTableAgreementSupplier(agreement) :
                        () -> agreement;

                    AgreementGUI.showAsOverlay(player, agreementSupplier);
                }

                return true; // If true is returned - stack will not be picked up from a slot.
            }
        }

        return super.overrideOtherStackedOnMe(agreementStack, otherStack, slot, action, player, slotAccess);
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void inventoryTick(ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        if (stack.is(Wares.Items.DELIVERY_AGREEMENT.get()) && entity instanceof ServerPlayer serverPlayer && stack.hasTag()) {

            boolean isExpired = false;

            CompoundTag tag = stack.getTag();
            if (tag.contains("expireTime")) {
                long expireTime = tag.getLong("expireTime");
                if (expireTime >= 0 && expireTime <= level.getGameTime()) {
                    ItemStack expiredStack = convertToExpired(stack);
                    serverPlayer.getInventory().setItem(slotId, expiredStack);
                    isExpired = true;
                }
            }

            if (!isExpired && tag.contains("ordered") && tag.contains("remaining")) {
                int ordered = tag.getInt("ordered");
                int remaining = tag.getInt("remaining");
                if (ordered > 0 && remaining <= 0) {
                    ItemStack completedStack = convertToCompleted(stack);
                    serverPlayer.getInventory().setItem(slotId, completedStack);
                }
            }
        }
    }

    public static @NotNull ItemStack convertToExpired(ItemStack stack) {
        if (stack.isEmpty())
            throw new IllegalStateException("Tried to convert an empty ItemStack to Expired Delivery Agreement.");
        else if (stack.is(Wares.Items.COMPLETED_DELIVERY_AGREEMENT.get()))
            throw new IllegalStateException("Tried to convert Completed Delivery Agreement to Expired Delivery Agreement. Stack: '" + stack + '.');
        else if (stack.is(Wares.Items.EXPIRED_DELIVERY_AGREEMENT.get()))
            return stack;

        ItemStack expiredStack = new ItemStack(Wares.Items.EXPIRED_DELIVERY_AGREEMENT.get());
        @Nullable CompoundTag stackTag = stack.getTag();
        if (stackTag != null)
            expiredStack.setTag(stackTag);

        DeliveryAgreement agreement = DeliveryAgreement.fromItemStack(stack).orElse(DeliveryAgreement.EMPTY);
        if (agreement != DeliveryAgreement.EMPTY) {
            agreement.expire();
            agreement.toItemStack(expiredStack);
        }

        return expiredStack;
    }

    public static @NotNull ItemStack convertToCompleted(ItemStack stack) {
        if (stack.isEmpty())
            throw new IllegalStateException("Tried to convert an empty ItemStack to Completed Delivery Agreement.");
        else if (stack.is(Wares.Items.EXPIRED_DELIVERY_AGREEMENT.get()))
            throw new IllegalStateException("Tried to convert Expired Delivery Agreement to Completed Delivery Agreement. Stack: '" + stack + '.');
        else if (stack.is(Wares.Items.COMPLETED_DELIVERY_AGREEMENT.get()))
            return stack;

        ItemStack completedStack = new ItemStack(Wares.Items.COMPLETED_DELIVERY_AGREEMENT.get());
        @Nullable CompoundTag stackTag = stack.getTag();
        if (stackTag != null)
            completedStack.setTag(stackTag);

        DeliveryAgreement agreement = DeliveryAgreement.fromItemStack(stack).orElse(DeliveryAgreement.EMPTY);
        if (agreement != DeliveryAgreement.EMPTY) {
            agreement.complete();
            agreement.toItemStack(completedStack);
        }

        return completedStack;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack usedItemStack = player.getItemInHand(hand);
        if (level.isClientSide) {
            Either<DeliveryAgreement, AgreementError> agreementOrError = getAgreementFromStack(usedItemStack);

            agreementOrError.ifLeft(deliveryAgreement -> AgreementGUI.showAsOverlay(player, () -> deliveryAgreement))
                            .ifRight(error -> {
                                String userErrorKey;
                                String loggerError;

                                switch (error) {
                                    case NO_TAG -> {
                                        userErrorKey = "item.wares.delivery_agreement.no_data.message";
                                        loggerError = usedItemStack + " does not have any Agreement data. Delivery Agreements must be created with specific nbt tags to work." +
                                                " Refer to the mod's Wiki page for configuration instructions.";
                                    }
                                    case EMPTY -> {
                                        userErrorKey = "item.wares.delivery_agreement.empty.message";
                                        loggerError = "Cannot read Delivery Agreement from stack nbt OR Agreement is empty." +
                                                " There's probably something wrong with the agreement definition." +
                                                " Refer to the Wiki for instructions and double check that everything is entered and formatted correctly.";
                                    }
                                    default -> {
                                        userErrorKey = "item.wares.delivery_agreement.damaged.message";
                                        loggerError = "Cannot read Delivery Agreement from stack nbt OR Agreement is empty." +
                                                " There's probably something wrong with the agreement definition." +
                                                " Refer to the Wiki for instructions and double check that everything is entered and formatted correctly.";
                                    }
                                }

                                player.displayClientMessage(Component.translatable(userErrorKey).withStyle(ChatFormatting.RED), true);
                                Wares.LOGGER.warn(loggerError);
                                player.playSound(Wares.SoundEvents.PAPER_CRACKLE.get(), 0.8f, 0.65f);
                            });

        }
        return InteractionResultHolder.sidedSuccess(usedItemStack, level.isClientSide);
    }

    public Either<DeliveryAgreement, AgreementError> getAgreementFromStack(ItemStack stack) {
        if (stack.getTag() == null || stack.getTag().isEmpty())
            return Either.right(AgreementError.NO_TAG);

        Optional<DeliveryAgreement> optionalAgreement = DeliveryAgreement.fromItemStack(stack);
        if (optionalAgreement.isEmpty())
            return Either.right(AgreementError.DAMAGED);

        DeliveryAgreement agreement = optionalAgreement.get();
        if (agreement.isEmpty())
            return Either.right(AgreementError.EMPTY);

        return Either.left(agreement);
    }

    public enum AgreementError {
        NO_TAG,
        EMPTY,
        DAMAGED
    }
}
