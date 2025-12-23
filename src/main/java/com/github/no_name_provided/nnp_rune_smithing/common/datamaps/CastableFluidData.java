package com.github.no_name_provided.nnp_rune_smithing.common.datamaps;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CastableFluidData(int colorWhenCool, int tier) {
    
    public static final Codec<CastableFluidData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("colorWhenCool").forGetter(CastableFluidData::colorWhenCool),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("tier").forGetter(CastableFluidData::tier)
    ).apply(instance, CastableFluidData::new));
    
}
