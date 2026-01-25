package com.github.no_name_provided.nnp_rune_smithing.datagen.providers.numbers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

import static com.github.no_name_provided.nnp_rune_smithing.datagen.providers.numbers.RSNumbers.ONE_IN_N;

/**
 * Has an approximately one in N chance of returning 1. Otherwise, returns 0.
 * Designed for super low frequency loot. Copied from NNP Easy Farming (and relicensed).
 */
public record OneInNSubProvider(int N) implements NumberProvider {
    
    public static final MapCodec<OneInNSubProvider> CODEC = RecordCodecBuilder.mapCodec(
            inst -> inst.group(
                    Codec.INT.fieldOf("N").forGetter(OneInNSubProvider::N)
            ).apply(inst, OneInNSubProvider::new));
    
    public int N() {
        return N;
    }
    
    @Override
    public float getFloat(LootContext lootContext) {
        return RandomSource.createNewThreadLocalInstance().nextInt(N) < 1 ? 1 : 0;
    }
    
    @Override
    public LootNumberProviderType getType() {
        return ONE_IN_N.get();
    }
}
