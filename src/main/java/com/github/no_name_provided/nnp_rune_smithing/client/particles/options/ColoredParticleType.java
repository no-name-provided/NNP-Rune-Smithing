package com.github.no_name_provided.nnp_rune_smithing.client.particles.options;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class ColoredParticleType extends ParticleType<ColoredParticleType> implements ParticleOptions {
    private final int red;
    private final int green;
    private final int blue;
    
    private final MapCodec<ColoredParticleType> CODEC = RecordCodecBuilder.mapCodec(inst ->
        inst.group(
                Codec.BOOL.fieldOf("override_limiter").forGetter(ColoredParticleType::getOverrideLimiter),
                Codec.INT.fieldOf("red").forGetter(ColoredParticleType::getRed),
                Codec.INT.fieldOf("red").forGetter(ColoredParticleType::getRed),
                Codec.INT.fieldOf("red").forGetter(ColoredParticleType::getRed)
        ).apply(inst, ColoredParticleType::new)
    );
    
    // May not need registry friendly version here
    private final StreamCodec<RegistryFriendlyByteBuf, ColoredParticleType> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ColoredParticleType::getOverrideLimiter,
            ByteBufCodecs.INT, ColoredParticleType::getRed,
            ByteBufCodecs.INT, ColoredParticleType::getGreen,
            ByteBufCodecs.INT, ColoredParticleType::getBlue,
            ColoredParticleType::new
    );
    
    public ColoredParticleType(boolean overrideLimiter, int red, int green, int blue) {
        super(overrideLimiter);
        this.red = red;
        this.green = green;
        this.blue = blue;
    }
    
    @Override
    public ParticleType<?> getType() {
        return this;
    }
    
    @Override
    public MapCodec<ColoredParticleType> codec() {
        return CODEC;
    }
    
    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, ColoredParticleType> streamCodec() {
        return STREAM_CODEC;
    }
    
    public int getRed() {
        return red;
    }
    
    public int getGreen() {
        return green;
    }
    
    public int getBlue() {
        return blue;
    }
}
