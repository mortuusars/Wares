package io.github.mortuusars.wares.data.provider;

import io.github.mortuusars.wares.Wares;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockStatesAndModels extends BlockStateProvider {

    public BlockStatesAndModels(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, Wares.ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
    }
}