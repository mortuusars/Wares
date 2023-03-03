package io.github.mortuusars.wares.data.provider;

import com.google.common.collect.Sets;
import com.google.gson.GsonBuilder;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Consumer;

public class Advancements extends AdvancementProvider {
    private final Path PATH;
    private ExistingFileHelper existingFileHelper;
    public static final Logger LOGGER = LogManager.getLogger();

    public Advancements(DataGenerator dataGenerator, ExistingFileHelper existingFileHelper) {
        super(dataGenerator);
        PATH = dataGenerator.getOutputFolder();
        this.existingFileHelper = existingFileHelper;
    }

    @Override
    public void run(HashCache cache) {
        Set<ResourceLocation> set = Sets.newHashSet();
        Consumer<Advancement> consumer = (advancement) -> {
            if (!set.add(advancement.getId())) {
                throw new IllegalStateException("Duplicate advancement " + advancement.getId());
            } else {
                Path path1 = getPath(PATH, advancement);

                try {
                    DataProvider.save((new GsonBuilder()).setPrettyPrinting().create(), cache, advancement.deconstruct().serializeToJson(), path1);
                }
                catch (IOException ioexception) {
                    LOGGER.error("Couldn't save advancement {}", path1, ioexception);
                }
            }
        };

        new SaltAdvancements(existingFileHelper).accept(consumer);
    }

    private static Path getPath(Path pathIn, Advancement advancementIn) {
        return pathIn.resolve("data/" + advancementIn.getId().getNamespace() + "/advancements/" + advancementIn.getId().getPath() + ".json");
    }

    public static class SaltAdvancements implements Consumer<Consumer<Advancement>> {
        private ExistingFileHelper existingFileHelper;

        public SaltAdvancements(ExistingFileHelper existingFileHelper) {
            this.existingFileHelper = existingFileHelper;
        }

        @Override
        public void accept(Consumer<Advancement> advancementConsumer) {
//            Advancement taste_explosion = Advancement.Builder.advancement()
//                    .parent(new ResourceLocation("minecraft:husbandry/root"))
//                    .display(new ItemStack(Salt.Items.SALT.get()),
//                            Salt.translate(LangKeys.ADVANCEMENT_TASTE_EXPLOSION_TITLE),
//                            Salt.translate(LangKeys.ADVANCEMENT_TASTE_EXPLOSION_DESCRIPTION),
//                            null, FrameType.TASK, true, false, false)
//                    .addCriterion("eat_salted_food", new SaltedFoodConsumedTrigger.TriggerInstance(EntityPredicate.Composite.ANY))
//                    .save(advancementConsumer, Salt.resource("adventure/taste_explosion"), existingFileHelper);
        }
    }
}
