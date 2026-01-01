package io.github.mortuusars.wares.integration.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;

public class WaresKubeJSPlugin extends KubeJSPlugin {
    @Override
    public void registerEvents() {
        WaresJSEvents.register();
    }
}
