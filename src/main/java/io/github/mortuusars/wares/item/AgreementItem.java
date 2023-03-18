package io.github.mortuusars.wares.item;

import com.mojang.datafixers.util.Either;
import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.block.entity.DeliveryTableBlockEntity;
import io.github.mortuusars.wares.client.gui.screen.AgreementScreen;
import io.github.mortuusars.wares.client.gui.screen.DeliveryTableScreen;
import io.github.mortuusars.wares.client.gui.tooltip.AgreementTooltip;
import io.github.mortuusars.wares.data.LangKeys;
import io.github.mortuusars.wares.data.agreement.DeliveryAgreement;
import io.github.mortuusars.wares.menu.AgreementMenu;
import io.github.mortuusars.wares.menu.DeliveryTableMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
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

public class AgreementItem extends Item {
    public AgreementItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack agreementStack, ItemStack otherStack, Slot slot, ClickAction action, Player player, SlotAccess pAccess) {
        // This method is called only client-side when in player inventory,
        // but both client- and server-side when in other containers.

        if (agreementStack.getItem() == this && otherStack.isEmpty() && action == ClickAction.SECONDARY) {
            Optional<DeliveryAgreement> agreementOptional = DeliveryAgreement.fromItemStack(agreementStack);
            if (agreementOptional.isEmpty()){
                Wares.LOGGER.error("Cannot read Delivery Agreement from stack nbt. No UI will be shown.");
                return false;
            }

            if (player instanceof LocalPlayer) {
                Either<DeliveryTableBlockEntity, ItemStack> source;

                if (Minecraft.getInstance().screen instanceof DeliveryTableScreen deliveryTableScreen) {
                    DeliveryTableMenu menu = deliveryTableScreen.getMenu();
                    source = Either.left(menu.blockEntity);
                }
                else
                    source = Either.right(agreementStack);


                return openClientAgreementGui(source, player);
            }
            else
                return true;

            // If true is returned - stack will not be picked up from a slot:
//            return player instanceof LocalPlayer ? openClientAgreementGui(Either.right(agreementStack), player) : true;
        }

        return super.overrideOtherStackedOnMe(agreementStack, otherStack, slot, action, player, pAccess);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        Optional<DeliveryAgreement> agreementOptional = DeliveryAgreement.fromItemStack(stack);

        agreementOptional.ifPresent(agreement -> {
            if (agreement.getRemaining() <= 0 && agreement.getOrdered() > 0)
                tooltipComponents.add(1, Wares.translate("COMPLETED"));
            else if (level != null && agreement.canExpire()) {
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
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();

        if (stack.is(this) && clickedFace == Direction.UP
                && level.getBlockEntity(clickedPos) instanceof DeliveryTableBlockEntity deliveryTableBlockEntity
                && deliveryTableBlockEntity.getAgreement() == DeliveryAgreement.EMPTY) {
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
                && deliveryTableBlockEntity.getAgreement() == DeliveryAgreement.EMPTY) {
            deliveryTableBlockEntity.setItem(DeliveryTableBlockEntity.AGREEMENT_SLOT, stack.split(1));
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack agreementStack = player.getItemInHand(hand);

        if (agreementStack.getItem() != this)
            return InteractionResultHolder.pass(agreementStack);

        Optional<DeliveryAgreement> agreementOptional = DeliveryAgreement.fromItemStack(agreementStack);

        if (agreementOptional.isEmpty()){
            Wares.LOGGER.error("Cannot read Delivery Agreement from stack nbt. No UI will be shown.");
            return InteractionResultHolder.pass(agreementStack);
        }

        if (level.isClientSide)
            openClientAgreementGui(Either.right(agreementStack), player);
        else
            level.playSound(player,
                    player.position().x,
                    player.position().y,
                    player.position().z, SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1f, level.getRandom()
                            .nextFloat() * 0.2f + 0.9f);

        return InteractionResultHolder.sidedSuccess(agreementStack, level.isClientSide);
    }

    public boolean openClientAgreementGui(Either<DeliveryTableBlockEntity, ItemStack> source, Player player) {
        if (!player.level.isClientSide)
            throw new IllegalStateException("Tried to open Agreement gui on the server. Not gonna happen.");

        int containerId = 1;
        AbstractContainerMenu containerMenu = Minecraft.getInstance().player.containerMenu;
        if (containerMenu != null)
            containerId = containerMenu.containerId + 1;

        Component title = source.map(
                deliveryEntity -> deliveryEntity.getItem(DeliveryTableBlockEntity.AGREEMENT_SLOT).getHoverName(),
                stack -> stack.getHoverName());

        Inventory playerInventory = player.getInventory();
        AgreementScreen screen = new AgreementScreen(new AgreementMenu(containerId, playerInventory, source),
                playerInventory, title);

        screen.show();
        return true;
    }

//    public static boolean openAgreementGui(ServerPlayer serverPlayer, ItemStack agreementStack) {
//        if (!(agreementStack.getItem() instanceof AgreementItem))
//            throw new IllegalArgumentException("Attempted to open Agreement gui with wrong item: '" + agreementStack + "'.");
//
//        Optional<DeliveryAgreement> agreementOptional = DeliveryAgreement.fromItemStack(agreementStack);
//
//        if (agreementOptional.isEmpty()){
//            Wares.LOGGER.error("Cannot read Delivery Agreement from stack nbt. No UI will be shown.");
//            return false;
//        }
//
//        NetworkHooks.openGui(serverPlayer,
//                new SimpleMenuProvider((id, playerInventory, playerArg) ->
//                        new AgreementMenu(id, playerInventory, agreementOptional.get()), new TranslatableComponent("ASD")),
//                buf -> buf.writeItem(agreementStack));
//
//
//        return true;
//    }
}
