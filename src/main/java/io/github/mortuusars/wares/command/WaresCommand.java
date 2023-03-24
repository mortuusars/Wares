package io.github.mortuusars.wares.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import io.github.mortuusars.mpfui.helper.LoremIpsum;
import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.data.agreement.*;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WaresCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("wares")
                        .requires((commandSourceStack -> commandSourceStack.hasPermission(2)))
                        .then(Commands.literal("sealedAgreement")
                                .then(Commands.literal("test").executes(WaresCommand::sealedAgreementTest))
                        )
                        .then(Commands.literal("agreement")
//                                .then(Commands.literal("complete").executes(WaresCommand::agreementComplete))
                                .then(Commands.literal("test").executes(WaresCommand::agreementTest))
                        ));
    }

    private static int sealedAgreementTest(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();

            ItemStack sealedAgreementStack = new ItemStack(Wares.Items.SEALED_AGREEMENT.get());

            List<ItemStack> items = new ArrayList<>();
            for (int i = 0; i < player.level.random.nextInt(4, 7); i++) {
                items.add(new ItemStack(net.minecraft.world.item.Items.BAKED_POTATO));
            }

            List<ItemStack> paymentItems = new ArrayList<>();
            for (int i = 0; i < player.level.random.nextInt(4, 7); i++) {
                paymentItems.add(new ItemStack(net.minecraft.world.item.Items.EMERALD));
            }

            AgreementDescription agreementDesctiption = new AgreementDescription(
                    Optional.of(TextProvider.of(
                            WeightedComponent.of(new TextComponent("Greg the Blacksmith").withStyle(ChatFormatting.DARK_GRAY), 2),
                            WeightedComponent.of(new TextComponent("Arnold the Butcher").withStyle(ChatFormatting.DARK_RED)))),
//                    Optional.empty(),
                    Optional.of(TextProvider.of(new TextComponent("12 Side Road, Vibrant Plains Village"))),
//                    Optional.empty(),
                    Optional.of(TextProvider.of(new TextComponent("Test Agreement"))),
//                    Optional.empty(),
                    Optional.of(TextProvider.of(new TextComponent(LoremIpsum.words(50)))),
//                    Optional.empty(),
                    Either.left(new ResourceLocation("minecraft:chests/village/village_butcher")),
                    Either.right(paymentItems),
                    Either.left(164),
                    Either.right(new SteppedInt(10, 40, 10)),
                    Either.left(100),
                    Either.left(5 * 60));

            agreementDesctiption.toItemStack(sealedAgreementStack);
            player.addItem(sealedAgreementStack);
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(new TextComponent(e.toString()));
            Wares.LOGGER.error("Executing command failed: " + e);
            return 1;
        }

        return 0;
    }

    private static int agreementTest(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();

            ItemStack agreementStack = new ItemStack(Wares.Items.DELIVERY_AGREEMENT.get());

            List<ItemStack> items = new ArrayList<>();
            for (int i = 0; i < player.level.random.nextInt(4, 7); i++) {
                items.add(new ItemStack(net.minecraft.world.item.Items.BAKED_POTATO));
            }

            List<ItemStack> paymentItems = new ArrayList<>();
            for (int i = 0; i < player.level.random.nextInt(4, 7); i++) {
                paymentItems.add(new ItemStack(net.minecraft.world.item.Items.EMERALD));
            }

            Agreement agreement = new Agreement(
                    Optional.of(new TextComponent("Greg the Blacksmith").withStyle(ChatFormatting.DARK_RED)),
//                    Optional.empty(),
                    Optional.of(new TextComponent("12 Side Road, Vibrant Plains Village")),
//                    Optional.empty(),
                    Optional.of(new TextComponent("Test Agreement").withStyle(ChatFormatting.GOLD)),
//                    Optional.empty(),
                    Optional.of(new TextComponent(LoremIpsum.words(50))),
//                    Optional.empty(),
                    items,
                    paymentItems,
                    356815,
                    5,
                    112121,
                    -1,
                    player.level.getGameTime() + 1728000); // 1 day

            agreement.toItemStack(agreementStack);
            player.addItem(agreementStack);
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(new TextComponent(e.toString()));
            Wares.LOGGER.error("Executing command failed: " + e);
            return 1;
        }
        return 0;
    }

    private static int agreementComplete(CommandContext<CommandSourceStack> context) {
        // TODO: Complete
        return 0;
    }
}
