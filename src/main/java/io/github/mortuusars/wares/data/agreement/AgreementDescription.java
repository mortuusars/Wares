package io.github.mortuusars.wares.data.agreement;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public record AgreementDescription(Optional<TextProvider> buyerName,
                                   Optional<TextProvider> buyerAddress,
                                   Optional<TextProvider> title,
                                   Optional<TextProvider> message,
                                   Either<String, List<ItemStack>> requested,
                                   Either<String, List<ItemStack>> payment,
                                   Either<Integer, SteppedInt> ordered,
                                   Either<Integer, SteppedInt> experience,
                                   Either<Integer, SteppedInt> deliveryTime,
                                   Either<Integer, SteppedInt> expiresInSeconds) {
    public static final Codec<AgreementDescription> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TextProvider.CODEC.optionalFieldOf("buyerName").forGetter(AgreementDescription::buyerName),
            TextProvider.CODEC.optionalFieldOf("buyerAddress").forGetter(AgreementDescription::buyerAddress),
            TextProvider.CODEC.optionalFieldOf("title").forGetter(AgreementDescription::title),
            TextProvider.CODEC.optionalFieldOf("message").forGetter(AgreementDescription::message),
            Codec.either(Codec.STRING, Codec.list(ItemStack.CODEC)).fieldOf("requested").forGetter(AgreementDescription::requested),
            Codec.either(Codec.STRING, Codec.list(ItemStack.CODEC)).fieldOf("payment").forGetter(AgreementDescription::payment),
            Codec.either(Codec.INT, SteppedInt.CODEC).optionalFieldOf("ordered", Either.left(-1)).forGetter(AgreementDescription::ordered),
            Codec.either(Codec.INT, SteppedInt.CODEC).optionalFieldOf("experience", Either.left(-1)).forGetter(AgreementDescription::experience),
            Codec.either(Codec.INT, SteppedInt.CODEC).optionalFieldOf("deliveryTime", Either.left(-1)).forGetter(AgreementDescription::deliveryTime),
            Codec.either(Codec.INT, SteppedInt.CODEC).optionalFieldOf("expiresInSeconds", Either.left(-1)).forGetter(AgreementDescription::expiresInSeconds)
        ).apply(instance, AgreementDescription::new));

    public Agreement realize(ServerLevel level) {
        Random random = level.getRandom();

        int quantity = ordered.map(integer -> integer, steppedInt -> steppedInt.sample(random));

        int expiresIn = expiresInSeconds.map(integer -> integer, steppedInt -> steppedInt.sample(random));
        long expireTime = level.getGameTime() + expiresIn * 20L;

        Agreement agreement = new Agreement(
                buyerName.map(provider -> provider.get(random)),
                buyerAddress.map(provider -> provider.get(random)),
                title.map(provider -> provider.get(random)),
                message.map(provider -> provider.get(random)),
                getStacks(requested, level),
                getStacks(payment, level),
                quantity,
                quantity,
                experience.map(integer -> integer, steppedInt -> steppedInt.sample(random)),
                deliveryTime.map(integer -> integer, steppedInt -> steppedInt.sample(random)),
                expireTime);

        return agreement;
    }

    private List<ItemStack> getStacks(Either<String, List<ItemStack>> tableOrItems, ServerLevel level) {
        return tableOrItems.map(path -> fromLootTable(new ResourceLocation(path), level), list -> list);
    }

    private List<ItemStack> fromLootTable(ResourceLocation lootTablePath, ServerLevel level) {
        LootTable lootTable = level.getServer().getLootTables().get(lootTablePath);
        LootContext.Builder lootContextBuilder = new LootContext.Builder(level);
        List<ItemStack> items = lootTable.getRandomItems(lootContextBuilder.create(LootContextParamSets.EMPTY));
        return items;
    }
}
