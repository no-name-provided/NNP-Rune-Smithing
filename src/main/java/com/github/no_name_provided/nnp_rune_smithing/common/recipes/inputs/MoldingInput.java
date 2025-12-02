package com.github.no_name_provided.nnp_rune_smithing.common.recipes.inputs;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;

public record MoldingInput(Ingredient template, Ingredient material) implements RecipeInput {
    
    @Override
    public ItemStack getItem(int index) {
        return switch(index) {
            case 0 -> template().getItems()[0];
            case 1 -> material().getItems()[0];
            default -> throw new IllegalArgumentException("Index out of bounds [0, 1].");
        };
    }
    
    @Override
    public int size() {
        return 2;
    }
}
