package io.github.mortuusars.wares.item;

import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.client.gui.agreement.SealedAgreementScreen;
import io.github.mortuusars.wares.data.agreement.DeliveryAgreement;
import io.github.mortuusars.wares.data.agreement.SealedDeliveryAgreement;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("DataFlowIssue")
public class SealedDeliveryAgreementItem extends Item {
    public static final String DAMAGED_TAG = "AgreementDamaged";
    public static final String UNOPENABLE_TAG = "AgreementUnopenable";

    public SealedDeliveryAgreementItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        if (stack.getTag() == null || stack.getTag().isEmpty() || stack.getTag().contains(DAMAGED_TAG) || stack.getTag().contains(UNOPENABLE_TAG))
            return;

        SealedDeliveryAgreement.fromItemStack(stack).ifPresent(a ->
                tooltipComponents.add(Component.translatable("item.wares.sealed_agreement.view.tooltip")
                        .withStyle(Style.EMPTY.withColor(0xd6b589))));
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        String id = this.getDescriptionId(stack);

        if (stack.hasTag() && stack.getTag().contains(DAMAGED_TAG))
            id = id + "_damaged";

        return Component.translatable(id);
    }

    @Override
    public boolean overrideOtherStackedOnMe(@NotNull ItemStack agreementStack, @NotNull ItemStack otherStack, @NotNull Slot slot, @NotNull ClickAction action, @NotNull Player player, @NotNull SlotAccess slotAccess) {
        if (!otherStack.isEmpty() || action == ClickAction.PRIMARY)
            return false;

        return inspect(agreementStack, player);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isSecondaryUseActive())
            inspect(stack, player);
        else if (stack.getTag() == null || stack.getTag().isEmpty() || stack.getTag().contains(DAMAGED_TAG) || stack.getTag().contains(UNOPENABLE_TAG)) {
            if (level.isClientSide)
                player.displayClientMessage(Component.translatable("item.wares.sealed_delivery_agreement.damaged.message")
                        .withStyle(ChatFormatting.RED), true);
            Wares.LOGGER.error(stack + " does not have agreement data or data is not correct. Make sure item stack has agreement nbt and it is correct.");
            player.playSound(Wares.SoundEvents.PAPER_CRACKLE.get(), 0.8f, 0.65f);
        }
        else
            player.startUsingItem(hand);

        return InteractionResultHolder.success(stack);
    }

    public boolean inspect(ItemStack sealedAgreementStack, Player player) {
        Optional<SealedDeliveryAgreement> sealed = SealedDeliveryAgreement.fromItemStack(sealedAgreementStack);
        if (sealed.isEmpty())
            return false;

        if (player.level.isClientSide)
            new SealedAgreementScreen(sealed.get().seal(), sealed.get().sealTooltip(), sealed.get().backsideMessage()).showAsOverlay();

        return true;
    }

    @Override
    public @NotNull SoundEvent getEatingSound() {
        return Wares.SoundEvents.PAPER_CRACKLE.get();
    }

    @Override
    public int getUseDuration(@NotNull ItemStack pStack) {
        return 25;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack pStack) {
        return UseAnim.EAT;
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity livingEntity) {
        if (!(livingEntity instanceof Player player))
            return stack;

        Optional<SealedDeliveryAgreement> descriptionOptional = SealedDeliveryAgreement.fromItemStack(stack);

        if (descriptionOptional.isEmpty()){
            Wares.LOGGER.error("Cannot read AgreementDescription from stack nbt.");
            stack.getOrCreateTag().putBoolean(DAMAGED_TAG, true);

            if (level.isClientSide)
                player.displayClientMessage(Component.translatable("item.wares.sealed_delivery_agreement.damaged.message"), true);

            return stack;
        }

        if (level instanceof ServerLevel serverLevel) {
            try {
                DeliveryAgreement agreement = descriptionOptional.get().realize(serverLevel);

                ItemStack agreementStack = new ItemStack(Wares.Items.DELIVERY_AGREEMENT.get());
                if (agreement.toItemStack(agreementStack)) {
                    player.awardStat(Wares.Stats.SEALED_LETTERS_OPENED);
                    level.playSound(null,
                            player.position().x,
                            player.position().y,
                            player.position().z, Wares.SoundEvents.PAPER_TEAR.get(), SoundSource.PLAYERS,
                            1f, level.getRandom().nextFloat() * 0.1f + 0.95f);

                    // Release RMB after using. Otherwise, right click will be still held and will activate use again.
                    if (level.isClientSide)
                        Minecraft.getInstance().options.keyUse.setDown(false);

                    return agreementStack;
                }
                else
                    throw new IllegalStateException("Saving Agreement to ItemStack failed.");
            }
            catch (Exception e) {
                Wares.LOGGER.error(e.toString());
                player.displayClientMessage(Component.translatable("item.wares.sealed_delivery_agreement.unopenable.message"), true);
                stack.getOrCreateTag().putBoolean(UNOPENABLE_TAG, true);
            }
        }

        // Release RMB after using. Otherwise, right click will be still held and will activate use again.
        if (level.isClientSide)
            Minecraft.getInstance().options.keyUse.setDown(false);

        return stack;
    }
}
