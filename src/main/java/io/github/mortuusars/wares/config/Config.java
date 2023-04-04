package io.github.mortuusars.wares.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class Config {
    private static final ForgeConfigSpec COMMON;

    public static final ForgeConfigSpec.BooleanValue DELIVERIES_REQUIRE_PACKAGES;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        DELIVERIES_REQUIRE_PACKAGES = builder
                .comment("Each delivery requires (and consumes) a Delivery Package. A slot for Delivery Packages will be added to delivery table.")
                .define("DeliveriesRequirePackages", true);

        COMMON = builder.build();
    }

    public static void init() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON);
    }
}
