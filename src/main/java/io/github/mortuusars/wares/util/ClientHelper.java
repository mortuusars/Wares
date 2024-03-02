package io.github.mortuusars.wares.util;

import io.github.mortuusars.wares.client.gui.screen.DeliveryTableScreen;
import io.github.mortuusars.wares.data.agreement.DeliveryAgreement;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class ClientHelper {
    public static void releaseUseKey() {
        Minecraft.getInstance().options.keyUse.setDown(false);
    }

    public static boolean isViewingInDeliveryTableScreen(ItemStack agreementStack) {
        return Minecraft.getInstance().screen instanceof DeliveryTableScreen deliveryTableScreen && ItemStack.isSameItemSameTags(deliveryTableScreen.getMenu().blockEntity.getAgreementItem(), agreementStack);
    }

    public static Supplier<DeliveryAgreement> getDeliveryTableAgreementSupplier() {
        return () -> {
            assert Minecraft.getInstance().screen != null;
            return ((DeliveryTableScreen) Minecraft.getInstance().screen).getMenu().blockEntity.getAgreement();
        };
    }
}
