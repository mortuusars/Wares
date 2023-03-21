package io.github.mortuusars.wares.item;

import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.data.LangKeys;
import io.github.mortuusars.wares.data.agreement.Agreement;
import io.github.mortuusars.wares.data.agreement.AgreementDescription;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class SealedAgreementItem extends Item {

    public static final String DAMAGED_TAG = "AgreementDamaged";

    public SealedAgreementItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        String id = this.getDescriptionId(stack);

        if (stack.hasTag() && stack.getTag().contains(DAMAGED_TAG))
            id = id + "_damaged";

        return new TranslatableComponent(id);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack usedItemStack = player.getItemInHand(hand);

        Optional<AgreementDescription> descriptionOptional = AgreementDescription.fromItemStack(usedItemStack);

        if (descriptionOptional.isEmpty()){
            Wares.LOGGER.error("Cannot read AgreementDescription from stack nbt.");
            if (usedItemStack.hasTag()) {
                CompoundTag tag = usedItemStack.getTag();
                tag.putBoolean(DAMAGED_TAG, true);
            }

            if (level.isClientSide)
                player.displayClientMessage(Wares.translate(LangKeys.SEALED_AGREEMENT_DAMAGED_MESSAGE), true);

            return InteractionResultHolder.pass(usedItemStack);
        }

        if (level instanceof ServerLevel serverLevel) {
            try {
                Agreement agreement = descriptionOptional.get().realize(serverLevel);

                ItemStack agreementStack = new ItemStack(Wares.Items.DELIVERY_AGREEMENT.get());
                if (agreement.toItemStack(agreementStack)) {
                    player.setItemInHand(hand, agreementStack);
                    level.playSound(null,
                            player.position().x,
                            player.position().y,
                            player.position().z,
                            SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS,
                            1f, level.getRandom().nextFloat() * 0.2f + 1.1f);
                }
                else
                    throw new IllegalStateException("Saving Agreement to ItemStack failed.");
            }
            catch (Exception e) {
                Wares.LOGGER.error(e.toString());
                player.displayClientMessage(Wares.translate(LangKeys.SEALED_AGREEMENT_UNOPENABLE_MESSAGE), true);
            }
        }

        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
}
