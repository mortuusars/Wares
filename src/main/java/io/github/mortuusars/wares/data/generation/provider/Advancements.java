package io.github.mortuusars.wares.data.generation.provider;

import com.google.common.collect.Sets;
import io.github.mortuusars.wares.Wares;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Advancements extends AdvancementProvider
{
    public Advancements(DataGenerator dataGenerator, CompletableFuture<HolderLookup.Provider> provider, ExistingFileHelper existingFileHelper) {
        super(dataGenerator.getPackOutput(), provider, List.of(new WaresAdvancements(existingFileHelper)));
    }

    @SuppressWarnings("unused")
    public static class WaresAdvancements implements AdvancementSubProvider {
        private final ExistingFileHelper existingFileHelper;

        public WaresAdvancements(ExistingFileHelper existingFileHelper) {
            this.existingFileHelper = existingFileHelper;
        }

        @Override
        public void generate(HolderLookup.Provider pRegistries, Consumer<Advancement> consumer) {
            CompoundTag almostExpiredTag = new CompoundTag();
            almostExpiredTag.putBoolean("almostExpired", true);

            Advancement.Builder.advancement()
                .parent(new ResourceLocation("minecraft:adventure/root"))
                .display(Wares.Items.COMPLETED_DELIVERY_AGREEMENT.get(),
                        Component.translatable("advancement.wares.last_minutes.title"),
                        Component.translatable("advancement.wares.last_minutes.description"), null, FrameType.CHALLENGE,
                        true, true, true)
                .addCriterion("almost_expired", InventoryChangeTrigger.TriggerInstance.hasItems(
                        ItemPredicate.Builder.item()
                                .of(Wares.Items.COMPLETED_DELIVERY_AGREEMENT.get())
                                .hasNbt(almostExpiredTag)
                                .build()))
                .save(consumer, Wares.resource("adventure/at_the_last_minutes"), existingFileHelper);
    }

            Advancement.Builder.advancement()
                    .parent(new ResourceLocation("minecraft:adventure/root"))
                    .display(Wares.Items.COMPLETED_DELIVERY_AGREEMENT.get(),
                            Lang.ADVANCEMENT_LAST_MINUTES_TITLE.translate(),
                            Lang.ADVANCEMENT_LAST_MINUTES_DESCRIPTION.translate(), null, FrameType.CHALLENGE,
                            true, true, true)
                    .addCriterion("almost_expired", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ItemPredicate.Builder.item()
                                    .of(Wares.Items.COMPLETED_DELIVERY_AGREEMENT.get())
                                    .hasNbt(almostExpiredTag)
                                    .build()))
                    .save(consumer, Wares.resource("adventure/at_the_last_minutes"), existingFileHelper);
        }
    }
}
