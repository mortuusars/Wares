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
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
public record Package(Either<ResourceLocation, List<ItemStack>> items) {
    public static final Codec<Package> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.either(ResourceLocation.CODEC, Codec.list(ItemStack.CODEC))
                    .optionalFieldOf("packedItems", Either.right(List.of(ItemStack.EMPTY))).forGetter(Package::items))
            .apply(instance, Package::new));

    public static final ResourceLocation DEFAULT_LOOT_TABLE = Wares.resource("gameplay/empty_package");
    public static final Package DEFAULT = new Package(Either.left(DEFAULT_LOOT_TABLE));

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
        //noinspection DataFlowIssue
        return stack.hasTag() ? fromTag(stack.getTag()) : Optional.empty();
    }

    public List<ItemStack> getItems(ServerLevel level) {
        return this.items.map(lootTable -> fromLootTable(level, lootTable), itemsList -> fromItemsList(level, itemsList));
    }

    public static List<ItemStack> getDefaultItems(ServerLevel level) {
        return fromLootTable(level, DEFAULT_LOOT_TABLE);
    }

    private static List<ItemStack> fromLootTable(ServerLevel level, ResourceLocation table) {
        LootTable lootTable = level.getServer().getLootTables().get(table);
        LootContext.Builder lootContextBuilder = new LootContext.Builder(level);
        List<ItemStack> randomItems = lootTable.getRandomItems(lootContextBuilder.create(LootContextParamSets.EMPTY));
        return randomItems.size() == 0 ? getDefaultItems(level) : randomItems;
    }

    private static List<ItemStack> fromItemsList(ServerLevel level, List<ItemStack> stacks) {
        ArrayList<ItemStack> nonEmptyStacks = new ArrayList<>(stacks.stream().filter(itemStack -> !itemStack.isEmpty()).toList());
        return nonEmptyStacks.size() == 0 ? getDefaultItems(level) : nonEmptyStacks;
    }
}
