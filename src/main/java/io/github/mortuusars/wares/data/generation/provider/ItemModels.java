package io.github.mortuusars.wares.data.generation.provider;


import io.github.mortuusars.wares.Wares;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

@SuppressWarnings({"DataFlowIssue", "UnusedReturnValue"})
public class ItemModels extends ItemModelProvider {
    public ItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator.getPackOutput(), Wares.ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        blockItem(Wares.Items.DELIVERY_TABLE.get());
        singleTextureItem(Wares.Items.CARDBOARD_BOX.get());
        singleTextureItem(Wares.Items.PACKAGE.get());
        singleTextureItem(Wares.Items.SEALED_DELIVERY_AGREEMENT.get());
        singleTextureItem(Wares.Items.DELIVERY_AGREEMENT.get());
        singleTextureItem(Wares.Items.COMPLETED_DELIVERY_AGREEMENT.get());
        singleTextureItem(Wares.Items.EXPIRED_DELIVERY_AGREEMENT.get());
    }

    private ItemModelBuilder blockItem(BlockItem item) {
        return withExistingParent(ForgeRegistries.ITEMS.getKey(item).getPath(), modLoc("block/" + ForgeRegistries.ITEMS.getKey(item).getPath()));
    }

    private ItemModelBuilder singleTextureItem(Item item) {
        return singleTexture(ForgeRegistries.ITEMS.getKey(item).getPath(),
                mcLoc("item/generated"), "layer0",
                modLoc("item/" + ForgeRegistries.ITEMS.getKey(item).getPath()));
    }
}
