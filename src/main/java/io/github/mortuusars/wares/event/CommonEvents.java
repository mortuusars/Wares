package io.github.mortuusars.wares.event;

import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.block.PackageDispenseBehavior;
import io.github.mortuusars.wares.command.WaresCommand;
import io.github.mortuusars.wares.config.Config;
import io.github.mortuusars.wares.data.agreement.SealedDeliveryAgreement;
import io.github.mortuusars.wares.data.agreement.component.SteppedInt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.common.BasicItemListing;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class CommonEvents {
    @Mod.EventBusSubscriber(modid = Wares.ID, bus = net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBus {
        @SubscribeEvent
        public static void commonSetup(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> {
                Wares.AdvancementTriggers.register();
                Wares.Stats.register();
                DispenserBlock.registerBehavior(Wares.Items.PACKAGE.get(), new PackageDispenseBehavior());
            });
        }
    }

    @Mod.EventBusSubscriber(modid = Wares.ID, bus = net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeBus {
        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent event) {
            WaresCommand.register(event.getDispatcher());
        }

        @SubscribeEvent
        public static void addWanderingTrades(WandererTradesEvent event) {
            if (!Config.WANDERING_TRADER_AGREEMENTS.get())
                return;

            ItemStack regularSealedAgreement = new ItemStack(Wares.Items.SEALED_DELIVERY_AGREEMENT.get());
            new SealedDeliveryAgreement.Builder()
                    .requested(Wares.resource("agreement/wandering_trader/regular_price"))
                    .payment(Wares.resource("agreement/wandering_trader/regular_ware"))
                    .ordered(new SteppedInt(16, 84, 4))
                    .experience(new SteppedInt(16, 64, 4))
                    .id("wandering_trader_agreement")
                    .build()
                    .toItemStack(regularSealedAgreement);

            event.getGenericTrades().add(new BasicItemListing(6, regularSealedAgreement, 1, 6));

            ItemStack rareSealedAgreement = new ItemStack(Wares.Items.SEALED_DELIVERY_AGREEMENT.get());
            new SealedDeliveryAgreement.Builder()
                    .requested(Wares.resource("agreement/wandering_trader/rare_price"))
                    .payment(Wares.resource("agreement/wandering_trader/rare_ware"))
                    .ordered(new SteppedInt(6, 48, 4))
                    .experience(new SteppedInt(32, 96, 4))
                    .id("wandering_trader_agreement")
                    .build()
                    .toItemStack(rareSealedAgreement);

            event.getRareTrades().add(new BasicItemListing(12, rareSealedAgreement, 1, 12));
        }
    }
}
