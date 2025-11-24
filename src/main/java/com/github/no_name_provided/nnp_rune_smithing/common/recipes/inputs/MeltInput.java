package com.github.no_name_provided.nnp_rune_smithing.common.recipes.inputs;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record MeltInput(ItemStack thingToMelt, int meltingTemp) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        if (index != 0) {
            throw new IllegalArgumentException("MeltInput Error: Items must be in first index.");
        }
        return thingToMelt();
    }
    
    @Override
    public int size() {
        return 1;
    }
}
