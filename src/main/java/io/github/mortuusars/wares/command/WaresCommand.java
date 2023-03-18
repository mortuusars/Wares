package io.github.mortuusars.wares.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.mortuusars.mpfui.helper.LoremIpsum;
import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.data.agreement.DeliveryAgreement;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
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
                        .then(Commands.literal("agreement")
//                                .then(Commands.literal("complete").executes(WaresCommand::agreementComplete))
                                .then(Commands.literal("test").executes(WaresCommand::agreementTest))
                        ));
    }

    private static int agreementComplete(CommandContext<CommandSourceStack> context) {
        // TODO: Complete
        return 0;
    }

    private static int agreementTest(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();

            ItemStack agreementStack = new ItemStack(Wares.Items.DELIVERY_AGREEMENT.get());

            List<ItemStack> items = new ArrayList<>();
            for (int i = 0; i < player.level.random.nextInt(1, 2); i++) {
                items.add(new ItemStack(net.minecraft.world.item.Items.BAKED_POTATO));
            }

            List<ItemStack> paymentItems = new ArrayList<>();
            for (int i = 0; i < player.level.random.nextInt(1, 2); i++) {
                paymentItems.add(new ItemStack(net.minecraft.world.item.Items.EMERALD));
            }

            DeliveryAgreement agreement = new DeliveryAgreement(
                    Optional.of(new TextComponent("Greg the Blacksmith").withStyle(ChatFormatting.DARK_RED)),
                    Optional.of(new TextComponent("12 Side Road, Vibrant Plains Village")),
                    Optional.of(new TextComponent("Test Agreement").withStyle(ChatFormatting.GOLD)),
                    Optional.of(new TextComponent(LoremIpsum.words(50))),
                    items,
                    paymentItems,
                    105,
                    105,
                    10,
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
}
