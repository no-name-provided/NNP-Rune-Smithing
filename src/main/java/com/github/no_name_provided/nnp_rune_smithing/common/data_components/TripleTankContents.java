package com.github.no_name_provided.nnp_rune_smithing.common.data_components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.fluids.FluidStack;

public record TripleTankContents(FluidStack tank1, FluidStack tank2, FluidStack tank3) {
    public static Codec<TripleTankContents> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    FluidStack.OPTIONAL_CODEC.fieldOf("tank1").forGetter(TripleTankContents::tank1),
                    FluidStack.OPTIONAL_CODEC.fieldOf("tank2").forGetter(TripleTankContents::tank2),
                    FluidStack.OPTIONAL_CODEC.fieldOf("tank3").forGetter(TripleTankContents::tank3)
            ).apply(instance, TripleTankContents::new)
    );
}
