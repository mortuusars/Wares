package io.github.mortuusars.wares.event;

import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.client.gui.screen.CardboardBoxScreen;
import io.github.mortuusars.wares.client.gui.screen.DeliveryTableScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Wares.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents {
    @SubscribeEvent
    public static void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(Wares.MenuTypes.DELIVERY_TABLE.get(), DeliveryTableScreen::new);
            MenuScreens.register(Wares.MenuTypes.CARDBOARD_BOX.get(), CardboardBoxScreen::new);
        });
    }

    @SubscribeEvent
    public static void onCreativeTabsBuild(CreativeModeTabEvent.BuildContents event) {
        if (event.getTab() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(Wares.Items.SEALED_DELIVERY_AGREEMENT.get());
            event.accept(Wares.Items.DELIVERY_AGREEMENT.get());
        }

        if (event.getTab() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(Wares.Items.DELIVERY_TABLE.get());
            event.accept(Wares.Items.CARDBOARD_BOX.get());
            event.accept(Wares.Items.PACKAGE.get());
        }
    }
}
