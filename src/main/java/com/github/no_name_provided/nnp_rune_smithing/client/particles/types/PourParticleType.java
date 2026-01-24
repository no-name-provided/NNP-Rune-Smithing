package com.github.no_name_provided.nnp_rune_smithing.client.particles.types;

import com.github.no_name_provided.nnp_rune_smithing.client.particles.options.PourParticleOption;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Used to model a column of fluid pouring out.
 */
public class PourParticleType extends ParticleType<PourParticleOption> {
    /**
     * Constructor.
     *
     * @param overrideLimiter Should the limited particles settings disable these particles?
     */
    public PourParticleType(boolean overrideLimiter) {
        super(overrideLimiter);
    }
    
    @Override
    public MapCodec<PourParticleOption> codec() {
        
        return PourParticleOption.codec(this);
    }
    
    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, PourParticleOption> streamCodec() {
        
        return PourParticleOption.streamCodec(this);
    }
}
