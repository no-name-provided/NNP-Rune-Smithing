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
     * Calculate the amount of fluid consumed per craft.
     * @return Fluid consumed per craft [mB].
     */
    int amountRequired();
    
    /**
     * Verify the fluid is valid.
     * @param fluid The fluid stack being validated.
     * @return True if a recipe can be evaluated, otherwise false.
     */
    boolean validateFluid(FluidStack fluid);
    
    /**
     * The time this recipe requires.
     * @param temperature The original temperature of the fluid. Typically, its melting point.
     * @return The time, in ticks, required to convert a fluid at this temperature to
     * a corresponding item.
     */
    default int coolingTime(int temperature) {
        return 20 * temperature/ 100;
    }
    
    /**
     * May log error on invalid recipe, so try not to pass in values before validation.
     * @return Itemstack created by pouring fluid into this mold.
     */
    ItemStack getResult(FluidStack fluid);
}
