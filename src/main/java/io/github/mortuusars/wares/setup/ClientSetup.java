package io.github.mortuusars.wares.setup;

import io.github.mortuusars.wares.client.gui.screen.mailbox.MailboxScreen;
import io.github.mortuusars.wares.client.gui.screen.PaymentParcelScreen;
import io.github.mortuusars.wares.client.gui.screen.purchase_request.PurchaseRequestScreen;
import io.github.mortuusars.wares.client.gui.screen.shipping_crate.ShippingCrateScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {

    public static void setup(FMLClientSetupEvent event) {
        registerScreens();
    }

    private static void registerScreens(){
        MenuScreens.register(ModContainers.PURCHASE_REQUEST.get(), PurchaseRequestScreen::new);
        MenuScreens.register(ModContainers.MAILBOX.get(), MailboxScreen::new);
        MenuScreens.register(ModContainers.SHIPPING_CRATE.get(), ShippingCrateScreen::new);
        MenuScreens.register(ModContainers.PAYMENT_PARCEL.get(), PaymentParcelScreen::new);
    }
}
