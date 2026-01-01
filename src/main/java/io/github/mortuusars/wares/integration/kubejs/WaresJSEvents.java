package io.github.mortuusars.wares.integration.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import io.github.mortuusars.wares.integration.kubejs.event.DeliveryEventJS;

public interface WaresJSEvents {
    EventGroup GROUP = EventGroup.of("WaresEvents");

    EventHandler BATCH_DELIVERED = GROUP.server("batchDelivered", () -> DeliveryEventJS.class);
    EventHandler AGREEMENT_COMPLETED = GROUP.server("agreementCompleted", () -> DeliveryEventJS.class);
    EventHandler AGREEMENT_EXPIRED = GROUP.server("agreementExpired", () -> DeliveryEventJS.class);

    static void register() {
        GROUP.register();
    }
}
