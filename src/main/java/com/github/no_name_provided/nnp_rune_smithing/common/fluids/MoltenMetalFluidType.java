package com.github.no_name_provided.nnp_rune_smithing.common.fluids;

import com.github.no_name_provided.nnp_rune_smithing.common.RSServerConfig;
import com.github.no_name_provided.nnp_rune_smithing.common.items.interfaces.RuneFluidType;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;

public class MoltenMetalFluidType extends FluidType implements RuneFluidType {
    public final int TIER;
    public final int COLOR_WHEN_COOL;

    @SuppressWarnings("unused") // Convenience constructor
    public MoltenMetalFluidType(int temperature) {
        this(temperature, 1, 0);
    }
    
    public MoltenMetalFluidType(int temperature, int tier, int colorWhenCool) {
        super(Properties.create()
                .canExtinguish(false)
                .temperature(temperature) // [celsius]
                .canPushEntity(true)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
                .fallDistanceModifier(0.5f)
                .density(8000) // [kg/m^3]
                .viscosity(30)
                .supportsBoating(false)
                .canSwim(false)
                .canHydrate(false)
                .canDrown(true)
                .pathType(PathType.LAVA)
        );
        TIER = tier;
        COLOR_WHEN_COOL = colorWhenCool;
    }
    
    @Override
    public int getTier() {
        
        return TIER;
    }
    
    @Override
    public int getLightLevel() {
        // Should probably look up realistic emissions at some point and make a proper map
        return Mth.clamp(getTemperature() / 100, 0, 15);
    }
    
    @Override
    public boolean canConvertToSource(FluidState state, LevelReader reader, BlockPos pos) {
        
        return RSServerConfig.moltenMetalFluidsAreInfinite;
    }
    
    /**
     * Ordinarily, this hook is intended to adjust living entity movement. However, we're using it to add a ticking
     * effect for entities touching this fluid.
     */
    @Override
    public boolean move(FluidState state, LivingEntity entity, Vec3 movementVector, double gravity) {
        entity.lavaHurt();
        
        return super.move(state, entity, movementVector, gravity);
    }
}
