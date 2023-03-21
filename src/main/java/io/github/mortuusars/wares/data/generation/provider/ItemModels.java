package io.github.mortuusars.wares.data.generation.provider;


import io.github.mortuusars.wares.Wares;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ItemModels extends ItemModelProvider {
    public ItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, Wares.ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        blockItem(Wares.Items.DELIVERY_TABLE.get());
        singleTextureItem(Wares.Items.SEALED_AGREEMENT.get());
        singleTextureItem(Wares.Items.DELIVERY_AGREEMENT.get());
        singleTextureItem(Wares.Items.DELIVERY_NOTE.get());
    }

    private ItemModelBuilder blockItem(BlockItem item) {
        return withExistingParent(item.getRegistryName().getPath(), modLoc("block/" + item.getBlock().getRegistryName().getPath()));
    }

    private ItemModelBuilder singleTextureItem(Item item) {
        return singleTexture(item.getRegistryName().getPath(),
                mcLoc("item/generated"), "layer0",
                modLoc("item/" + item.getRegistryName().getPath()));
    }
}
