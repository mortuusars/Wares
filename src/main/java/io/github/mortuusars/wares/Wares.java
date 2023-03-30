package io.github.mortuusars.wares;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.wares.block.DeliveryTableBlock;
import io.github.mortuusars.wares.block.entity.DeliveryTableBlockEntity;
import io.github.mortuusars.wares.item.AgreementItem;
import io.github.mortuusars.wares.item.SealedAgreementItem;
import io.github.mortuusars.wares.menu.DeliveryTableMenu;
import io.github.mortuusars.wares.test.Tests;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
                () -> new DeliveryTableBlock(BlockBehaviour.Properties.of(Material.WOOD).strength(2f)));
    }

    public static class BlockEntities {
        private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, ID);

        @SuppressWarnings("DataFlowIssue")
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

        public static final RegistryObject<SealedAgreementItem> SEALED_DELIVERY_AGREEMENT = ITEMS.register("sealed_delivery_agreement", () ->
                new SealedAgreementItem(new Item.Properties()
                        .tab(CreativeModeTab.TAB_MISC)
                        .stacksTo(1)));
        public static final RegistryObject<AgreementItem> DELIVERY_AGREEMENT = ITEMS.register("delivery_agreement", () ->
                new AgreementItem(new Item.Properties()
                        .tab(CreativeModeTab.TAB_MISC)
                        .stacksTo(1)));
        public static final RegistryObject<AgreementItem> COMPLETED_DELIVERY_AGREEMENT = ITEMS.register("completed_delivery_agreement", () ->
                new AgreementItem(new Item.Properties()
                        .tab(CreativeModeTab.TAB_MISC)
                        .stacksTo(1)));

        public static final RegistryObject<AgreementItem> EXPIRED_DELIVERY_AGREEMENT = ITEMS.register("expired_delivery_agreement", () ->
                new AgreementItem(new Item.Properties()
                        .tab(CreativeModeTab.TAB_MISC)
                        .stacksTo(1)));

        public static final RegistryObject<BlockItem> DELIVERY_TABLE = ITEMS.register("delivery_table", () ->
                new BlockItem(Blocks.DELIVERY_TABLE.get(), new Item.Properties()
                        .tab(CreativeModeTab.TAB_DECORATIONS)));
    }

    public static class Tags {
        public static class Items {
            public static final TagKey<Item> AGREEMENTS = ItemTags.create(Wares.resource("agreements"));
        }
    }
}
