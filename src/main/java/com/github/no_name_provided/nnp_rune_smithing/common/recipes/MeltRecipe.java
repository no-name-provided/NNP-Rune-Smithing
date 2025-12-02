package com.github.no_name_provided.nnp_rune_smithing.common.recipes;

import com.github.no_name_provided.nnp_rune_smithing.common.recipes.inputs.MeltInput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import static com.github.no_name_provided.nnp_rune_smithing.common.recipes.RSRecipes.MELT;
import static com.github.no_name_provided.nnp_rune_smithing.common.recipes.RSRecipes.MELT_SERIALIZER;

public class MeltRecipe implements Recipe<MeltInput> {
    private final Ingredient TO_MELT;
    private final int MELTING_TEMP;
    private final FluidStack RESULT;
    
    public MeltRecipe(Ingredient toMelt, int meltingTemp, FluidStack result) {
        TO_MELT = toMelt;
        MELTING_TEMP = meltingTemp;
        RESULT = result;
    }
    
    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(TO_MELT);
    }
    
    @Override
    public boolean matches(MeltInput input, Level level) {
        return !TO_MELT.hasNoItems() && ItemStack.matches(TO_MELT.getItems()[0], input.thingToMelt()) && input.meltingTemp() > 0;
    }
    
    @Override
    public ItemStack assemble(MeltInput input, HolderLookup.Provider registries) {
        return getResultItem(registries).copy();
    }
    
    /**
     * Used to determine if this recipe can fit in a grid of the given width/height
     *
     * @param width
     * @param height
     */
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }
    
    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return RESULT.getFluid().getBucket().getDefaultInstance();
//        Optional<Holder.Reference<Item>> result = registries.lookupOrThrow(Registries.ITEM).listElements().filter((item) -> item.value() instanceof BucketItem bucket && bucket.content.getFluidType() == type).findFirst();
//        return result.map(itemReference -> itemReference.value().getDefaultInstance()).orElse(ItemStack.EMPTY);

//        Optional<FluidHelper.FluidSet> fluid = FLUID_SETS.stream().filter((set) -> set.type().get() == RESULT.getFluidType()).findFirst();
//        return fluid.map(fluidSet -> fluidSet.bucket().get().getDefaultInstance()).orElse(ItemStack.EMPTY);
    }
    
    @Override
    public RecipeSerializer<?> getSerializer() {
        return MELT_SERIALIZER.get();
    }
    
    @Override
    public RecipeType<?> getType() {
        return MELT.get();
    }
    
    public Ingredient getTO_MELT() {
        return TO_MELT;
    }
    
    public int getMELTING_TEMP() {
        return MELTING_TEMP;
    }
    
    public FluidStack getRESULT() {
        return RESULT;
    }
}
