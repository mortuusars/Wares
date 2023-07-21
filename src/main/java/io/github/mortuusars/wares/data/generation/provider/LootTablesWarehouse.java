package io.github.mortuusars.wares.data.generation.provider;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;
import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.data.agreement.SealedDeliveryAgreement;
import io.github.mortuusars.wares.data.agreement.component.SteppedInt;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class LootTablesWarehouse {
    public List<Pair<ResourceLocation, LootTable>> createTables() {
        List<Pair<ResourceLocation, LootTable>> list = new ArrayList<>();

        list.addAll(plains());
        list.addAll(taiga());
        list.addAll(desert());
        list.addAll(savanna());
        list.addAll(snowy());

        return list;
    }

    private List<Pair<ResourceLocation, LootTable>> plains() {
        List<Pair<ResourceLocation, LootTable>> list = new ArrayList<>();

        String type = "plains";

        list.add(Pair.of(Wares.resource("agreement/village/" + type + "_requested_sell"), agreementItems(
                item(Items.WHEAT, 8, 14),
                item(Items.POTATO, 8, 14),
                item(Items.LEATHER, 4, 10),
                item(Items.PAPER, 10, 16),
                item(Items.BEEF, 2, 6),
                item(Items.PORKCHOP, 2, 6),
                item(Items.CHICKEN, 2, 6),
                item(Items.BREAD, 3, 6))));

        list.add(Pair.of(Wares.resource("agreement/village/" + type + "_payment_sell"), agreementItems(
                item(Items.EMERALD, 1, 3))));

        list.add(Pair.of(Wares.resource("agreement/village/" + type + "_requested_buy"), agreementItems(
                item(Items.EMERALD, 3, 8))));

        list.add(Pair.of(Wares.resource("agreement/village/" + type + "_payment_buy"), agreementItems(
                item(Items.WHEAT, 6, 14),
                item(Items.POTATO, 6, 14),
                item(Items.LEATHER, 4, 10),
                item(Items.PAPER, 8, 16),
                item(Items.BEEF, 2, 6),
                item(Items.PORKCHOP, 2, 6),
                item(Items.CHICKEN, 2, 6),
                item(Items.BREAD, 3, 6),
                item(Items.DANDELION, 6, 10),
                item(Items.SUNFLOWER, 5, 10))));

        list.add(Pair.of(Wares.resource("chests/village/" + type + "_warehouse"), chestLootTableWithAgreement(type)));

        list.add(Pair.of(Wares.resource("package/village/" + type), LootTable.lootTable()
                .setParamSet(LootContextParamSets.CHEST)
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(item(Items.WHEAT, 12, 32).setWeight(10))
                        .add(item(Items.POTATO, 12, 32).setWeight(10))
                        .add(item(Items.CARROT, 12, 32).setWeight(10))
                        .add(item(Items.PAPER, 10, 30).setWeight(10))
                        .add(item(Items.BOOK, 4, 12).setWeight(10))
                        .add(item(Items.BLACK_DYE, 6, 16).setWeight(10))
                        .add(item(Items.LEATHER, 6, 16).setWeight(10))
                        .add(item(Items.PORKCHOP, 4, 10).setWeight(10))
                        .add(item(Items.BEEF, 4, 10).setWeight(10))
                        .add(item(Items.MUTTON, 4, 10).setWeight(10))
                        .add(item(Items.SUNFLOWER, 4, 10).setWeight(4))
                        .add(item(Items.TALL_GRASS, 4, 10).setWeight(2))
                        .add(item(Items.EMERALD,8, 20).setWeight(2))
                        .add(item(Items.EXPERIENCE_BOTTLE, 2, 8).setWeight(2)))
                .build()));

        return list;
    }

    private List<Pair<ResourceLocation, LootTable>> taiga() {
        List<Pair<ResourceLocation, LootTable>> list = new ArrayList<>();

        String type = "taiga";

        list.add(Pair.of(Wares.resource("agreement/village/" + type + "_requested_sell"), agreementItems(
                item(Items.PUMPKIN, 4, 12),
                item(Items.SWEET_BERRIES, 10, 16),
                item(Items.LEATHER, 4, 10),
                item(Items.PAPER, 10, 16),
                item(Items.SPRUCE_SAPLING, 6, 16),
                item(Items.SPRUCE_LOG, 4, 12),
                item(Items.POTATO, 10, 18),
                item(Items.BREAD, 3, 6),
                item(Items.RED_MUSHROOM, 8, 14),
                item(Items.BROWN_MUSHROOM, 8, 14))));

        list.add(Pair.of(Wares.resource("agreement/village/" + type + "_payment_sell"), agreementItems(
                item(Items.EMERALD, 1, 3))));

        list.add(Pair.of(Wares.resource("agreement/village/" + type + "_requested_buy"), agreementItems(
                item(Items.EMERALD, 3, 8))));

        list.add(Pair.of(Wares.resource("agreement/village/" + type + "_payment_buy"), agreementItems(
                item(Items.PUMPKIN, 6, 14).setWeight(10),
                item(Items.POTATO, 6, 14).setWeight(10),
                item(Items.LEATHER, 4, 10).setWeight(10),
                item(Items.PAPER, 8, 16).setWeight(10),
                item(Items.SPRUCE_SAPLING, 16, 24).setWeight(10),
                item(Items.SPRUCE_LOG, 14, 24).setWeight(10),
                item(Items.BREAD, 6, 12).setWeight(10),
                item(Items.MOSSY_COBBLESTONE, 12, 24).setWeight(4),
                item(Items.PODZOL, 12, 24).setWeight(4),
                item(Items.RED_MUSHROOM, 8, 14).setWeight(4),
                item(Items.BROWN_MUSHROOM, 8, 14).setWeight(4))));

        list.add(Pair.of(Wares.resource("chests/village/" + type + "_warehouse"), chestLootTableWithAgreement(type)));

        list.add(Pair.of(Wares.resource("package/village/" + type), LootTable.lootTable()
                .setParamSet(LootContextParamSets.CHEST)
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(item(Items.SWEET_BERRIES,12, 32).setWeight(10))
                        .add(item(Items.PUMPKIN,8, 16).setWeight(10))
                        .add(item(Items.POTATO,12, 32).setWeight(10))
                        .add(item(Items.PAPER,10, 30).setWeight(10))
                        .add(item(Items.BOOK,4, 12).setWeight(10))
                        .add(item(Items.BLACK_DYE,6, 16).setWeight(10))
                        .add(item(Items.LEATHER,6, 16).setWeight(10))
                        .add(item(Items.SPRUCE_LOG,16, 32).setWeight(10))
                        .add(item(Items.SPRUCE_SAPLING,8, 18).setWeight(10))
                        .add(item(Items.FERN,8, 18).setWeight(10))
                        .add(item(Items.LARGE_FERN,8, 18).setWeight(2))
                        .add(item(Items.EMERALD,8, 20).setWeight(2))
                        .add(item(Items.EXPERIENCE_BOTTLE,2, 8).setWeight(2)))
                .build()));

        return list;
    }

    private List<Pair<ResourceLocation, LootTable>> desert() {
        List<Pair<ResourceLocation, LootTable>> list = new ArrayList<>();

        String type = "desert";

        list.add(Pair.of(Wares.resource("agreement/village/" + type + "_requested_sell"), agreementItems(
                item(Items.WHEAT, 6, 16),
                item(Items.CACTUS, 4, 12),
                item(Items.GREEN_DYE, 8, 16),
                item(Items.CLAY_BALL, 8, 14),
                item(Items.SANDSTONE, 10, 16),
                item(Items.RED_SANDSTONE, 10, 16),
                item(Items.TERRACOTTA, 10, 16),
                item(Items.BLACK_TERRACOTTA, 10, 16),
                item(Items.WHITE_TERRACOTTA, 10, 16),
                item(Items.RABBIT_HIDE, 4, 10),
                item(Items.PAPER, 10, 16),
                item(Items.BREAD, 3, 6))));

        list.add(Pair.of(Wares.resource("agreement/village/" + type + "_payment_sell"), agreementItems(
                item(Items.EMERALD, 1, 3))));

        list.add(Pair.of(Wares.resource("agreement/village/" + type + "_requested_buy"), agreementItems(
                item(Items.EMERALD, 3, 8))));

        list.add(Pair.of(Wares.resource("agreement/village/" + type + "_payment_buy"), agreementItems(
                item(Items.WHEAT, 4, 8).setWeight(10),
                item(Items.CACTUS, 4, 8).setWeight(10),
                item(Items.RABBIT_HIDE, 4, 10).setWeight(10),
                item(Items.PAPER, 8, 16).setWeight(10),
                item(Items.CLAY_BALL, 8, 14).setWeight(8),
                item(Items.DEAD_BUSH, 8, 16).setWeight(6),
                item(Items.BREAD, 6, 12).setWeight(10))));

        list.add(Pair.of(Wares.resource("chests/village/" + type + "_warehouse"), chestLootTableWithAgreement(type)));

        list.add(Pair.of(Wares.resource("package/village/" + type), LootTable.lootTable()
                .setParamSet(LootContextParamSets.CHEST)
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(item(Items.CACTUS,8, 16).setWeight(10))
                        .add(item(Items.CLAY_BALL,8, 16).setWeight(10))
                        .add(item(Items.WHEAT,6, 12).setWeight(10))
                        .add(item(Items.PAPER,10, 30).setWeight(10))
                        .add(item(Items.BOOK,4, 12).setWeight(10))
                        .add(item(Items.BLACK_DYE,6, 16).setWeight(10))
                        .add(item(Items.RABBIT_HIDE,6, 16).setWeight(10))
                        .add(item(Items.DEAD_BUSH,8, 18).setWeight(10))
                        .add(item(Items.BLACK_GLAZED_TERRACOTTA,6, 16).setWeight(4))
                        .add(item(Items.BLUE_GLAZED_TERRACOTTA,6, 16).setWeight(4))
                        .add(item(Items.RED_GLAZED_TERRACOTTA,6, 16).setWeight(4))
                        .add(item(Items.WHITE_GLAZED_TERRACOTTA,6, 16).setWeight(4))
                        .add(item(Items.RABBIT_FOOT,2, 6).setWeight(2))
                        .add(item(Items.EMERALD,8, 20).setWeight(2))
                        .add(item(Items.EXPERIENCE_BOTTLE,2, 8).setWeight(2)))
                .build()));

        return list;
    }

    private List<Pair<ResourceLocation, LootTable>> savanna() {
        List<Pair<ResourceLocation, LootTable>> list = new ArrayList<>();

        String type = "savanna";

        list.add(Pair.of(Wares.resource("agreement/village/" + type + "_requested_sell"), agreementItems(
                item(Items.CARROT, 6, 16),
                item(Items.BEETROOT, 6, 16),
                item(Items.GOLD_NUGGET, 8, 14),
                item(Items.RED_SANDSTONE, 10, 16),
                item(Items.ACACIA_LOG, 10, 16),
                item(Items.ACACIA_SAPLING, 10, 16),
                item(Items.PAPER, 10, 16),
                item(Items.BREAD, 3, 6))));

        list.add(Pair.of(Wares.resource("agreement/village/" + type + "_payment_sell"), agreementItems(
                item(Items.EMERALD, 1, 3))));

        list.add(Pair.of(Wares.resource("agreement/village/" + type + "_requested_buy"), agreementItems(
                item(Items.EMERALD, 3, 8))));

        list.add(Pair.of(Wares.resource("agreement/village/" + type + "_payment_buy"), agreementItems(
                item(Items.CARROT, 4, 8).setWeight(10),
                item(Items.BEETROOT, 4, 8).setWeight(10),
                item(Items.GOLD_INGOT, 2, 6).setWeight(10),
                item(Items.PAPER, 8, 16).setWeight(10),
                item(Items.ACACIA_SAPLING, 8, 14).setWeight(8),
                item(Items.ACACIA_LOG, 8, 16).setWeight(6),
                item(Items.BREAD, 6, 12).setWeight(10))));

        list.add(Pair.of(Wares.resource("chests/village/" + type + "_warehouse"), chestLootTableWithAgreement(type)));

        list.add(Pair.of(Wares.resource("package/village/" + type), LootTable.lootTable()
                .setParamSet(LootContextParamSets.CHEST)
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(item(Items.CARROT,8, 16).setWeight(10))
                        .add(item(Items.BEETROOT,8, 16).setWeight(10))
                        .add(item(Items.WHEAT,6, 12).setWeight(10))
                        .add(item(Items.PAPER,10, 30).setWeight(10))
                        .add(item(Items.BOOK,4, 12).setWeight(10))
                        .add(item(Items.BLACK_DYE,6, 16).setWeight(10))
                        .add(item(Items.DEAD_BUSH,8, 18).setWeight(10))
                        .add(item(Items.MUTTON,6, 12).setWeight(10))
                        .add(item(Items.VINE,6, 12).setWeight(4))
                        .add(item(Items.BELL,1, 3).setWeight(2))
                        .add(item(Items.EMERALD,8, 20).setWeight(2))
                        .add(item(Items.EXPERIENCE_BOTTLE,2, 8).setWeight(2)))
                .build()));

        return list;
    }

    private List<Pair<ResourceLocation, LootTable>> snowy() {
        List<Pair<ResourceLocation, LootTable>> list = new ArrayList<>();

        String type = "snowy";

        list.add(Pair.of(Wares.resource("agreement/village/" + type + "_requested_sell"), agreementItems(
                item(Items.POTATO, 6, 16),
                item(Items.LANTERN, 3, 8),
                item(Items.COAL, 8, 16),
                item(Items.STRIPPED_SPRUCE_LOG, 8, 14),
                item(Items.SPRUCE_PLANKS, 10, 16),
                item(Items.PAPER, 10, 16),
                item(Items.BREAD, 3, 6))));

        list.add(Pair.of(Wares.resource("agreement/village/" + type + "_payment_sell"), agreementItems(
                item(Items.EMERALD, 1, 3))));

        list.add(Pair.of(Wares.resource("agreement/village/" + type + "_requested_buy"), agreementItems(
                item(Items.EMERALD, 3, 8))));

        list.add(Pair.of(Wares.resource("agreement/village/" + type + "_payment_buy"), agreementItems(
                item(Items.POTATO, 3, 8).setWeight(10),
                item(Items.SNOW_BLOCK, 8, 14).setWeight(10),
                item(Items.ICE, 8, 14).setWeight(10),
                item(Items.BLUE_ICE, 6, 12).setWeight(10),
                item(Items.SNOWBALL, 14, 24).setWeight(10),

                item(Items.BREAD, 6, 12).setWeight(10))));

        list.add(Pair.of(Wares.resource("chests/village/" + type + "_warehouse"), chestLootTableWithAgreement(type)));

        list.add(Pair.of(Wares.resource("package/village/" + type), LootTable.lootTable()
                .setParamSet(LootContextParamSets.CHEST)
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(item(Items.POTATO,8, 16).setWeight(10))
                        .add(item(Items.SNOW_BLOCK,12, 20).setWeight(10))
                        .add(item(Items.ICE,12, 20).setWeight(10))
                        .add(item(Items.BLUE_ICE,12, 20).setWeight(10))
                        .add(item(Items.STRIPPED_SPRUCE_LOG,12, 20).setWeight(10))
                        .add(item(Items.BOOK,4, 12).setWeight(10))
                        .add(item(Items.BLACK_DYE,6, 16).setWeight(10))
                        .add(item(Items.BELL,1, 3).setWeight(2))
                        .add(item(Items.EMERALD,8, 20).setWeight(2))
                        .add(item(Items.EXPERIENCE_BOTTLE,2, 8).setWeight(2)))
                .build()));

        return list;
    }

    @NotNull
    private LootTable chestLootTableWithAgreement(String type) {
        SealedDeliveryAgreement agreementSell = new SealedDeliveryAgreement.Builder()
                .requested(Wares.resource("agreement/village/" + type + "_requested_sell"))
                .payment(Wares.resource("agreement/village/" + type + "_payment_sell"))
                .ordered(new SteppedInt(64, 256, 8))
                .experience(new SteppedInt(12, 32, 4))
                .build();

        ItemStack sealedStackSell = new ItemStack(Wares.Items.SEALED_DELIVERY_AGREEMENT.get());
        agreementSell.toItemStack(sealedStackSell);

        SealedDeliveryAgreement agreementBuy = new SealedDeliveryAgreement.Builder()
                .requested(Wares.resource("agreement/village/" + type + "_requested_buy"))
                .payment(Wares.resource("agreement/village/" + type + "_payment_buy"))
                .ordered(new SteppedInt(64, 256, 8))
                .experience(new SteppedInt(12, 32, 4))
                .build();

        ItemStack sealedStackBuy = new ItemStack(Wares.Items.SEALED_DELIVERY_AGREEMENT.get());
        agreementBuy.toItemStack(sealedStackBuy);

        return LootTable.lootTable()
                .setParamSet(LootContextParamSets.CHEST)
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(2, 6))
                        .add(item(Items.PAPER, 1, 3).setWeight(10)))
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(1, 6))
                        .add(item(Wares.Items.CARDBOARD_BOX.get(), 1, 1).setWeight(8)))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(Wares.Items.SEALED_DELIVERY_AGREEMENT.get())
                                .apply(SetNbtFunction.setTag(sealedStackSell.getOrCreateTag()))
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))).setWeight(6))
                        .add(LootItem.lootTableItem(Wares.Items.SEALED_DELIVERY_AGREEMENT.get())
                                .apply(SetNbtFunction.setTag(sealedStackBuy.getOrCreateTag()))
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))).setWeight(3))
                        .add(EmptyLootItem.emptyItem()))
                .build();
    }

    private LootTable agreementItems(LootPoolSingletonContainer.Builder<?>... items) {
        Preconditions.checkArgument(items.length > 0, "No items were provided.");

        LootPool.Builder pool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1));

        for (LootPoolSingletonContainer.Builder<?> item : items) {
            pool.add(item);
        }

        return LootTable.lootTable()
                .setParamSet(LootContextParamSets.CHEST)
                .withPool(pool)
                .build();
    }

    @SuppressWarnings("unused")
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
}
