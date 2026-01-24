package com.github.no_name_provided.nnp_rune_smithing.client.particles;

import com.github.no_name_provided.nnp_rune_smithing.client.particles.types.ColorParticleType;
import com.github.no_name_provided.nnp_rune_smithing.client.particles.types.PourParticleType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

/**
 * Registers particle types. Exposes static references to their deferred holders.
 */
public class RSParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, MODID);
    
    public static final DeferredHolder<ParticleType<?>, ColorParticleType> SELF_RUNE = PARTICLE_TYPES.register(
            "self_rune",
            () -> new ColorParticleType(false)
    );
    public static final DeferredHolder<ParticleType<?>, ColorParticleType> WIELD_RUNE = PARTICLE_TYPES.register(
            "wield_rune",
            () -> new ColorParticleType(false)
    );
    public static final DeferredHolder<ParticleType<?>, ColorParticleType> COLLISION_RUNE = PARTICLE_TYPES.register(
            "collision_rune",
            () -> new ColorParticleType(false)
    );
    
    public static final DeferredHolder<ParticleType<?>, PourParticleType> MELTER_POUR = PARTICLE_TYPES.register(
            "melter_pour",
            () -> new PourParticleType(true)
    );
    
    /**
     * Adds this deferred register to the mod event bus, so it won't be ignored. Odd default behavior, I know.
     */
    public static void register(IEventBus modBus) {
        PARTICLE_TYPES.register(modBus);
    }
}
