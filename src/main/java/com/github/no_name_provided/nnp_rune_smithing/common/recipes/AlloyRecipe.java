package com.github.no_name_provided.nnp_rune_smithing.common.recipes;

import com.github.no_name_provided.nnp_rune_smithing.common.recipes.inputs.AlloyInput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import static com.github.no_name_provided.nnp_rune_smithing.common.recipes.RSRecipes.ALLOY;
import static com.github.no_name_provided.nnp_rune_smithing.common.recipes.RSRecipes.ALLOY_SERIALIZER;

public class AlloyRecipe implements Recipe<AlloyInput> {
    private final SizedFluidIngredient input1;
    private final SizedFluidIngredient input2;
    private final FluidStack result;
    
    public AlloyRecipe(SizedFluidIngredient input1, SizedFluidIngredient input2, FluidStack result) {
        this.input1 = input1;
        this.input2 = input2;
        this.result = result;
    }
    
    @Override
    public NonNullList<Ingredient> getIngredients() {
        
        return NonNullList.of(Ingredient.EMPTY);
    }
    
    @Override
    public boolean matches(AlloyInput input, Level level) {
        
        return input1.test(input.first()) && input2.test(input.second());
    }
    
    @Override
    public ItemStack assemble(AlloyInput input, HolderLookup.Provider registries) {
        
        return getResultItem(registries).copy();
    }
    
    /**
     * Used to determine if this recipe can fit in a grid of the given width/height.
     * Nothing to do with levels.
     */
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }
    
    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        
        return result.getFluid().getBucket().getDefaultInstance();
    }
    
    @Override
    public RecipeSerializer<?> getSerializer() {
        return ALLOY_SERIALIZER.get();
    }
    
    @Override
    public RecipeType<?> getType() {
        
        return ALLOY.get();
    }
    
    public SizedFluidIngredient getInput1() {
        
        return input1;
    }
    
    public SizedFluidIngredient getInput2() {
        
        return input2;
    }
    
    public FluidStack getResult() {
        
        return result;
    }
}
