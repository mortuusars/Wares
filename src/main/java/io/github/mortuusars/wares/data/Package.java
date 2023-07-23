package io.github.mortuusars.wares.data;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.wares.Wares;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
public record Package(Either<ResourceLocation, List<ItemStack>> items, String sender) {
    public static final Codec<Package> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.either(ResourceLocation.CODEC, Codec.list(ItemStack.CODEC)).optionalFieldOf("packedItems", Either.right(List.of(ItemStack.EMPTY))).forGetter(Package::items),
            Codec.STRING.optionalFieldOf("sender", "").forGetter(Package::sender))
            .apply(instance, Package::new));

    public static final ResourceLocation DEFAULT_LOOT_TABLE = Wares.resource("gameplay/empty_package");
    public static final Package DEFAULT = new Package(Either.left(DEFAULT_LOOT_TABLE));

    public Package(Either<ResourceLocation, List<ItemStack>> items) {
        this(items, "");
    }

    public void toTag(CompoundTag tag) {
        CODEC.encodeStart(NbtOps.INSTANCE, this)
                .resultOrPartial(Wares.LOGGER::error)
                .ifPresent(resultTag -> tag.merge((CompoundTag) resultTag));
    }

    public void toItemStack(ItemStack stack) {
        toTag(stack.getOrCreateTag());
    }

    public static Optional<Package> fromTag(@NotNull CompoundTag tag) {
        if (!tag.contains("packedItems") && tag.contains("BlockEntityTag", Tag.TAG_COMPOUND))
            tag = tag.getCompound("BlockEntityTag");

        return CODEC.decode(NbtOps.INSTANCE, tag)
                .resultOrPartial(Wares.LOGGER::error)
                .map(Pair::getFirst);
    }

    public static Optional<Package> fromItemStack(ItemStack stack) {
        return stack.getTag() != null ? fromTag(stack.getTag()) : Optional.empty();
    }

    public List<ItemStack> getItems(ServerLevel level, Vec3 position) {
        return this.items.map(lootTable -> fromLootTable(level, position, lootTable), itemsList -> fromItemsList(level, position, itemsList));
    }

    public static List<ItemStack> getDefaultItems(ServerLevel level, Vec3 position) {
        return unpackLootTable(level, position, DEFAULT_LOOT_TABLE);
    }

    private static List<ItemStack> fromLootTable(ServerLevel level, Vec3 position, ResourceLocation table) {
        List<ItemStack> items = unpackLootTable(level, position, table);
        return items.size() == 0 ? getDefaultItems(level, position) : items;
    }

    private static List<ItemStack> fromItemsList(ServerLevel level, Vec3 position, List<ItemStack> stacks) {
        ArrayList<ItemStack> nonEmptyStacks = new ArrayList<>(stacks.stream().filter(itemStack -> !itemStack.isEmpty()).toList());
        return nonEmptyStacks.size() == 0 ? getDefaultItems(level, position) : nonEmptyStacks;
    }

    private static List<ItemStack> unpackLootTable(ServerLevel level, Vec3 position, ResourceLocation tableLocation) {
        LootTable lootTable = level.getServer().getLootTables().get(tableLocation);
        LootContext.Builder lootContextBuilder = new LootContext.Builder(level);
        lootContextBuilder.withParameter(LootContextParams.ORIGIN, position);
        return lootTable.getRandomItems(lootContextBuilder.create(LootContextParamSets.CHEST));
    }
}
