package com.github.no_name_provided.nnp_rune_smithing.client.particles.options;

import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.Fluid;

public class PourParticleOption implements ParticleOptions {
    private final ParticleType<PourParticleOption> type;
    private final Fluid fluid;
    
    public static MapCodec<PourParticleOption> codec(ParticleType<PourParticleOption> particleType) {
        
        return BuiltInRegistries.FLUID.byNameCodec().xmap(
                f -> new PourParticleOption(particleType, f),
                option -> option.fluid
        ).fieldOf("fluid");
    }
    
    public static StreamCodec<? super ByteBuf, PourParticleOption> streamCodec(ParticleType<PourParticleOption> type) {
        
        // Okay to use IDs here, since registries are frozen during the game and this isn't used for saving
        return ByteBufCodecs.INT.map(
                i -> new PourParticleOption(type, BuiltInRegistries.FLUID.byId(i)),
                option -> BuiltInRegistries.FLUID.getId(option.fluid)
        );
    }
    
    public PourParticleOption(ParticleType<PourParticleOption> type, Fluid fluid) {
        this.type = type;
        this.fluid = fluid;
    }
    
    @Override
    public ParticleType<?> getType() {
        
        return type;
    }
    
    public Fluid getFluid() {
        
        return fluid;
    }
}
