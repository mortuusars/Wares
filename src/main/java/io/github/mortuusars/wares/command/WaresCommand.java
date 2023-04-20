package io.github.mortuusars.wares.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import io.github.mortuusars.mpfui.helper.LoremIpsum;
import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.data.Lang;
import io.github.mortuusars.wares.data.agreement.*;
import io.github.mortuusars.wares.data.agreement.component.SteppedInt;
import io.github.mortuusars.wares.data.agreement.component.TextProvider;
import io.github.mortuusars.wares.data.agreement.component.WeightedComponent;
import io.github.mortuusars.wares.test.Tests;
import io.github.mortuusars.wares.test.framework.TestingResult;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class WaresCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("wares")
                        .requires((commandSourceStack -> commandSourceStack.hasPermission(2)))
                        .then(Commands.literal("give")
                                .then(Commands.literal("sealedAgreementExample").executes(WaresCommand::giveExampleSealedAgreement))
                                .then(Commands.literal("agreementExample").executes(WaresCommand::giveExampleAgreement)))
                        .then(Commands.literal("debug")
                                .then(Commands.literal("runTests").executes(WaresCommand::runTests))
                                .then(Commands.literal("completeAgreement").executes(WaresCommand::completeAgreement))));
    }

    private static int runTests(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            TestingResult testingResult = new Tests(player).run();

            MutableComponent message = Component.literal("Testing: ").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal("Total: " + testingResult.getTotalTestCount() + ".").withStyle(ChatFormatting.WHITE));

            if (testingResult.passed().size() > 0) {
                message.append(" ");
                message.append(Component.literal("Passed: " + testingResult.passed()
                        .size() + ".").withStyle(ChatFormatting.GREEN));
            }

            if (testingResult.failed().size() > 0) {
                message.append(" ");
                message.append(Component.literal("Failed: " + testingResult.failed()
                        .size() + ".").withStyle(ChatFormatting.RED));
            }

            if (testingResult.skipped().size() > 0) {
                message.append(" ");
                message.append(Component.literal("Skipped: " + testingResult.skipped()
                        .size() + ".").withStyle(ChatFormatting.GRAY));
            }

            if (testingResult.failed().size() == 0)
                context.getSource().sendSuccess(message, false);
            else
                context.getSource().sendFailure(message);

        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }

        return 0;
    }

    private static int giveExampleSealedAgreement(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();

        SealedAgreement sealedAgreement = new SealedAgreement("example_agreement_sealed",
                TextProvider.of(
                        WeightedComponent.of(Component.literal("Greg the Blacksmith").withStyle(ChatFormatting.DARK_GRAY)),
                        WeightedComponent.of(Component.literal("Arnold the Butcher").withStyle(ChatFormatting.DARK_RED))),
                TextProvider.of(Component.literal("59 Side Road, Vibrant Plains Village")),
                TextProvider.of(Component.literal("Example Agreement")),
                TextProvider.of(Component.literal(LoremIpsum.words(20))),
                "default",
                Component.literal("Wares Inc."),
                Component.literal(LoremIpsum.words(45)),
                Either.left(new ResourceLocation("minecraft:chests/village/village_butcher")),
                Either.left(new ResourceLocation("minecraft:chests/buried_treasure")),
                Either.right(new SteppedInt(12, 64, 8)),
                Either.right(new SteppedInt(10, 40, 10)),
                Either.left(100),
                Either.left(5 * 60));

        ItemStack sealedAgreementStack = new ItemStack(Wares.Items.SEALED_DELIVERY_AGREEMENT.get());
        sealedAgreement.toItemStack(sealedAgreementStack);

        if (serverPlayer.getMainHandItem().isEmpty())
            serverPlayer.setItemInHand(InteractionHand.MAIN_HAND, sealedAgreementStack);
        else if (!serverPlayer.addItem(sealedAgreementStack))
            serverPlayer.drop(sealedAgreementStack, false);

        return 0;
    }

    private static int giveExampleAgreement(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();

        Agreement agreement = Agreement.builder()
                .id("example_agreement")
                .buyerName(Component.literal("Greg the Blacksmith").withStyle(Style.EMPTY.withColor(0x333333)))
                .buyerAddress(Component.literal("12 Side Road, Vibrant Plains Village").withStyle(ChatFormatting.OBFUSCATED))
                .title(Component.literal("Example Agreement").withStyle(Style.EMPTY.withColor(0x922706)))
                .message(Component.literal(LoremIpsum.words(50)))
                .addRequestedItem(new ItemStack(Items.BAKED_POTATO, 4))
                .addRequestedItem(new ItemStack(Items.PUMPKIN_PIE, 2))
                .addPaymentItem(new ItemStack(Items.EMERALD, 2))
                .ordered(5)
                .experience(12)
                .expireTime(serverPlayer.level.getGameTime() + 20 * 60 * 15)
                .build();

        ItemStack agreementStack = new ItemStack(Wares.Items.DELIVERY_AGREEMENT.get());
        agreement.toItemStack(agreementStack);

        if (serverPlayer.getMainHandItem().isEmpty())
            serverPlayer.setItemInHand(InteractionHand.MAIN_HAND, agreementStack);
        else if (!serverPlayer.addItem(agreementStack))
            serverPlayer.drop(agreementStack, false);

        return 0;
    }

    private static int completeAgreement(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack mainHandItem = player.getMainHandItem();

        if (mainHandItem.is(Wares.Items.COMPLETED_DELIVERY_AGREEMENT.get())) {
            context.getSource().sendFailure(Lang.COMMAND_AGREEMENT_COMPLETE_ALREADY_COMPLETED.translate());
            return 1;
        }
        else if (mainHandItem.is(Wares.Items.EXPIRED_DELIVERY_AGREEMENT.get())) {
            context.getSource().sendFailure(Lang.COMMAND_AGREEMENT_COMPLETE_IS_EXPIRED.translate());
            return 1;
        }
        else if (!mainHandItem.is(Wares.Items.DELIVERY_AGREEMENT.get())) {
            context.getSource().sendFailure(Lang.COMMAND_AGREEMENT_COMPLETE_WRONG_ITEM
                    .translate(Wares.Items.DELIVERY_AGREEMENT.get(), mainHandItem.getItem()));
            return 1;
        }

        Agreement agreement = Agreement.fromItemStack(mainHandItem).orElse(Agreement.EMPTY);
        if (agreement == Agreement.EMPTY) {
            context.getSource().sendFailure(Lang.COMMAND_AGREEMENT_COMPLETE_IS_EMPTY.translate());
            return 1;
        }

        ItemStack completedStack = new ItemStack(Wares.Items.COMPLETED_DELIVERY_AGREEMENT.get());
        agreement.complete();
        agreement.toItemStack(completedStack);
        player.setItemInHand(InteractionHand.MAIN_HAND, completedStack);

        return 0;
    }
}
