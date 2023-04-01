package io.github.mortuusars.wares.data.generation;

import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.data.generation.provider.*;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = Wares.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGeneration
{
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper helper = event.getExistingFileHelper();
        if (event.includeServer()) {
//            generator.addProvider(new Advancements(generator, helper));
//            generator.addProvider(new LootTables(generator));
//            generator.addProvider(new Recipes(generator));
            BlockTags blockTags = new BlockTags(generator, helper);
            generator.addProvider(blockTags);
            generator.addProvider(new ItemTags(generator, blockTags, helper));
//            generator.addProvider(new BiomeTags(generator, helper));
        }
        if (event.includeClient()) {
            BlockStatesAndModels blockStates = new BlockStatesAndModels(generator, helper);
            generator.addProvider(blockStates);
            generator.addProvider(new ItemModels(generator, blockStates.models().existingFileHelper));
//            generator.addProvider(new Sounds(generator, helper));
            generator.addProvider(new Languages(generator, "en_us", Languages.MissingEntriesPolicy.EXCEPTION));
            generator.addProvider(new Languages(generator, "uk_ua", Languages.MissingEntriesPolicy.EXCEPTION));
        }
    }
}