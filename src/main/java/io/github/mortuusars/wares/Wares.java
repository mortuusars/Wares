package io.github.mortuusars.wares;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.wares.block.CardboardBoxBlock;
import io.github.mortuusars.wares.block.DeliveryTableBlock;
import io.github.mortuusars.wares.block.PackageBlock;
import io.github.mortuusars.wares.block.entity.DeliveryTableBlockEntity;
import io.github.mortuusars.wares.block.entity.PackageBlockEntity;
import io.github.mortuusars.wares.config.Config;
import io.github.mortuusars.wares.item.DeliveryAgreementItem;
import io.github.mortuusars.wares.item.CardboardBoxItem;
import io.github.mortuusars.wares.item.PackageItem;
import io.github.mortuusars.wares.item.SealedDeliveryAgreementItem;
import io.github.mortuusars.wares.menu.CardboardBoxMenu;
import io.github.mortuusars.wares.menu.DeliveryTableMenu;
import io.github.mortuusars.wares.world.VillageStructures;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.StatFormatter;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.common.util.ForgeSoundType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Mod(Wares.ID)
public class Wares
{
    public static final String ID = "wares";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Wares()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        Config.init();
        modEventBus.addListener(Config::onConfigLoad);
        modEventBus.addListener(Config::onConfigReload);

        Blocks.BLOCKS.register(modEventBus);
        BlockEntities.BLOCK_ENTITIES.register(modEventBus);
        MenuTypes.MENU_TYPES.register(modEventBus);
        Items.ITEMS.register(modEventBus);
        Villagers.POI_TYPES.register(modEventBus);
        Villagers.PROFESSIONS.register(modEventBus);
        SoundEvents.SOUNDS.register(modEventBus);

