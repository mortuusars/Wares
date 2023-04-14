package io.github.mortuusars.wares.data.generation.provider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.block.CardboardBoxBlock;
import io.github.mortuusars.wares.data.agreement.SealedAgreement;
import io.github.mortuusars.wares.data.agreement.component.SteppedInt;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

@SuppressWarnings({"unused"})
public class LootTables extends LootTableProvider {
    public static final ResourceLocation PACKAGE_VILLAGE_PLAINS = Wares.resource("package/village/plains");

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final DataGenerator generator;

    public LootTables(DataGenerator generator) {
        super(generator);
        this.generator = generator;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void run(@NotNull HashCache cache) {
        dropsSelf(cache, Wares.Items.DELIVERY_TABLE.get());

        writeTable(cache, Wares.resource("gameplay/empty_package"), LootTable.lootTable()
                .setParamSet(LootContextParamSets.CHEST)
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(Items.PAPER)))
                .build());

        CardboardBoxBlock cardboardBoxBlock = Wares.Blocks.CARDBOARD_BOX.get();
        writeTable(cache, Wares.resource("blocks/" + Wares.Blocks.CARDBOARD_BOX.getId().getPath()), LootTable.lootTable()
                .setParamSet(LootContextParamSets.BLOCK)
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(cardboardBoxBlock)
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F))
                                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(cardboardBoxBlock)
                                                .setProperties(StatePropertiesPredicate.Builder.properties()
                                                        .hasProperty(CardboardBoxBlock.BOXES, 2))))
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(3.0F))
                                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(cardboardBoxBlock)
                                                .setProperties(StatePropertiesPredicate.Builder.properties()
                                                        .hasProperty(CardboardBoxBlock.BOXES, 3))))
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(4.0F))
                                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(cardboardBoxBlock)
                                                .setProperties(StatePropertiesPredicate.Builder.properties()
                                                        .hasProperty(CardboardBoxBlock.BOXES, 4))))))
                .build());

        // AGREEMENT ITEMS
        writeTable(cache, Wares.resource("agreement/village/plains_requested"), LootTable.lootTable()
                .setParamSet(LootContextParamSets.CHEST)
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(item(Items.WHEAT, 8, 14))
                        .add(item(Items.POTATO, 8, 14))
                        .add(item(Items.LEATHER, 4, 10))
                        .add(item(Items.PAPER, 10, 16))
                        .add(item(Items.BEEF, 2, 6))
                        .add(item(Items.PORKCHOP, 2, 6))
                        .add(item(Items.CHICKEN, 2, 6))
                        .add(item(Items.BREAD, 3, 6)))
                .build());

        writeTable(cache, Wares.resource("agreement/village/plains_payment"), LootTable.lootTable()
                .setParamSet(LootContextParamSets.CHEST)
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(item(Items.EMERALD, 1, 3)))
                .build());

        // CHESTS

        SealedAgreement plainsSealedAgreement = new SealedAgreement.Builder()
                .requested(Wares.resource("agreement/village/plains_requested"))
                .payment(Wares.resource("agreement/village/plains_payment"))
                .ordered(new SteppedInt(32, 96, 8))
                .experience(new SteppedInt(12, 32, 4))
                .build();

        ItemStack plainsSealedStack = new ItemStack(Wares.Items.SEALED_DELIVERY_AGREEMENT.get());
        plainsSealedAgreement.toItemStack(plainsSealedStack);

        writeTable(cache, Wares.resource("chests/village/plains_warehouse"), LootTable.lootTable()
                .setParamSet(LootContextParamSets.CHEST)
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(2, 6))
                        .add(LootItem.lootTableItem(Items.PAPER).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))).setWeight(10)))
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(1, 6))
                        .add(LootItem.lootTableItem(Wares.Items.CARDBOARD_BOX.get()).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))).setWeight(8)))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(Wares.Items.SEALED_DELIVERY_AGREEMENT.get()).apply(SetNbtFunction.setTag(plainsSealedStack.getOrCreateTag())).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))).setWeight(6))
                        .add(EmptyLootItem.emptyItem()))
                .build());

        // PACKAGES
        writeTable(cache, PACKAGE_VILLAGE_PLAINS, LootTable.lootTable()
                .setParamSet(LootContextParamSets.CHEST)
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(Items.WHEAT).apply(SetItemCountFunction.setCount(UniformGenerator.between(12, 32))).setWeight(10))
                        .add(LootItem.lootTableItem(Items.POTATO).apply(SetItemCountFunction.setCount(UniformGenerator.between(12, 32))).setWeight(10))
                        .add(LootItem.lootTableItem(Items.CARROT).apply(SetItemCountFunction.setCount(UniformGenerator.between(12, 32))).setWeight(10))
                        .add(LootItem.lootTableItem(Items.PAPER).apply(SetItemCountFunction.setCount(UniformGenerator.between(10, 30))).setWeight(10))
                        .add(LootItem.lootTableItem(Items.BOOK).apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 12))).setWeight(10))
                        .add(LootItem.lootTableItem(Items.BLACK_DYE).apply(SetItemCountFunction.setCount(UniformGenerator.between(6, 16))).setWeight(10))
                        .add(LootItem.lootTableItem(Items.LEATHER).apply(SetItemCountFunction.setCount(UniformGenerator.between(6, 16))).setWeight(10))
                        .add(LootItem.lootTableItem(Items.PORKCHOP).apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 10))).setWeight(10))
                        .add(LootItem.lootTableItem(Items.BEEF).apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 10))).setWeight(10))
                        .add(LootItem.lootTableItem(Items.MUTTON).apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 10))).setWeight(10))
                        .add(LootItem.lootTableItem(Items.EXPERIENCE_BOTTLE).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 8))).setWeight(2)))
                .build());
    }

    private LootPoolSingletonContainer.Builder<?> item(ItemLike item, int count) {
        return item(item, count, count);
    }

    private LootPoolSingletonContainer.Builder<?> item(ItemLike item, int min, int max) {
        LootPoolSingletonContainer.Builder<?> itemBuilder = LootItem.lootTableItem(item);

        if (min == max)
            itemBuilder.apply(SetItemCountFunction.setCount(ConstantValue.exactly(min)));
        else
            itemBuilder.apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)));

        return itemBuilder;
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
