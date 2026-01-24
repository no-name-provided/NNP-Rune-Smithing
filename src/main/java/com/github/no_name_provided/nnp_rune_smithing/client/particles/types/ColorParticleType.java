package com.github.no_name_provided.nnp_rune_smithing.client.particles.types;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Right now, this class is largely superfluous. It acts as a place where I can inject custom parameters into my colored
 * particles. It also increases symmetry with the documentation.
 */
public class ColorParticleType extends ParticleType<ColorParticleOption> {
    /**
     * Constructor.
     *
     * @param overrideLimiter Should the limited particles settings disable these particles?
     */
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
