package com.github.no_name_provided.nnp_rune_smithing.common.recipes;

import com.github.no_name_provided.nnp_rune_smithing.common.recipes.inputs.MoldingInput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import static com.github.no_name_provided.nnp_rune_smithing.common.recipes.RSRecipes.MOLDING;
import static com.github.no_name_provided.nnp_rune_smithing.common.recipes.RSRecipes.MOLDING_SERIALIZER;

public record MoldingRecipe(Ingredient template, Ingredient material, ItemStack result) implements Recipe<MoldingInput> {
    
    /**
     * @param input The input provided during registration?
     * @param level A level.
     * @return Whether the values provided during recipe registration matches the input provided during recipe lookup.
     */
    @Override
    public boolean matches(MoldingInput input, Level level) {
        return template.test(input.getItem(0)) && material().test(input.getItem(1));
    }
    
    /**
     * In vanilla, adds data to result stack and returns the actual recipe output.
     */
    @Override
    public ItemStack assemble(MoldingInput input, HolderLookup.Provider registries) {
        return getResultItem(registries).copy();
    }
    
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }
    
    /**
     * In vanilla, used as getter for result. (Recipe book?)
     */
    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result();
    }
    
    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(template());
        list.add(material());
        
        return list;
    }
    
    @Override
    public RecipeSerializer<?> getSerializer() {
        return MOLDING_SERIALIZER.get();
    }
    
    @Override
    public RecipeType<?> getType() {
        return MOLDING.get();
    }
}
