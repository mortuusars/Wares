package io.github.mortuusars.wares.item;

import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.data.Lang;
import io.github.mortuusars.wares.data.agreement.Agreement;
import io.github.mortuusars.wares.data.agreement.AgreementDescription;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@SuppressWarnings("DataFlowIssue")
public class SealedAgreementItem extends Item {

    public static final String DAMAGED_TAG = "AgreementDamaged";
    public static final String UNOPENABLE_TAG = "AgreementUnopenable";

    public SealedAgreementItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        String id = this.getDescriptionId(stack);

        if (stack.hasTag() && stack.getTag().contains(DAMAGED_TAG))
            id = id + "_damaged";

        return new TranslatableComponent(id);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public SoundEvent getEatingSound() {
        return Wares.SoundEvents.PAPER_TEAR.get();
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return 25;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.EAT;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!(livingEntity instanceof Player player))
            return stack;

        Optional<AgreementDescription> descriptionOptional = AgreementDescription.fromItemStack(stack);

        if (descriptionOptional.isEmpty()){
            Wares.LOGGER.error("Cannot read AgreementDescription from stack nbt.");
            stack.getOrCreateTag().putBoolean(DAMAGED_TAG, true);

            if (level.isClientSide)
                player.displayClientMessage(Lang.SEALED_AGREEMENT_DAMAGED_ERROR_MESSAGE.translate(), true);

            return stack;
        }

        if (level instanceof ServerLevel serverLevel) {
            try {
                Agreement agreement = descriptionOptional.get().realize(serverLevel);

                ItemStack agreementStack = new ItemStack(Wares.Items.DELIVERY_AGREEMENT.get());
                if (agreement.toItemStack(agreementStack)) {
                    stack.shrink(1);
                    player.addItem(agreementStack);
                    player.getCooldowns().addCooldown(Wares.Items.DELIVERY_AGREEMENT.get(), 12);
                    player.awardStat(Wares.Stats.SEALED_LETTERS_OPENED);
                    level.playSound(null,
                            player.position().x,
                            player.position().y,
                            player.position().z, Wares.SoundEvents.PAPER_TEAR.get(), SoundSource.PLAYERS,
                            1f, level.getRandom().nextFloat() * 0.1f + 0.95f);
                }
                else
                    throw new IllegalStateException("Saving Agreement to ItemStack failed.");
            }
            catch (Exception e) {
                Wares.LOGGER.error(e.toString());
                player.displayClientMessage(Lang.SEALED_AGREEMENT_UNOPENABLE_ERROR_MESSAGE.translate(), true);
                stack.getOrCreateTag().putBoolean(UNOPENABLE_TAG, true);
            }
        }

        return stack;
    }
}
