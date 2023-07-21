package io.github.mortuusars.wares.data.agreement;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.data.agreement.component.RequestedItem;
import io.github.mortuusars.wares.data.agreement.component.SealedRequestedItem;
import io.github.mortuusars.wares.data.agreement.component.SteppedInt;
import io.github.mortuusars.wares.data.agreement.component.TextProvider;
import io.github.mortuusars.wares.data.serialization.ComponentCodec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

@SuppressWarnings({"UnusedReturnValue", "SameParameterValue"})
public record SealedDeliveryAgreement(String id,
                                      TextProvider buyerName,
                                      TextProvider buyerAddress,
                                      TextProvider title,
                                      TextProvider message,
                                      String seal,
                                      Component sealTooltip,
                                      Component backsideMessage,
                                      Either<ResourceLocation, List<SealedRequestedItem>> requested,
                                      Either<ResourceLocation, List<ItemStack>> payment,
                                      Either<Integer, SteppedInt> ordered,
                                      Either<Integer, SteppedInt> experience,
                                      Either<Integer, SteppedInt> deliveryTime,
                                      Either<Integer, SteppedInt> expiresInSeconds) {

    public static final Codec<SealedDeliveryAgreement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(SealedDeliveryAgreement::id),
            TextProvider.CODEC.optionalFieldOf("buyerName", TextProvider.EMPTY).forGetter(SealedDeliveryAgreement::buyerName),
            TextProvider.CODEC.optionalFieldOf("buyerAddress", TextProvider.EMPTY).forGetter(SealedDeliveryAgreement::buyerAddress),
            TextProvider.CODEC.optionalFieldOf("title", TextProvider.EMPTY).forGetter(SealedDeliveryAgreement::title),
            TextProvider.CODEC.optionalFieldOf("message", TextProvider.EMPTY).forGetter(SealedDeliveryAgreement::message),
            Codec.STRING.optionalFieldOf("seal", "default").forGetter(SealedDeliveryAgreement::seal),
            ComponentCodec.CODEC.optionalFieldOf("sealTooltip", Component.empty()).forGetter(SealedDeliveryAgreement::sealTooltip),
            ComponentCodec.CODEC.optionalFieldOf("backsideMessage", Component.empty()).forGetter(SealedDeliveryAgreement::backsideMessage),
            Codec.either(ResourceLocation.CODEC, Codec.list(SealedRequestedItem.CODEC)).optionalFieldOf("requested", Either.right(Collections.emptyList())).forGetter(SealedDeliveryAgreement::requested),
            Codec.either(ResourceLocation.CODEC, Codec.list(ItemStack.CODEC)).optionalFieldOf("payment", Either.right(Collections.emptyList())).forGetter(SealedDeliveryAgreement::payment),
            Codec.either(Codec.INT, SteppedInt.CODEC).optionalFieldOf("ordered", Either.left(0)).forGetter(SealedDeliveryAgreement::ordered),
            Codec.either(Codec.INT, SteppedInt.CODEC).optionalFieldOf("experience", Either.left(0)).forGetter(SealedDeliveryAgreement::experience),
            Codec.either(Codec.INT, SteppedInt.CODEC).optionalFieldOf("deliveryTime", Either.left(-1)).forGetter(SealedDeliveryAgreement::deliveryTime),
            Codec.either(Codec.INT, SteppedInt.CODEC).optionalFieldOf("expiresInSeconds", Either.left(-1)).forGetter(SealedDeliveryAgreement::expiresInSeconds)
        ).apply(instance, SealedDeliveryAgreement::new));

    public static Optional<SealedDeliveryAgreement> fromItemStack(ItemStack itemStack) {
        @Nullable CompoundTag compoundTag = itemStack.getTag();
        if (compoundTag == null)
            return Optional.empty();

        try {
            DataResult<Pair<SealedDeliveryAgreement, Tag>> result = CODEC.decode(NbtOps.INSTANCE, compoundTag);
            SealedDeliveryAgreement agreement = result.getOrThrow(false, Wares.LOGGER::error).getFirst();
            return Optional.of(agreement);
        } catch (Exception e) {
            Wares.LOGGER.error("Failed to decode SealedAgreement from item : '" + itemStack + "'.\n" + e);
            return Optional.empty();
        }
    }

    public boolean toItemStack(ItemStack stack) {
        try {
            CompoundTag tag = stack.getOrCreateTag();
            DataResult<Tag> result = CODEC.encodeStart(NbtOps.INSTANCE, this);
            Tag encodedTag = result.getOrThrow(false, Wares.LOGGER::error);
            tag.merge((CompoundTag) encodedTag);
            return true;
        } catch (Exception e) {
            Wares.LOGGER.error("Failed to encode SealedAgreement to item :\n" + e);
            return false;
        }
    }

    public DeliveryAgreement realize(ServerLevel level) {
        RandomSource random = level.getRandom();

        int quantity = ordered.map(integer -> integer, steppedInt -> steppedInt.sample(random));

        int expiresIn = expiresInSeconds.map(integer -> integer, steppedInt -> steppedInt.sample(random));
        long expireTime = expiresIn <= 0 ? -1 : level.getGameTime() + expiresIn * 20L;

        return DeliveryAgreement.builder().id(id)
                .buyerName(buyerName.get(random))
                .buyerAddress(buyerAddress.get(random))
                .title(title.get(random))
                .message(message.get(random))
                .seal(seal)
                .requested(realizeRequested(requested, level, DeliveryAgreement.MAX_REQUESTED_STACKS))
                .payment(realizePayment(payment, level, DeliveryAgreement.MAX_PAYMENT_STACKS))
                .ordered(quantity)
                .experience(experience.map(integer -> integer, steppedInt -> steppedInt.sample(random)))
                .deliveryTime(deliveryTime.map(integer -> integer, steppedInt -> steppedInt.sample(random)))
                .expireTime(expireTime)
                .build();
    }

    private List<RequestedItem> realizeRequested(Either<ResourceLocation, List<SealedRequestedItem>> requested, ServerLevel level, int maxCount) {
        return requested.map(
                lootTable -> compressAndLimitStacks(unpackLootTable(lootTable, level), maxCount)
                        .stream()
                        .map(RequestedItem::new)
                        .toList(),
                sealedItems -> sealedItems
                        .stream()
                        .map(mapSealedItem(level))
                        .toList());
    }

    @NotNull
    private static Function<SealedRequestedItem, RequestedItem> mapSealedItem(ServerLevel level) {
        return sealedItem -> new RequestedItem(
                sealedItem.getTagOrItem(),
                sealedItem.getCount().map(integer -> integer, steppedInt -> steppedInt.sample(level.getRandom())),
                sealedItem.getTag());
    }

    private List<ItemStack> realizePayment(Either<ResourceLocation, List<ItemStack>> payment, ServerLevel level, int maxCount) {
        return compressAndLimitStacks(payment.map(lootTable -> unpackLootTable(lootTable, level), stacks -> stacks), maxCount);
    }

    private List<ItemStack> unpackLootTable(ResourceLocation lootTablePath, ServerLevel level) {
        LootTable lootTable = level.getServer().getLootTables().get(lootTablePath);
        LootContext.Builder lootContextBuilder = new LootContext.Builder(level);
        return lootTable.getRandomItems(lootContextBuilder.create(LootContextParamSets.EMPTY));
    }

    private List<ItemStack> compressAndLimitStacks(List<ItemStack> stacks, int stackLimit) {
        SimpleContainer container = new SimpleContainer(stackLimit);

        for (ItemStack item : stacks) {
            container.addItem(item);
        }

        return container.removeAllItems();
    }

    @SuppressWarnings("unused")
    public static class Builder {
        private String id = "";
        private TextProvider buyerName = TextProvider.EMPTY;
        private TextProvider buyerAddress = TextProvider.EMPTY;
        private TextProvider title = TextProvider.EMPTY;
        private TextProvider message = TextProvider.EMPTY;
        private String seal = "default";
        private Component sealTooltip = Component.empty();
        private Component backsideMessage = Component.empty();
        private Either<ResourceLocation, List<SealedRequestedItem>> requested = Either.right(Collections.emptyList());
        private Either<ResourceLocation, List<ItemStack>> payment = Either.right(Collections.emptyList());
        private Either<Integer, SteppedInt> ordered = Either.left(0);
        private Either<Integer, SteppedInt> experience = Either.left(0);
        private Either<Integer, SteppedInt> deliveryTime = Either.left(-1);
        private Either<Integer, SteppedInt> expiresInSecond = Either.left(-1);

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder buyerName(TextProvider buyerName) {
            this.buyerName = buyerName;
            return this;
        }

        public Builder buyerAddress(TextProvider buyerAddress) {
            this.buyerAddress = buyerAddress;
            return this;
        }

        public Builder title(TextProvider title) {
            this.title = title;
            return this;
        }

        public Builder message(TextProvider message) {
            this.message = message;
            return this;
        }

        public Builder seal(String seal) {
            this.seal = seal;
            return this;
        }

        public Builder sealTooltip(Component sealTooltip) {
            this.sealTooltip = sealTooltip;
            return this;
        }

        public Builder backsideMessage(Component backsideMessage) {
            this.backsideMessage = backsideMessage;
            return this;
        }

        public Builder requested(ResourceLocation lootTable) {
            this.requested = Either.left(lootTable);
            return this;
        }

        public Builder payment(ResourceLocation lootTable) {
            this.payment = Either.left(lootTable);
            return this;
        }

        public Builder requested(List<SealedRequestedItem> items) {
            this.requested = Either.right(items);
            return this;
        }

        public Builder payment(List<ItemStack> items) {
            this.payment = Either.right(items);
            return this;
        }

        public Builder ordered(int ordered) {
            this.ordered = Either.left(ordered);
            return this;
        }

        public Builder ordered(SteppedInt ordered) {
            this.ordered = Either.right(ordered);
            return this;
        }

        public Builder experience(int experience) {
            this.experience = Either.left(experience);
            return this;
        }

        public Builder experience(SteppedInt experience) {
            this.experience = Either.right(experience);
            return this;
        }

        public Builder deliveryTime(int deliveryTime) {
            this.deliveryTime = Either.left(deliveryTime);
            return this;
        }

        public Builder deliveryTime(SteppedInt deliveryTime) {
            this.deliveryTime = Either.right(deliveryTime);
            return this;
        }

        public Builder expiresInSecond(int expiresInSecond) {
            this.expiresInSecond = Either.left(expiresInSecond);
            return this;
        }

        public Builder expiresInSecond(SteppedInt expiresInSecond) {
            this.expiresInSecond = Either.right(expiresInSecond);
            return this;
        }

        public SealedDeliveryAgreement build() {
            return new SealedDeliveryAgreement(id, buyerName, buyerAddress, title, message, seal, sealTooltip, backsideMessage,
                    requested, payment, ordered, experience, deliveryTime, expiresInSecond);
        }
    }
}
