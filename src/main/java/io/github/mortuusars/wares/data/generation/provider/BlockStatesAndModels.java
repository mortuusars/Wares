package io.github.mortuusars.wares.data.generation.provider;

import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.block.DeliveryTableBlock;
import io.github.mortuusars.wares.data.agreement.AgreementStatus;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.HashMap;
import java.util.Map;

public class BlockStatesAndModels extends BlockStateProvider {

    public BlockStatesAndModels(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, Wares.ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        getVariantBuilder(Wares.Blocks.DELIVERY_TABLE.get()).forAllStatesExcept(state -> {
            ModelFile.ExistingModelFile model = models().getExistingFile(
                    modLoc("block/delivery_table_agreement_" + state.getValue(DeliveryTableBlock.AGREEMENT).getSerializedName()));
            return ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationY((int) state.getValue(DeliveryTableBlock.FACING).getOpposite().toYRot())
                    .build();
        });
    }
}