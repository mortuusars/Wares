package io.github.mortuusars.wares.data.providers;

import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.common.mailbox.MailboxBlock;
import io.github.mortuusars.wares.setup.ModBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

import java.util.Objects;

public class ModBlockStatesProvider extends BlockStateProvider {
    public ModBlockStatesProvider(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, Wares.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {

        horizontalBlock(ModBlocks.MAILBOX.get(), state -> switch (state.getValue(MailboxBlock.FILL_LEVEL)) {
            case 1 -> models().getExistingFile(modLoc("block/mailbox_low"));
            case 2 -> models().getExistingFile(modLoc("block/mailbox_medium"));
            case 3 -> models().getExistingFile(modLoc("block/mailbox_full"));
            default -> models().getExistingFile(modLoc("block/mailbox_empty"));
        });

        crate();

        BlockModelBuilder shippingCrateModel = models()
                .withExistingParent(blockPath(ModBlocks.SHIPPING_CRATE), modLoc("block/" + blockPath(ModBlocks.CRATE)))
                .texture("up", modLoc("block/" + blockPath(ModBlocks.SHIPPING_CRATE)));
        directionalBlock(ModBlocks.SHIPPING_CRATE.get(), (blockState -> blockState.getValue(BarrelBlock.OPEN) ?
                models().withExistingParent(blockPath(ModBlocks.SHIPPING_CRATE) + "_opened", modLoc("block/" + blockPath(ModBlocks.CRATE) + "_opened")) :
                shippingCrateModel));

        simpleBlock(ModBlocks.DELIVERY_NOTE.get(), models().getExistingFile(modLoc("block/" + blockPath(ModBlocks.DELIVERY_NOTE))));
        simpleBlock(ModBlocks.PAYMENT_PARCEL.get(), models().getExistingFile(modLoc("block/" + blockPath(ModBlocks.PAYMENT_PARCEL))));
    }

    private void crate() {
        ResourceLocation top = modLoc("block/crate");
        ResourceLocation side = modLoc("block/crate_side");
        ResourceLocation topOpened = modLoc("block/crate_opened");

        BlockModelBuilder crateModel = models()
                .cubeTop(blockPath(ModBlocks.CRATE), side, top)
                .texture("particle", top);

        BlockModelBuilder crateOpenedModel = models()
                .withExistingParent(blockPath(ModBlocks.CRATE) + "_opened", modLoc(blockPath(ModBlocks.CRATE)))
                .texture("up", topOpened);

        directionalBlock(ModBlocks.CRATE.get(), (blockState -> blockState.getValue(BarrelBlock.OPEN) ? crateOpenedModel : crateModel));
    }

    private String blockPath(RegistryObject<? extends Block> registryObject){
        return Objects.requireNonNull(registryObject.get().getRegistryName()).getPath();
    }
}
