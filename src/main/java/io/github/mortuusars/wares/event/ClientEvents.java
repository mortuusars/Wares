package io.github.mortuusars.wares.event;

import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.client.gui.screen.AgreementScreen;
import io.github.mortuusars.wares.client.gui.screen.DeliveryTableScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Wares.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents {

    @SubscribeEvent
    public static void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(Wares.MenuTypes.DELIVERY_TABLE.get(), DeliveryTableScreen::new);
            MenuScreens.register(Wares.MenuTypes.AGREEMENT.get(), AgreementScreen::new);
        });
    }
}
