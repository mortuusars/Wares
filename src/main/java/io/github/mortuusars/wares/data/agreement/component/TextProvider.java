package io.github.mortuusars.wares.data.agreement.component;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.mortuusars.wares.data.serialization.ComponentCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public record TextProvider(Either<Component, List<WeightedComponent>> provider) {
    public static final Codec<TextProvider> CODEC = Codec.either(ComponentCodec.CODEC, Codec.list(WeightedComponent.CODEC))
            .flatXmap(i -> DataResult.success(new TextProvider(i)), i -> DataResult.success(i.provider));

    public static final TextProvider EMPTY = TextProvider.of(Component.empty());

    public static TextProvider of(Component component) {
        return new TextProvider(Either.left(component));
    }

    public static TextProvider of(List<WeightedComponent> list) {
        return new TextProvider(Either.right(list));
    }

    public static TextProvider of(WeightedComponent... components) {
        return new TextProvider(Either.right(Arrays.stream(components).toList()));
    }

    public Component get(RandomSource random) {
        return provider.map(component -> component, weightedComponents -> TextProvider.fromWeightedList(weightedComponents, random));
    }

    private static Component fromWeightedList(List<WeightedComponent> weightedComponents, RandomSource random) {
        SimpleWeightedRandomList.Builder<Component> weightedList = SimpleWeightedRandomList.builder();

        for (WeightedComponent weightedComponent : weightedComponents) {
            weightedList.add(weightedComponent.component(), weightedComponent.weight().asInt());
        }

        Optional<Component> randomValue = weightedList.build().getRandomValue(random);
        if (randomValue.isEmpty()) {
            throw new IllegalStateException("No result from Weighted List. Something must be wrong. List: [" +
                    weightedComponents.stream().map(Record::toString).collect(Collectors.joining(",")));
        }

        return randomValue.get();
    }
}
