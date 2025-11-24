package com.github.no_name_provided.nnp_rune_smithing.common.items;

import com.github.no_name_provided.nnp_rune_smithing.common.items.interfaces.CastingMold;
import net.neoforged.neoforge.fluids.FluidStack;

public abstract class RuneMold implements CastingMold {
    public int Tier;
    
    @Override
    public int amountRequired() {
        return 0;
    }
    
    @Override
    public boolean validateFluid(FluidStack fluid) {
        return false;
    }
}
