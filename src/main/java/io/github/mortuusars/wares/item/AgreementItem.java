package io.github.mortuusars.wares.item;

import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.block.entity.DeliveryTableBlockEntity;
import io.github.mortuusars.wares.client.gui.screen.AgreementScreen;
import io.github.mortuusars.wares.client.gui.screen.DeliveryTableScreen;
import io.github.mortuusars.wares.data.LangKeys;
import io.github.mortuusars.wares.data.agreement.Agreement;
import io.github.mortuusars.wares.util.TextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class AgreementItem extends Item {
    public AgreementItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack agreementStack, ItemStack otherStack, Slot slot, ClickAction action, Player player, SlotAccess slotAccess) {
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

                AgreementScreen.showAsOverlay(player, agreementSupplier);
            }

            return true; // If true is returned - stack will not be picked up from a slot.
        }

        return super.overrideOtherStackedOnMe(agreementStack, otherStack, slot, action, player, slotAccess);
    }

    @Override
    public Component getName(ItemStack stack) {
        String id = this.getDescriptionId(stack);

        if (Agreement.fromItemStack(stack).orElse(Agreement.EMPTY).isCompleted())
            id = id + "_completed";

        return new TranslatableComponent(id);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        Optional<Agreement> agreementOptional = Agreement.fromItemStack(stack);

        agreementOptional.ifPresent(agreement -> {
            if (agreement.getRemaining() <= 0 && agreement.getOrdered() > 0)
                tooltipComponents.add(1, Wares.translate("COMPLETED"));
            else if (level != null && agreement.canExpire()) {
                MutableComponent expireTooltip = agreement.isExpired(level.getGameTime()) ?
                        Wares.translate(LangKeys.GUI_EXPIRED).withStyle(ChatFormatting.RED)
                        : Wares.translate(LangKeys.GUI_EXPIRES_IN)
                            .append(TextUtil.timeFromTicks(agreement.getExpireTime() - level.getGameTime()))
                        .withStyle(ChatFormatting.DARK_RED);

                tooltipComponents.add(1, expireTooltip);
            }

            if (Screen.hasShiftDown() && isAdvanced.isAdvanced())
                tooltipComponents.add(new TextComponent(agreement.toJsonString()).withStyle(ChatFormatting.GRAY));
        });
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
    public InteractionResult useOn(UseOnContext context) {
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
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack usedItemStack = player.getItemInHand(hand);

        Agreement agreement = Agreement.fromItemStack(usedItemStack).orElse(Agreement.EMPTY);

        if (agreement == Agreement.EMPTY){
            Wares.LOGGER.error("Cannot read Delivery Agreement from stack nbt OR Agreement is empty. No UI will be shown.");
            return InteractionResultHolder.pass(usedItemStack);
        }

        if (level.isClientSide)
            AgreementScreen.showAsOverlay(player, () -> agreement);

        return InteractionResultHolder.sidedSuccess(usedItemStack, level.isClientSide);
    }
}
