package io.github.mortuusars.wares;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import io.github.mortuusars.wares.block.DeliveryTableBlock;
import io.github.mortuusars.wares.block.entity.DeliveryTableBlockEntity;
import io.github.mortuusars.wares.data.agreement.AgreementDescription;
import io.github.mortuusars.wares.data.agreement.SteppedInt;
import io.github.mortuusars.wares.data.agreement.TextProvider;
import io.github.mortuusars.wares.data.agreement.WeightedComponent;
import io.github.mortuusars.wares.item.AgreementItem;
import io.github.mortuusars.wares.menu.DeliveryTableMenu;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Wares.ID)
public class Wares
{
    public static final String ID = "wares";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Wares()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        Blocks.BLOCKS.register(modEventBus);
        BlockEntities.BLOCK_ENTITIES.register(modEventBus);
        MenuTypes.MENU_TYPES.register(modEventBus);
        Items.ITEMS.register(modEventBus);

        MinecraftForge.EVENT_BUS.addListener(Wares::onRightClick);
    }

    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getPlayer();
        if (!player.isSecondaryUseActive() || player.level.isClientSide || event.getHand() == InteractionHand.OFF_HAND)
            return;

        testDesc();
    }

    public static void testDesc() {
        AgreementDescription description = new AgreementDescription(
                Optional.of(TextProvider.of(List.of(WeightedComponent.of(new TextComponent("Simple Buyer"), 45)))),
                Optional.of(TextProvider.of(List.of(WeightedComponent.of(new TextComponent("Simple Address")), WeightedComponent.of(new TextComponent("Simple 2"))))),
                Optional.of(TextProvider.of(Wares.translate("title.translate.key"))),
                Optional.of(TextProvider.of(new TextComponent("message here"))),
                Either.left("asd"),
                Either.left("asd"),
                Either.left(50),
                Either.right(new SteppedInt(1, 10)),
                Either.left(50),
                Either.left(20000));

        try {
            DataResult<JsonElement> jsonElementDataResult = AgreementDescription.CODEC.encodeStart(JsonOps.INSTANCE, description);
            JsonElement element = jsonElementDataResult.getOrThrow(false, s -> Wares.LOGGER.error(s));
            String s = element.toString();
            Wares.LOGGER.info(s);

            AgreementDescription orThrow = AgreementDescription.CODEC.parse(JsonOps.INSTANCE, GsonHelper.parse(s))
                    .getOrThrow(false, st -> Wares.LOGGER.error(st));
            Wares.LOGGER.info(s);


        }
        catch (Exception e) {
            Wares.LOGGER.error(e.toString());
        }
    }

    /**
     * Creates resource location in the mod namespace with the given path.
     */
    public static ResourceLocation resource(String path) {
        return new ResourceLocation(ID, path);
    }

    /**
     * Creates TranslatableComponent from a given key prefixed with the MOD ID.
     */
    public static MutableComponent translate(String key, Object... args) {
        return new TranslatableComponent(key, args);
    }

    public static class Blocks {
        private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ID);

        public static final RegistryObject<DeliveryTableBlock> DELIVERY_TABLE = BLOCKS.register("delivery_table",
                () -> new DeliveryTableBlock(BlockBehaviour.Properties.of(Material.WOOD).strength(2f)));

//        public static final RegistryObject<AgreementBlock> AGREEMENT = BLOCKS.register("agreement",
//                () -> new AgreementBlock(BlockBehaviour.Properties.of(Material.DECORATION).strength(0.3f)));
    }

    public static class BlockEntities {
        private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, ID);

        public static final RegistryObject<BlockEntityType<DeliveryTableBlockEntity>> DELIVERY_TABLE =
                BLOCK_ENTITIES.register("delivery_table",
                        () -> BlockEntityType.Builder.of(DeliveryTableBlockEntity::new, Blocks.DELIVERY_TABLE.get()).build(null));
    }

    public static class MenuTypes {
        private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.CONTAINERS, Wares.ID);

        public static final RegistryObject<MenuType<DeliveryTableMenu>> DELIVERY_TABLE = MENU_TYPES
                .register("delivery_table", () -> IForgeMenuType.create(DeliveryTableMenu::fromBuffer));
    }

    public static class Items {
        private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ID);

        public static final RegistryObject<AgreementItem> DELIVERY_AGREEMENT = ITEMS.register("delivery_agreement", () ->
                new AgreementItem(new Item.Properties()
                        .tab(CreativeModeTab.TAB_MISC)
                        .stacksTo(1)));
        public static final RegistryObject<AgreementItem> DELIVERY_NOTE = ITEMS.register("delivery_note", () ->
                new AgreementItem(new Item.Properties()
                        .tab(CreativeModeTab.TAB_MISC)
                        .stacksTo(1)));

        public static final RegistryObject<BlockItem> DELIVERY_TABLE = ITEMS.register("delivery_table", () ->
                new BlockItem(Blocks.DELIVERY_TABLE.get(), new Item.Properties()
                        .tab(CreativeModeTab.TAB_MISC)));
    }

    public static class Tags {
        public static class Items {
            public static final TagKey<Item> AGREEMENTS = ItemTags.create(
                    Wares.resource("agreements"));
        }
    }
}
