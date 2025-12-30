package com.github.no_name_provided.nnp_rune_smithing.common.items.runes;

import com.github.no_name_provided.nnp_rune_smithing.common.datamaps.CastableFluidData;
import com.github.no_name_provided.nnp_rune_smithing.common.datamaps.RSDataMaps;
import com.github.no_name_provided.nnp_rune_smithing.common.items.runes.molds.AbstractRuneMold;
import net.neoforged.neoforge.fluids.FluidStack;

import static com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems.AMPLIFY_RUNE;

public class BaseAmplifyRuneItem extends AbstractRuneItem {
    private final int tier;
    
    public BaseAmplifyRuneItem(Properties properties) {
        this(properties, 1);
    }
    public BaseAmplifyRuneItem(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
    }
    
    @Override
    public boolean validateFluid(FluidStack fluid) {
        CastableFluidData data = fluid.getFluidHolder().getData(RSDataMaps.CASTABLE_FLUID_DATA);
        
        return null != data && data.tier() >= tier;
    }
    
    @Override
    public Type getType() {
        return Type.AMPLIFIER;
    }
    
    public static class Mold extends AbstractRuneMold {
        public Mold(Properties properties) {
            super(properties, AMPLIFY_RUNE);
        }
    }
}
