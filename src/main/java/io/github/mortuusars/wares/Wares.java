package io.github.mortuusars.wares;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import io.github.mortuusars.wares.data.contract.Contract;
import io.github.mortuusars.wares.item.ContractItem;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Wares.ID)
public class Wares
{
    public static final String ID = "wares";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Wares()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        Items.ITEMS.register(modEventBus);

        MinecraftForge.EVENT_BUS.addListener(Wares::onRightClick);
    }

    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getPlayer();
        if (!player.isSecondaryUseActive() || player.level.isClientSide)
            return;

    }

    public static class Blocks {
        private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ID);



    }

    public static class Items {
        private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ID);

        public static final RegistryObject<ContractItem> CONTRACT = ITEMS.register("contract", () ->
                new ContractItem(new Item.Properties()
                        .tab(CreativeModeTab.TAB_MISC)
                        .stacksTo(1)));
    }
}
