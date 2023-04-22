package io.github.mortuusars.wares.data.generation.provider;

import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.block.CardboardBoxBlock;
import io.github.mortuusars.wares.block.DeliveryTableBlock;
import io.github.mortuusars.wares.data.agreement.AgreementType;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockStatesAndModels extends BlockStateProvider {

    public BlockStatesAndModels(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen.getPackOutput(), Wares.ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        String table_path = Wares.Blocks.DELIVERY_TABLE.getId().getPath();

        getVariantBuilder(Wares.Blocks.DELIVERY_TABLE.get()).forAllStates(state -> {
            AgreementType agreement = state.getValue(DeliveryTableBlock.AGREEMENT);
            String agreement_suffix = agreement == AgreementType.NONE ? "" : "_agreement_" + agreement.getSerializedName();
            ModelFile.ExistingModelFile model = models().getExistingFile(
                    modLoc("block/" + table_path + agreement_suffix));
            return ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationY((int) state.getValue(DeliveryTableBlock.FACING).getOpposite().toYRot())
                    .build();
        });

        getVariantBuilder(Wares.Blocks.CARDBOARD_BOX.get()).forAllStates(state -> {
            Int2ObjectMap<String> PACKAGES = new Int2ObjectOpenHashMap<>(
                    new int[]{1, 2, 3, 4},
                    new String[]{"one", "two", "three", "four"});
            ModelFile.ExistingModelFile model = models().getExistingFile(
                    modLoc("block/" + Wares.Blocks.CARDBOARD_BOX.getId()
                            .getPath() + "_" + PACKAGES.get(state.getValue(CardboardBoxBlock.BOXES).intValue())));
            return ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationY(state.getValue(CardboardBoxBlock.AXIS) == Direction.Axis.X ? 0 : 90)
                    .build();
        });

        horizontalBlock(Wares.Blocks.PACKAGE.get(), models().getExistingFile(modLoc("block/" + Wares.Blocks.PACKAGE.getId().getPath())));
    }
}