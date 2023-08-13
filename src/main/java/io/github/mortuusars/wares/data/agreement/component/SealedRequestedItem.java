package io.github.mortuusars.wares.data.agreement.component;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class SealedRequestedItem {
    @SuppressWarnings("deprecation")
    public static final Codec<SealedRequestedItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.either(TagKey.hashedCodec(Registry.ITEM_REGISTRY), Registry.ITEM.byNameCodec()).fieldOf("id").forGetter(SealedRequestedItem::getTagOrItem),
                    Codec.either(ExtraCodecs.POSITIVE_INT, SteppedInt.CODEC).optionalFieldOf("Count", Either.left(1)).forGetter(SealedRequestedItem::getCount),
                    CompoundTag.CODEC.optionalFieldOf("tag").forGetter(sri -> Optional.ofNullable(sri.getTag())),
                    StringRepresentable.fromEnum(CompoundTagCompareBehavior::values).optionalFieldOf("TagMatching", CompoundTagCompareBehavior.WEAK).forGetter(SealedRequestedItem::getTagCompareBehavior))
            .apply(instance, SealedRequestedItem::new));

    public static final SealedRequestedItem EMPTY = new SealedRequestedItem(Either.right(Items.AIR), Either.left(1), (CompoundTag)null);

    private final Either<TagKey<Item>, Item> tagOrItem;
    private final Either<Integer, SteppedInt> count;
    @Nullable
    private final CompoundTag tag;
    private final CompoundTagCompareBehavior tagCompareBehavior;

    public SealedRequestedItem(Either<TagKey<Item>, Item> tagOrItem, Either<Integer, SteppedInt> count, @Nullable CompoundTag tag, CompoundTagCompareBehavior tagCompareBehavior) {
        this.tagOrItem = tagOrItem;
        this.count = count;
        this.tag = tag;
        this.tagCompareBehavior = tagCompareBehavior;
    }

    public SealedRequestedItem(Either<TagKey<Item>, Item> tagOrItem, Either<Integer, SteppedInt> count, @Nullable CompoundTag tag) {
        this(tagOrItem, count, tag, CompoundTagCompareBehavior.STRONG);
    }

    private SealedRequestedItem(Either<TagKey<Item>, Item> tagOrItem, Either<Integer, SteppedInt> count, Optional<CompoundTag> tag, CompoundTagCompareBehavior tagCompareBehavior) {
        this(tagOrItem, count, tag.orElse(null), tagCompareBehavior);
    }

    public Either<TagKey<Item>, Item> getTagOrItem() {
        return tagOrItem;
    }

    public Either<Integer, SteppedInt> getCount() {
        return count;
    }

    public @Nullable CompoundTag getTag() {
        return tag;
    }

    public CompoundTagCompareBehavior getTagCompareBehavior() {
        return tagCompareBehavior;
    }

    @Override
    public String toString() {
        return "RequestedItem{" +
                "tagOrItem=" + tagOrItem.map(tag -> "#" + tag.location(), item -> Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item)).toString()) +
                ", count=" + count.map(integer -> Integer.toString(integer),
                    steppedInt -> String.format("SteppedInt{%s,%s,%s}", steppedInt.min(), steppedInt.max(), steppedInt.step())) +
                (tag != null ? (", tag=" + tag) : "") +
                ",TagMatching:" + tagCompareBehavior.getSerializedName() +
                '}';
    }
}
