package io.github.mortuusars.wares.item;

import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.client.gui.tooltip.AgreementTooltip;
import io.github.mortuusars.wares.data.LangKeys;
import io.github.mortuusars.wares.data.agreement.DeliveryAgreement;
import io.github.mortuusars.wares.menu.AgreementMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class AgreementItem extends Item {
    public AgreementItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        Optional<DeliveryAgreement> agreementOptional = DeliveryAgreement.fromItemStack(stack);

        agreementOptional.ifPresent(agreement -> {
            if (agreement.getRemaining() <= 0 && agreement.getOrdered() > 0)
                tooltipComponents.add(1, Wares.translate("COMPLETED"));
            else if (level != null && agreement.hasExpirationTime()) {
                MutableComponent expireTooltip = agreement.isExpired(level.getGameTime()) ?
                        Wares.translate(LangKeys.GUI_EXPIRED).withStyle(ChatFormatting.RED)
                        : Wares.translate(LangKeys.GUI_EXPIRES_IN)
                            .append(AgreementTooltip.timeFromTicks(agreement.getExpireTime() - level.getGameTime()))
                        .withStyle(ChatFormatting.DARK_RED);

                tooltipComponents.add(1, expireTooltip);
            }

            if (Screen.hasShiftDown() && isAdvanced.isAdvanced())
                tooltipComponents.add(new TextComponent(agreement.toJsonString()).withStyle(ChatFormatting.GRAY));
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);

        if (!heldStack.is(Wares.Tags.Items.AGREEMENTS))
            return InteractionResultHolder.pass(heldStack);

        Optional<DeliveryAgreement> agreementOptional = DeliveryAgreement.fromItemStack(heldStack);

        if (agreementOptional.isEmpty()){
            Wares.LOGGER.error("Cannot read Delivery Agreement from stack nbt. No UI will be shown.");
            return InteractionResultHolder.pass(heldStack);
        }

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            DeliveryAgreement deliveryAgreement = agreementOptional.get();
            NetworkHooks.openGui(serverPlayer,
                    new SimpleMenuProvider((id, playerInventory, playerArg) ->
                            new AgreementMenu(id, playerInventory, deliveryAgreement), new TranslatableComponent("ASD")),
                    buf -> buf.writeItem(heldStack));
        } else if (level.isClientSide){
            level.playSound(player, player.position().x, player.position().y, player.position().z, SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1f, level.getRandom().nextFloat() * 0.2f + 0.9f );
        }

        return InteractionResultHolder.sidedSuccess(heldStack, level.isClientSide);
    }
}
