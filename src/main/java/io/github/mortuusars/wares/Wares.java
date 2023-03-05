package io.github.mortuusars.wares;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.wares.block.DeliveryTableBlock;
import io.github.mortuusars.wares.block.entity.DeliveryTableBlockEntity;
import io.github.mortuusars.wares.data.bill.Bill;
import io.github.mortuusars.wares.item.BillItem;
import io.github.mortuusars.wares.menu.DeliveryTableMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
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

        ItemStack billStack = new ItemStack(Items.BILL.get());

        Bill bill = new Bill(
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                List.of(new ItemStack(net.minecraft.world.item.Items.STONE, 4)), List.of(new ItemStack(net.minecraft.world.item.Items.EMERALD)), -1, -1, -1);

        try {
            Tag tag = Bill.CODEC.encodeStart(NbtOps.INSTANCE, bill).getOrThrow(false, s -> {});
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.put("Bill", tag);
            billStack.setTag(compoundTag);

            player.addItem(billStack);
        }
        catch (Throwable i) {
            boolean t = true;
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
        return new TranslatableComponent(ID + "." + key, args);
    }

    public static class Blocks {
        private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ID);

        public static final RegistryObject<DeliveryTableBlock> DELIVERY_TABLE = BLOCKS.register("delivery_table",
                () -> new DeliveryTableBlock(BlockBehaviour.Properties.of(Material.WOOD).strength(2f)));
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

        public static final RegistryObject<BillItem> BILL = ITEMS.register("bill", () ->
                new BillItem(new Item.Properties()
                        .tab(CreativeModeTab.TAB_MISC)
                        .stacksTo(1)));

        public static final RegistryObject<BlockItem> DELIVERY_TABLE = ITEMS.register("delivery_table", () ->
                new BlockItem(Blocks.DELIVERY_TABLE.get(), new Item.Properties()
                        .tab(CreativeModeTab.TAB_MISC)));
    }

    public static class Tags {
        public static class Items {
            public static final TagKey<Item> BILLS = ItemTags.create(
                    Wares.resource("bills"));
        }
    }
}
