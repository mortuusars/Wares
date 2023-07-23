package io.github.mortuusars.wares.data.generation.provider;

import io.github.mortuusars.wares.Wares;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeAdvancementProvider;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class Advancements extends ForgeAdvancementProvider
{
    public Advancements(DataGenerator dataGenerator, CompletableFuture<HolderLookup.Provider> provider, ExistingFileHelper existingFileHelper) {
        super(dataGenerator.getPackOutput(), provider, existingFileHelper, List.of(new WaresAdvancements()));
    }

    @SuppressWarnings("unused")
    public static class WaresAdvancements implements AdvancementGenerator {
        @Override
        public void generate(HolderLookup.Provider registries, Consumer<Advancement> saver, ExistingFileHelper existingFileHelper) {
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
                .save(saver, Wares.resource("adventure/at_the_last_minutes"), existingFileHelper);
        }
    }
}
