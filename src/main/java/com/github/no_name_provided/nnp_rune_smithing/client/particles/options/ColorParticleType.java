package com.github.no_name_provided.nnp_rune_smithing.client.particles.options;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class ColorParticleType extends ParticleType<ColorParticleOption> {
    
    public ColorParticleType(boolean overrideLimiter) {
        super(overrideLimiter);
    }
    
    @Override
    public MapCodec<ColorParticleOption> codec() {
        return ColorParticleOption.codec(this);
    }
    
    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, ColorParticleOption> streamCodec() {
        return ColorParticleOption.streamCodec(this);
    }
}
