package com.github.no_name_provided.nnp_rune_smithing.common.datamaps;

import com.github.no_name_provided.nnp_rune_smithing.common.fluids.FluidHelper.SolidEquivalents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record CastableFluidData(int colorWhenCool, int tier, Optional<SolidEquivalents> solidEquivalents) {
    
    public static final Codec<CastableFluidData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("colorWhenCool").forGetter(CastableFluidData::colorWhenCool),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("tier").forGetter(CastableFluidData::tier),
            SolidEquivalents.CODEC.optionalFieldOf("solid_equivalents").forGetter(CastableFluidData::solidEquivalents)
    ).apply(instance, CastableFluidData::new));
}