        MinecraftForge.EVENT_BUS.addListener(VillageStructures::addVillageStructures);
    }

    /**
     * Creates resource location in the mod namespace with the given path.
     */
    public static ResourceLocation resource(String path) {
        return new ResourceLocation(ID, path);
    }


    public static class Blocks {
        private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ID);

        public static final RegistryObject<DeliveryTableBlock> DELIVERY_TABLE = BLOCKS.register("delivery_table",
                () -> new DeliveryTableBlock(BlockBehaviour.Properties.of()
                        .sound(SoundType.WOOD)
                        .mapColor(MapColor.COLOR_BROWN)
                        .strength(2f)));

        public static final RegistryObject<CardboardBoxBlock> CARDBOARD_BOX = BLOCKS.register("cardboard_box",
                () -> new CardboardBoxBlock(BlockBehaviour.Properties.of()
                        .sound(SoundTypes.CARDBOARD)
                        .mapColor(MapColor.COLOR_BROWN)
                        .strength(0.4f)));

        public static final RegistryObject<PackageBlock> PACKAGE = BLOCKS.register("package",
                () -> new PackageBlock(BlockBehaviour.Properties.of()
                        .sound(SoundTypes.CARDBOARD)
                        .mapColor(MapColor.COLOR_BROWN)
                        .strength(0.6f)));
    }

    @SuppressWarnings("DataFlowIssue")
    public static class BlockEntities {
        private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ID);

        @SuppressWarnings("DataFlowIssue")
        public static final RegistryObject<BlockEntityType<DeliveryTableBlockEntity>> DELIVERY_TABLE =
                BLOCK_ENTITIES.register("delivery_table",
                        () -> BlockEntityType.Builder.of(DeliveryTableBlockEntity::new, Blocks.DELIVERY_TABLE.get()).build(null));

        public static final RegistryObject<BlockEntityType<PackageBlockEntity>> PACKAGE =
                BLOCK_ENTITIES.register("package",
                        () -> BlockEntityType.Builder.of(PackageBlockEntity::new, Blocks.PACKAGE.get()).build(null));
    }

    public static class MenuTypes {
        private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Wares.ID);

        public static final RegistryObject<MenuType<DeliveryTableMenu>> DELIVERY_TABLE = MENU_TYPES
                .register("delivery_table", () -> IForgeMenuType.create(DeliveryTableMenu::fromBuffer));

        public static final RegistryObject<MenuType<CardboardBoxMenu>> CARDBOARD_BOX = MENU_TYPES
                .register("cardboard_box", () -> IForgeMenuType.create(CardboardBoxMenu::fromBuffer));
    }

    public static class Items {
        private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ID);

        public static final RegistryObject<SealedDeliveryAgreementItem> SEALED_DELIVERY_AGREEMENT = ITEMS.register("sealed_delivery_agreement", () ->
                new SealedDeliveryAgreementItem(new Item.Properties()
                        .stacksTo(1)));
        public static final RegistryObject<DeliveryAgreementItem> DELIVERY_AGREEMENT = ITEMS.register("delivery_agreement", () ->
                new DeliveryAgreementItem(new Item.Properties()
                        .stacksTo(1)));
        public static final RegistryObject<DeliveryAgreementItem> COMPLETED_DELIVERY_AGREEMENT = ITEMS.register("completed_delivery_agreement", () ->
                new DeliveryAgreementItem(new Item.Properties()
                        .stacksTo(1)));
        public static final RegistryObject<DeliveryAgreementItem> EXPIRED_DELIVERY_AGREEMENT = ITEMS.register("expired_delivery_agreement", () ->
                new DeliveryAgreementItem(new Item.Properties()
                        .stacksTo(1)));

        public static final RegistryObject<BlockItem> DELIVERY_TABLE = ITEMS.register("delivery_table", () ->
                new BlockItem(Blocks.DELIVERY_TABLE.get(), new Item.Properties()));

        public static final RegistryObject<CardboardBoxItem> CARDBOARD_BOX = ITEMS.register("cardboard_box", () ->
                new CardboardBoxItem(Blocks.CARDBOARD_BOX.get(), new Item.Properties()));
        public static final RegistryObject<PackageItem> PACKAGE = ITEMS.register("package", () ->
                new PackageItem(Blocks.PACKAGE.get(), new Item.Properties()
                        .stacksTo(1)));
    }

    public static class Villagers {
        public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(ForgeRegistries.POI_TYPES, Wares.ID);
        public static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(ForgeRegistries.VILLAGER_PROFESSIONS, Wares.ID);


        public static final RegistryObject<PoiType> DELIVERY_TABLE_POI = POI_TYPES.register(Blocks.DELIVERY_TABLE.getId().getPath(),
                () -> new PoiType(ImmutableSet.copyOf(Blocks.DELIVERY_TABLE.get().getStateDefinition().getPossibleStates()), 1, 1));

        public static final RegistryObject<VillagerProfession> PACKAGER = PROFESSIONS.register("packager",
                () -> new VillagerProfession("packager", poi -> poi.is(Objects.requireNonNull(DELIVERY_TABLE_POI.getKey())), poi -> poi.is(Objects.requireNonNull(DELIVERY_TABLE_POI.getKey())),
                        ImmutableSet.of(), ImmutableSet.of(), null));
    }

    public static class SoundEvents {
        private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Wares.ID);

        public static final RegistryObject<SoundEvent> PAPER_TEAR = registerSound("item", "paper.tear");
        public static final RegistryObject<SoundEvent> PAPER_CRACKLE = registerSound("item", "paper.crackle");


        public static final RegistryObject<SoundEvent> CARDBOARD_PLACE = registerSound("block", "cardboard.place");
        public static final RegistryObject<SoundEvent> CARDBOARD_BREAK = registerSound("block", "cardboard.break");
        public static final RegistryObject<SoundEvent> CARDBOARD_HIT = registerSound("block", "cardboard.hit");
        public static final RegistryObject<SoundEvent> CARDBOARD_FALL = registerSound("block", "cardboard.fall");
        public static final RegistryObject<SoundEvent> CARDBOARD_STEP = registerSound("block", "cardboard.step");

        public static final RegistryObject<SoundEvent> WRITING = registerSound("block", "delivery_table.writing");
        public static final RegistryObject<SoundEvent> DELIVERY_TABLE_OPEN = registerSound("block", "delivery_table.open");
        public static final RegistryObject<SoundEvent> DELIVERY_TABLE_CLOSE = registerSound("block", "delivery_table.close");
        public static final RegistryObject<SoundEvent> CARDBOARD_BOX_USE = registerSound("block", "cardboard_box.use");

        public static final RegistryObject<SoundEvent> VILLAGER_WORK_PACKAGER = registerSound("entity", "villager.work_packager");

        private static RegistryObject<SoundEvent> registerSound(String category, String key) {
            Preconditions.checkState(category != null && category.length() > 0, "'category' should not be empty.");
            Preconditions.checkState(key != null && key.length() > 0, "'key' should not be empty.");
            String path = category + "." + key;
            return SOUNDS.register(path, () -> SoundEvent.createVariableRangeEvent(Wares.resource(path)));
        }
    }

    public static class SoundTypes {
        public static final SoundType CARDBOARD = new ForgeSoundType(1f, 1f, SoundEvents.CARDBOARD_BREAK, SoundEvents.CARDBOARD_STEP, SoundEvents.CARDBOARD_PLACE, SoundEvents.CARDBOARD_HIT, SoundEvents.CARDBOARD_FALL);
    }

    public static class Stats {
        private static final Map<ResourceLocation, StatFormatter> STATS = new HashMap<>();

        public static final ResourceLocation SEALED_LETTERS_OPENED =
                register(Wares.resource("sealed_letters_opened"), StatFormatter.DEFAULT);
        public static final ResourceLocation PACKAGES_OPENED =
                register(Wares.resource("packages_opened"), StatFormatter.DEFAULT);
        public static final ResourceLocation INTERACT_WITH_DELIVERY_TABLE =
                register(Wares.resource("interact_with_delivery_table"), StatFormatter.DEFAULT);

        @SuppressWarnings("SameParameterValue")
        private static ResourceLocation register(ResourceLocation location, StatFormatter formatter) {
            STATS.put(location, formatter);
            return location;
        }

        public static void register() {
            STATS.forEach((location, formatter) -> {
                Registry.register(BuiltInRegistries.CUSTOM_STAT, location, location);
                net.minecraft.stats.Stats.CUSTOM.get(location, formatter);
            });
        }
    }

    public static class Tags {
        public static class Items {
            public static final TagKey<Item> AGREEMENTS = ItemTags.create(Wares.resource("agreements"));
            public static final TagKey<Item> DELIVERY_BOXES = ItemTags.create(Wares.resource("delivery_boxes"));
            public static final TagKey<Item> CARDBOARD_BOX_BLACKLISTED = ItemTags.create(Wares.resource("cardboard_box_blacklisted"));
        }
    }
}
