package io.github.mortuusars.wares.data.agreement;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public record AgreementDescription(String id,
                                   TextProvider buyerName,
                                   TextProvider buyerAddress,
                                   TextProvider title,
                                   TextProvider message,
                                   Either<ResourceLocation, List<ItemStack>> requested,
                                   Either<ResourceLocation, List<ItemStack>> payment,
                                   Either<Integer, SteppedInt> ordered,
                                   Either<Integer, SteppedInt> experience,
                                   Either<Integer, SteppedInt> deliveryTime,
                                   Either<Integer, SteppedInt> expiresInSeconds) {

    public static final Codec<AgreementDescription> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(AgreementDescription::id),
            TextProvider.CODEC.optionalFieldOf("buyerName", TextProvider.EMPTY).forGetter(AgreementDescription::buyerName),
            TextProvider.CODEC.optionalFieldOf("buyerAddress", TextProvider.EMPTY).forGetter(AgreementDescription::buyerAddress),
            TextProvider.CODEC.optionalFieldOf("title", TextProvider.EMPTY).forGetter(AgreementDescription::title),
            TextProvider.CODEC.optionalFieldOf("message", TextProvider.EMPTY).forGetter(AgreementDescription::message),
            Codec.either(ResourceLocation.CODEC, Codec.list(ItemStack.CODEC)).fieldOf("requested").forGetter(AgreementDescription::requested),
            Codec.either(ResourceLocation.CODEC, Codec.list(ItemStack.CODEC)).fieldOf("payment").forGetter(AgreementDescription::payment),
            Codec.either(Codec.INT, SteppedInt.CODEC).optionalFieldOf("ordered", Either.left(0)).forGetter(AgreementDescription::ordered),
            Codec.either(Codec.INT, SteppedInt.CODEC).optionalFieldOf("experience", Either.left(0)).forGetter(AgreementDescription::experience),
            Codec.either(Codec.INT, SteppedInt.CODEC).optionalFieldOf("deliveryTime", Either.left(-1)).forGetter(AgreementDescription::deliveryTime),
            Codec.either(Codec.INT, SteppedInt.CODEC).optionalFieldOf("expiresInSeconds", Either.left(-1)).forGetter(AgreementDescription::expiresInSeconds)
        ).apply(instance, AgreementDescription::new));

    public static Optional<AgreementDescription> fromItemStack(ItemStack itemStack) {
        @Nullable CompoundTag compoundTag = itemStack.getTag();
        if (compoundTag == null)
            return Optional.empty();

        try {
            DataResult<Pair<AgreementDescription, Tag>> result = CODEC.decode(NbtOps.INSTANCE, compoundTag);
            AgreementDescription agreement = result.getOrThrow(false, Wares.LOGGER::error).getFirst();
            return Optional.of(agreement);
        } catch (Exception e) {
            Wares.LOGGER.error("Failed to decode AgreementDescription from item : '" + itemStack + "'.\n" + e);
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
            Wares.LOGGER.error("Failed to encode AgreementDescription to item :\n" + e);
            return false;
        }
    }

    public Agreement realize(ServerLevel level) {
        Random random = level.getRandom();

        int quantity = ordered.map(integer -> integer, steppedInt -> steppedInt.sample(random));

        int expiresIn = expiresInSeconds.map(integer -> integer, steppedInt -> steppedInt.sample(random));
        long expireTime = level.getGameTime() + expiresIn * 20L;

        Agreement agreement = Agreement.builder().id(id)
                .buyerName(buyerName.get(random))
                .buyerAddress(buyerAddress.get(random))
                .title(title.get(random))
                .message(message.get(random))
                .requestedItems(getStacks(requested, level))
                .paymentItems(getStacks(payment, level))
                .ordered(quantity)
                .experience(experience.map(integer -> integer, steppedInt -> steppedInt.sample(random)))
                .deliveryTime(deliveryTime.map(integer -> integer, steppedInt -> steppedInt.sample(random)))
                .expireTime(expireTime)
                .build();

        return agreement;
    }

    private List<ItemStack> getStacks(Either<ResourceLocation, List<ItemStack>> tableOrItems, ServerLevel level) {
        return tableOrItems.map(tableLocation -> fromLootTable(tableLocation, level), list -> list);
    }

    private List<ItemStack> fromLootTable(ResourceLocation lootTablePath, ServerLevel level) {
        LootTable lootTable = level.getServer().getLootTables().get(lootTablePath);
        LootContext.Builder lootContextBuilder = new LootContext.Builder(level);
        List<ItemStack> items = lootTable.getRandomItems(lootContextBuilder.create(LootContextParamSets.EMPTY))
                .stream().limit(6).collect(Collectors.toList());
        return items;
    }
}
