package io.github.mortuusars.wares.config;

import com.google.common.base.Preconditions;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.List;

public class Config {
    private static final ForgeConfigSpec COMMON;
    private static final ForgeConfigSpec CLIENT;

    // COMMON
    public static final ForgeConfigSpec.BooleanValue PACKAGER_REQUIRED;
    public static final ForgeConfigSpec.BooleanValue PACKAGER_SHOULD_BE_WORKING;
    public static final ForgeConfigSpec.BooleanValue MANUAL_DELIVERY_ALLOWED;
    public static final ForgeConfigSpec.DoubleValue MANUAL_DELIVERY_TIME_MODIFIER;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> BATCH_SIZE_PER_LEVEL;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> PACKAGER_XP_PER_LEVEL;
    public static final ForgeConfigSpec.IntValue DEFAULT_DELIVERY_TIME;
    public static final ForgeConfigSpec.BooleanValue DELIVERIES_REQUIRE_BOXES;

    public static final ForgeConfigSpec.BooleanValue GENERATE_WAREHOUSES;
    public static final ForgeConfigSpec.IntValue WAREHOUSE_WEIGHT;

    // CLIENT
    public static final ForgeConfigSpec.BooleanValue AGREEMENT_CLOSE_WITH_RMB;
    public static final ForgeConfigSpec.BooleanValue AGREEMENT_APPEND_BUYER_INFO_TO_MESSAGE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("Delivery");

        builder.push("Packager");

        PACKAGER_REQUIRED = builder
                .comment("Packager worker is required for Delivery Table to work.",
                        "For a Packager to be considered a 'worker' he should have current table as a 'job_site' and have 'last_worked_at_poi' less than 40 seconds ago.",
                        "Default: true")
                .define("PackagerRequiredForDelivery", true);

        PACKAGER_SHOULD_BE_WORKING = builder
                .comment("Packager should have 'last_worked_at_poi' less than 40 seconds ago to be considered a worker.",
                        "Requires 'PackagerRequiredForDelivery'. Default: true")
                .define("PackagerShouldHaveWorkedRecently", true);

        BATCH_SIZE_PER_LEVEL = builder
                .comment("Number of packages that Packager can pack for one delivery based on Packager's level.",
                        "Should have 5 values corresponding to each villager level. Default: [1,2,4,6,8]")
                .defineList("PackagerBatchSizePerLevel", List.of(1, 2, 4, 6, 8), value -> ((int) value) > 0);

        PACKAGER_XP_PER_LEVEL = builder
                .comment("How many xp points Packager levels require. Each delivered package counts as 1xp. ",
                        "This works the same as with regular villagers. ",
                        "Villager XP points do not reset to 0 on level up - so each consecutive value should be larger than previous.",
                        "Default: [0, 30, 90, 200, 400]")
                .defineList("PackagerXpPerLevel", List.of(0, 40, 110, 250, 500), value -> ((int) value) >= 0);

        MANUAL_DELIVERY_ALLOWED = builder
                .comment("Players can manually deliver a package if Packager is not currently working at table.",
                        "Requires 'PackagerRequiredForDelivery'. Default: true")
                .define("PlayerCanDeliverManually", true);

        MANUAL_DELIVERY_TIME_MODIFIER = builder
                .comment("Time modifier when delivering manually.",
                        "Default: 3x")
                .defineInRange("ManualDeliveryTimeModifier", 3.0D, 1.0D, 999.0D);

        builder.pop();

        DEFAULT_DELIVERY_TIME = builder
                .comment("Time in ticks that deliveries take. Agreement can override this value. Default: 200 ticks (10 seconds)")
                .defineInRange("DefaultDeliveryTime", 200, 1, Integer.MAX_VALUE);

        DELIVERIES_REQUIRE_BOXES = builder
                .comment("Each delivery requires (and consumes) a packaging. ('wares:delivery_boxes' tag)",
                        "A slot for Delivery Packages will be added to delivery table. Default: true")
                .define("DeliveriesRequirePackaging", true);

        builder.pop();

        builder.push("Structures");

        GENERATE_WAREHOUSES = builder
                .comment("Warehouses will generate in villages.")
                .define("GenerateWarehouses", true);

        WAREHOUSE_WEIGHT = builder
                .comment("Warehouse structure weight. Larger number = more chances to spawn.")
                .defineInRange("WarehouseWeight", 10, 1, Integer.MAX_VALUE);

        builder.pop();

        COMMON = builder.build();


        builder = new ForgeConfigSpec.Builder();

        builder.push("AgreementGUI");

        AGREEMENT_CLOSE_WITH_RMB = builder
                .comment("Delivery Agreement and Sealed Agreement view screens will close on Right Click.")
                .define("AgreementScreenCloseWithRMB", true);
        AGREEMENT_APPEND_BUYER_INFO_TO_MESSAGE = builder
                .comment("Buyer Name and Buyer Address will be appended (if provided) to the end of the message in the Delivery Agreement View Screen.",
                        "(Buyer info can also be seen by hovering over the Wax Seal)")
                .define("AgreementScreenAppendBuyerInfo", true);

        builder.pop();

        CLIENT = builder.build();
    }

    public static void onConfigReload(final ModConfigEvent.Reloading ignoredEvent) {
        validateConfig();
    }

    public static void onConfigLoad(final ModConfigEvent.Loading ignoredEvent) {
        validateConfig();
    }

    public static void validateConfig() {
        Preconditions.checkState(BATCH_SIZE_PER_LEVEL.get().size() == 5, "PackagerBatchSizePerLevel should have 5 values.");
        List<? extends Integer> levelThresholds = PACKAGER_XP_PER_LEVEL.get();
        Preconditions.checkState(levelThresholds.size() == 5, "PackagerXpPerLevel should have 5 values");
        int prevXp = -1;
        for (int xp : levelThresholds) {
            if (xp <= prevXp)
                throw new IllegalStateException("Wares: Invalid configuration of PackagerXpPerLevel:\nNext value should be larger than previous value. [XPValue: %s]".formatted(xp));

            prevXp = xp;
        }
    }

    public static int getBatchSizeForLevel(final int level) {
        return BATCH_SIZE_PER_LEVEL.get().get(level - 1);
    }

    public static int getMaxXpPerLevel(final int level) {
        return PACKAGER_XP_PER_LEVEL.get().get(level);
    }

    public static void init() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT);
    }
}
