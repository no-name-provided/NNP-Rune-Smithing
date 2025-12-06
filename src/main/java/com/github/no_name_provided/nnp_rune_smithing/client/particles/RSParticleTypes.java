package com.github.no_name_provided.nnp_rune_smithing.client.particles;

import com.github.no_name_provided.nnp_rune_smithing.client.particles.options.ColoredParticleType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

public class RSParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, MODID);
    
    public static final DeferredHolder<ParticleType<?>, ColoredParticleType> SELF_RUNE = PARTICLE_TYPES.register(
            "self_rune",
            () -> new ColoredParticleType(false, 0, 0, 0)
    );
    public static final DeferredHolder<ParticleType<?>, ColoredParticleType> WIELD_RUNE = PARTICLE_TYPES.register(
            "wield_rune",
            () -> new ColoredParticleType(false, 0, 0, 0)
    );
    public static final DeferredHolder<ParticleType<?>, ColoredParticleType> COLLISION_RUNE = PARTICLE_TYPES.register(
            "collision_rune",
            () -> new ColoredParticleType(false, 0, 0, 0)
    );
    
    
    public static void register(IEventBus modBus) {
        PARTICLE_TYPES.register(modBus);
    }
}
