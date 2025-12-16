package com.github.no_name_provided.nnp_rune_smithing.common.recipes.inputs;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;

public record AlloyInput(FluidStack first, FluidStack second) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return ItemStack.EMPTY;
    }
    
    @Override
    public int size() {
        return 2;
    }
    
    @Override
    public boolean isEmpty() {
        // We need two nonempty fluids, or we might as well stop here
        return first.isEmpty() || second.isEmpty();
    }
}
