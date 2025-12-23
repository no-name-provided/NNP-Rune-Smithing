package com.github.no_name_provided.nnp_rune_smithing.client.jei.makers;

import com.github.no_name_provided.nnp_rune_smithing.client.jei.fake_recipes.CastingRecipe;
import com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems;
import com.github.no_name_provided.nnp_rune_smithing.common.items.interfaces.CastingMold;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class CastingRecipeMaker {
    public static List<CastingRecipe> makeRecipes() {
        ArrayList<CastingRecipe> recipeList = new ArrayList<>();
        RSItems.ITEMS.getEntries().stream()
                .filter(holder -> holder.get() instanceof CastingMold)
                .map(holder -> populateFluids((CastingMold) holder.get()))
                .forEach(recipeList::addAll);
        
        return recipeList;
    }
    
    private static List<CastingRecipe> populateFluids(CastingMold mold) {
        ArrayList<CastingRecipe> subList = new ArrayList<>();
        BuiltInRegistries.FLUID.stream().filter(fluid -> fluid.isSource(fluid.defaultFluidState())).forEach(fluid -> {
            if (mold.validateFluid(new FluidStack(fluid, mold.amountRequired()))) {
                subList.add(new CastingRecipe(mold, fluid));
            }
        });
        
        return subList;
    }
}
