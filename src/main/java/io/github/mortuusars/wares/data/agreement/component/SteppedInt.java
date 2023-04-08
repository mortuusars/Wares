package io.github.mortuusars.wares.data.agreement.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Random;
import java.util.function.Function;

@SuppressWarnings("unused")
public record SteppedInt(int min, int max, int step) {
    public static final Codec<SteppedInt> CODEC = codec();

    public SteppedInt(int min, int max) {
        this(min, max, 1);
    }

    private static Codec<SteppedInt> codec() {
        Codec<SteppedInt> codec = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("min").forGetter(SteppedInt::min),
                Codec.INT.fieldOf("max").forGetter(SteppedInt::max),
                Codec.INT.optionalFieldOf("step", 1).forGetter(SteppedInt::step)
        ).apply(instance, SteppedInt::new));
        Function<SteppedInt, DataResult<SteppedInt>> validateFunc = (steppedInt) -> {
            if (steppedInt.max < steppedInt.min)
                return DataResult.error("'max' should be larger than 'min'. '" + steppedInt + "'.");
            else
                return DataResult.success(steppedInt);
        };
        // Cannot map a RecordCodecBuilder straight up.
        return codec.flatXmap(validateFunc, validateFunc);
    }

    public int sample(Random random) {
        int sample = random.nextInt(min, max + 1);
        int offset = Math.abs(sample % step);
        return offset != 0 ? Math.min(max, sample - offset + Math.abs(step)) : sample;
    }
}
