package com.github.no_name_provided.nnp_rune_smithing.common.recipes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import static com.github.no_name_provided.nnp_rune_smithing.common.recipes.RSRecipes.WHITTLING;
import static com.github.no_name_provided.nnp_rune_smithing.common.recipes.RSRecipes.WHITTLING_SERIALIZER;

public class WhittlingRecipe extends SingleItemRecipe {
    private final String group;
    private final Ingredient ingredient;
    private final ItemStack result;
    
    public WhittlingRecipe(String group, Ingredient ingredient, ItemStack result) {
        super(WHITTLING.get(), WHITTLING_SERIALIZER.get(), group, ingredient, result);
        this.result = result;
        this.ingredient = ingredient;
        this.group = group;
    }
    
    @Override
    public
    boolean matches(SingleRecipeInput input, Level level) {
        return ingredient.test(input.item());
    }
    
    @Override
    public String getGroup() {
        return group;
    }
    
    public
    Ingredient getIngredient() {
        return ingredient;
    }
    
    public
    ItemStack getResult() {
        return result;
    }
}
