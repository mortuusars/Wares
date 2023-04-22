package io.github.mortuusars.wares.data.generation.provider;

import io.github.mortuusars.wares.Wares;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class ItemTags extends ItemTagsProvider {
    public ItemTags(DataGenerator generator, CompletableFuture<HolderLookup.Provider> pLookupProvider, BlockTagsProvider blockTagsProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator.getPackOutput(), pLookupProvider, blockTagsProvider, Wares.ID, existingFileHelper);
    }


    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        tag(Wares.Tags.Items.AGREEMENTS)
                .add(Wares.Items.SEALED_DELIVERY_AGREEMENT.get(),
                     Wares.Items.DELIVERY_AGREEMENT.get(),
                     Wares.Items.COMPLETED_DELIVERY_AGREEMENT.get(),
                     Wares.Items.EXPIRED_DELIVERY_AGREEMENT.get());

        tag(Wares.Tags.Items.DELIVERY_BOXES)
                .add(Wares.Items.CARDBOARD_BOX.get());

        tag(Wares.Tags.Items.CARDBOARD_BOX_BLACKLISTED)
                .add(Wares.Items.PACKAGE.get(),
                     Wares.Items.CARDBOARD_BOX.get());
    }

    private void optionalTags(TagAppender<Item> tag, String namespace, String... items) {
        for (String item : items) {
            tag.addOptionalTag(new ResourceLocation(namespace, item));
        }
    }

    private void optional(TagAppender<Item> tag, String namespace, String... items) {
        for (String item : items) {
            tag.addOptional(new ResourceLocation(namespace, item));
        }
    }
}