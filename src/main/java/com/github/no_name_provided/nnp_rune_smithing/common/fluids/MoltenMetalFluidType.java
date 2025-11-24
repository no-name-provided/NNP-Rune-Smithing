package com.github.no_name_provided.nnp_rune_smithing.common.fluids;

import com.github.no_name_provided.nnp_rune_smithing.common.items.interfaces.RuneFluidType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;

public class MoltenMetalFluidType extends FluidType implements RuneFluidType {
    public final int TIER;
    public final int COLOR_WHEN_COOL;

    public MoltenMetalFluidType(int temperature) {
        this(temperature, 1, 0);
    }
    
    public MoltenMetalFluidType(int temperature, int tier, int colorWhenCool) {
        super(Properties.create()
                .canExtinguish(false)
                .temperature(temperature) // [celsius]
                .canPushEntity(true)
                .lightLevel(8)
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
    
    @Override public int getTier() {
        return TIER;
    }
}
