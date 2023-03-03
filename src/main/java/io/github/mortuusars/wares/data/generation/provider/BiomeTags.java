package io.github.mortuusars.wares.data.generation.provider;

import io.github.mortuusars.wares.Wares;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class BiomeTags extends BiomeTagsProvider {
    public BiomeTags(DataGenerator generator, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator, Wares.ID, existingFileHelper);
    }

    @Override
    protected void addTags() {

    }
}
