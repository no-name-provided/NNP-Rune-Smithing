package com.github.no_name_provided.nnp_rune_smithing.client.jei.makers;

import com.github.no_name_provided.nnp_rune_smithing.client.jei.fake_recipes.CastingRecipe;
import com.github.no_name_provided.nnp_rune_smithing.common.fluids.FluidHelper;
import com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems;
import com.github.no_name_provided.nnp_rune_smithing.common.items.interfaces.CastingMold;
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
        FluidHelper.FLUID_SETS.forEach(set -> {
            if (mold.validateFluid(new FluidStack(set.source().get(), mold.amountRequired()))) {
                subList.add(new CastingRecipe(mold, set.source().get()));
            }
        });
        
        return subList;
    }
}
