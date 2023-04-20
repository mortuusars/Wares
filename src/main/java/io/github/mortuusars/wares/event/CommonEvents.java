package io.github.mortuusars.wares.event;

import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.command.WaresCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class CommonEvents {
    @net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = Wares.ID, bus = net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD)
    public static class Mod {
        @SubscribeEvent
        public static void commonSetup(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> {
                Wares.Stats.register();
            });
        }
    }

    @net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = Wares.ID, bus = net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.FORGE)
    public static class Forge {
        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent event) {
            WaresCommand.register(event.getDispatcher());
        }
    }
}
