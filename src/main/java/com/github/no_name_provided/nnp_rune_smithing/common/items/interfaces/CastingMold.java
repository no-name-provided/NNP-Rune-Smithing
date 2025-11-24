package com.github.no_name_provided.nnp_rune_smithing.common.items.interfaces;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public interface CastingMold {
    /**
     * Should the mold be destroyed after a single craft or last forever?
     * @return True to destroy.
     */
    default boolean consumed() {
        return true;
    }
    
    /**
     * @return Fluid consumed per craft.
     */
    int amountRequired();
    boolean validateFluid(FluidStack fluid);
    default int coolingTime(int temperature) {
        return 20 * temperature/ 100;
    }
    ItemStack getResult(FluidStack fluid);
}
