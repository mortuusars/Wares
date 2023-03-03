package io.github.mortuusars.wares.data.generation.provider;

import io.github.mortuusars.wares.Wares;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class BlockTags extends BlockTagsProvider {
    public BlockTags(DataGenerator generator, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator, Wares.ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
//        tag(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE)
//                .add(Salt.Blocks.ROCK_SALT_ORE.get());
    }
}
