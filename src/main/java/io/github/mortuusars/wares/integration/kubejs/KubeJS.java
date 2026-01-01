package io.github.mortuusars.wares.integration.kubejs;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import io.github.mortuusars.wares.block.entity.DeliveryTableBlockEntity;
import io.github.mortuusars.wares.integration.kubejs.event.DeliveryEventJS;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;

public class KubeJS {
    private static final Supplier<Boolean> isLoaded = Suppliers.memoize(() -> ModList.get().isLoaded("kubejs"));

    public static boolean isLoaded() {
        return isLoaded.get();
    }

    public static void batchDelivered(DeliveryTableBlockEntity blockEntity, @Nullable ServerPlayer player) {
        if (isLoaded()) {
            Events.batchDelivered(blockEntity, player);
        }
    }

    public static void agreementCompleted(DeliveryTableBlockEntity blockEntity, @Nullable ServerPlayer player) {
        if (isLoaded()) {
            Events.agreementCompleted(blockEntity, player);
        }
    }

    public static void agreementExpired(DeliveryTableBlockEntity blockEntity, @Nullable ServerPlayer player) {
        if (isLoaded()) {
            Events.agreementExpired(blockEntity, player);
        }
    }

    /**
     * Indirect calls is to avoid crashing the game due to loading of non-existing classes (if KubeJS is not present).
     */
    private static class Events {
        public static void batchDelivered(DeliveryTableBlockEntity blockEntity, @Nullable ServerPlayer player) {
            WaresJSEvents.BATCH_DELIVERED.post(new DeliveryEventJS(blockEntity, player));
        }

        public static void agreementCompleted(DeliveryTableBlockEntity blockEntity, @Nullable ServerPlayer player) {
            WaresJSEvents.AGREEMENT_COMPLETED.post(new DeliveryEventJS(blockEntity, player));
        }

        public static void agreementExpired(DeliveryTableBlockEntity blockEntity, @Nullable ServerPlayer player) {
            WaresJSEvents.AGREEMENT_EXPIRED.post(new DeliveryEventJS(blockEntity, player));
        }
    }
}
