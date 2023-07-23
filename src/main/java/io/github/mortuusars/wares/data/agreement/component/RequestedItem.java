package io.github.mortuusars.wares.data.agreement.component;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class RequestedItem {
    @SuppressWarnings("deprecation")
    public static final Codec<RequestedItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.either(TagKey.hashedCodec(Registry.ITEM_REGISTRY), Registry.ITEM.byNameCodec()).fieldOf("id").forGetter(RequestedItem::getTagOrItem),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("Count", 1).forGetter(RequestedItem::getCount),
                    CompoundTag.CODEC.optionalFieldOf("tag").forGetter(ri -> Optional.ofNullable(ri.getTag())))
            .apply(instance, RequestedItem::new));

    public static final RequestedItem EMPTY = new RequestedItem(Items.AIR, 1);

    private final Either<TagKey<Item>, Item> tagOrItem;
    private final int count;
    @Nullable
    private final CompoundTag tag;

    public RequestedItem(Either<TagKey<Item>, Item> tagOrItem, int count, @Nullable CompoundTag tag) {
        this.tagOrItem = tagOrItem;
        this.count = count;
        this.tag = tag;
    }

    public RequestedItem(TagKey<Item> tag, int count) {
        this(Either.left(tag), count, (CompoundTag)null);
    }

    public RequestedItem(Item item, int count) {
        this(Either.right(item), count, (CompoundTag)null);
    }

    public RequestedItem(ItemStack stack) {
        this(Either.right(stack.getItem()), stack.getCount(), stack.getTag());
    }

    private RequestedItem(Either<TagKey<Item>, Item> tagOrItem, int count, Optional<CompoundTag> tag) {
        this(tagOrItem, count, tag.orElse(null));
    }

    public Either<TagKey<Item>, Item> getTagOrItem() {
        return tagOrItem;
    }

    public int getCount() {
        return count;
    }

    public @Nullable CompoundTag getTag() {
        return tag;
    }

    public List<ItemStack> getStacks() {
        return tagOrItem.map(tag -> Objects.requireNonNull(ForgeRegistries.ITEMS.tags()).getTag(tag).stream().map(item -> {
            ItemStack stack = new ItemStack(item, getCount());
            stack.setTag(getTag());
            return stack;
        }).toList(), item -> {
            ItemStack stack = new ItemStack(item, getCount());
            stack.setTag(getTag());
            return List.of(stack);
        });
    }

    @SuppressWarnings("Convert2MethodRef")
    public boolean matches(ItemStack stack) {
        return getTagOrItem().map(item -> stack.is(item), tag -> stack.is(tag)) && tagMatches(stack);
    }

    @SuppressWarnings("unused")
    public boolean matchesWithCount(ItemStack stack) {
        return matches(stack) && stack.getCount() >= getCount();
    }

    public boolean tagMatches(ItemStack stack) {
        CompoundTag tag = getTag();
        if (tag == null)
            return stack.getTag() == null || stack.getTag().isEmpty();

        CompoundTag stackTag = stack.getTag();
        if (stackTag == null)
            return tag.isEmpty();

        return tag.equals(stackTag);
    }

    public boolean isEmpty() {
        return this.equals(EMPTY) || getTagOrItem().map(tag -> false, item -> item instanceof AirItem);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestedItem that = (RequestedItem) o;
        return count == that.count && Objects.equals(tagOrItem, that.tagOrItem) && Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagOrItem, count, tag);
    }

    @Override
    public String toString() {
        return "RequestedItem{" +
                "tagOrItem=" + tagOrItem.map(tag -> "#" + tag.location(), item -> Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item)).toString()) +
                ", count=" + count +
                (tag != null ? (", tag=" + tag) : "") +
                '}';
    }
}
