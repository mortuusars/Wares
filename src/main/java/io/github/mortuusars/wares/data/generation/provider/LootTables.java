package io.github.mortuusars.wares.data.generation.provider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.block.DeliveryPackageBlock;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

@SuppressWarnings({"unused", "CommentedOutCode"})
public class LootTables extends LootTableProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final DataGenerator generator;

    public LootTables(DataGenerator generator) {
        super(generator);
        this.generator = generator;
    }

    @Override
    public void run(@NotNull HashCache cache) {
        dropsSelf(cache, Wares.Items.DELIVERY_TABLE.get());

        // Package

        DeliveryPackageBlock packageBlock = Wares.Blocks.DELIVERY_PACKAGE.get();
        writeTable(cache, Wares.resource("blocks/" + Wares.Blocks.DELIVERY_PACKAGE.getId().getPath()), LootTable.lootTable()
                .setParamSet(LootContextParamSets.BLOCK)
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(packageBlock)
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F))
                                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(packageBlock)
                                                .setProperties(StatePropertiesPredicate.Builder.properties()
                                                        .hasProperty(DeliveryPackageBlock.PACKAGES, 2))))
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(3.0F))
                                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(packageBlock)
                                                .setProperties(StatePropertiesPredicate.Builder.properties()
                                                        .hasProperty(DeliveryPackageBlock.PACKAGES, 3))))
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(4.0F))
                                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(packageBlock)
                                                .setProperties(StatePropertiesPredicate.Builder.properties()
                                                        .hasProperty(DeliveryPackageBlock.PACKAGES, 4))))))
                .build());
    }

    private void dropsSelf(HashCache cache, BlockItem blockItem) {
        writeTable(cache, Wares.resource("blocks/" + Objects.requireNonNull(blockItem.getBlock().getRegistryName()).getPath()),
                LootTable.lootTable()
                        .setParamSet(LootContextParamSets.BLOCK)
                        .withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(blockItem)))
                        .build());
    }

    protected LootTable.Builder silkTouchTable(Item lootItem) {
        return LootTable.lootTable()
                .setParamSet(LootContextParamSets.BLOCK)
                .withPool(
                LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(lootItem)
                                .when(MatchTool.toolMatches(ItemPredicate.Builder.item()
                                        .hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.atLeast(1)))))));
    }

    protected LootTable.Builder silkTouchOrDefaultTable(Block block, Item lootItem, float min, float max) {
        LootPool.Builder builder = LootPool.lootPool()
                .name(Objects.requireNonNull(block.getRegistryName()).getPath())
                .setRolls(ConstantValue.exactly(1))
                .add(AlternativesEntry.alternatives(
                                LootItem.lootTableItem(block)
                                        .when(MatchTool.toolMatches(ItemPredicate.Builder.item()
                                                .hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.atLeast(1))))),
                                LootItem.lootTableItem(lootItem)
                                        .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))
                                        .apply(ApplyExplosionDecay.explosionDecay())));
        return LootTable.lootTable().setParamSet(LootContextParamSets.BLOCK).withPool(builder);
    }

    private void writeTable(HashCache cache, ResourceLocation location, LootTable lootTable) {
        Path outputFolder = this.generator.getOutputFolder();
        Path path = outputFolder.resolve("data/" + location.getNamespace() + "/loot_tables/" + location.getPath() + ".json");
        try {
            DataProvider.save(GSON, cache, net.minecraft.world.level.storage.loot.LootTables.serialize(lootTable), path);
        } catch (IOException e) {
            LOGGER.error("Couldn't write loot lootTable {}", path, e);
        }
    }
}
