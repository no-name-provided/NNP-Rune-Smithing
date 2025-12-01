package com.github.no_name_provided.nnp_rune_smithing.common.items;

import com.github.no_name_provided.nnp_rune_smithing.common.items.interfaces.CastingMold;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public class BlankMold extends Item implements CastingMold {
    public BlankMold(Properties properties) {
        super(properties);
    }
    
    /**
     * @return Fluid consumed per craft.
     */
    @Override
    public int amountRequired() {
        return 0;
    }
    
    @Override
    public boolean validateFluid(FluidStack fluid) {
        return false;
    }
    
    @Override
    public ItemStack getResult(FluidStack fluid) {
        return ItemStack.EMPTY;
    }
}
