package io.github.mortuusars.wares.data.generation.provider;

import io.github.mortuusars.wares.Wares;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class ItemTags extends ItemTagsProvider {
    public ItemTags(DataGenerator generator, BlockTagsProvider blockTagsProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator, blockTagsProvider, Wares.ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        tag(Wares.Tags.Items.AGREEMENTS)
                .add(Wares.Items.DELIVERY_AGREEMENT.get())
                .add(Wares.Items.COMPLETED_DELIVERY_AGREEMENT.get())
                .add(Wares.Items.EXPIRED_DELIVERY_AGREEMENT.get());
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