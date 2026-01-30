package com.github.no_name_provided.nnp_rune_smithing.client.jei.makers;

import com.github.no_name_provided.nnp_rune_smithing.client.jei.fake_recipes.CastingRecipe;
import com.github.no_name_provided.nnp_rune_smithing.common.fluids.FluidHelper;
import com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems;
import com.github.no_name_provided.nnp_rune_smithing.common.items.interfaces.CastingMold;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CastingRecipeMaker {
    
    /**
     * Gets, filters, and maps a list of all fluids to their associated recipes. Required because we
     * didn't bother setting up a "proper" recipe system for casting.
     */
    public static List<CastingRecipe> makeRecipes() {
        ArrayList<CastingRecipe> recipeList = new ArrayList<>();
        RSItems.ITEMS.getEntries().stream()
                .filter(holder -> holder.get() instanceof CastingMold)
                .map(holder -> populateFluids((CastingMold) holder.get()))
                .forEach(recipeList::addAll);
        
        return recipeList;
    }
    
    /**
     * Iterates over the casting molds in our deferred register and tests them against each fluid, returning a list of
     * valid casting (JEI fake) recipes.
     */
    private static List<CastingRecipe> populateFluids(CastingMold mold) {
        ArrayList<CastingRecipe> subList = new ArrayList<>();
        BuiltInRegistries.FLUID.stream()
                .filter(fluid -> fluid.isSource(fluid.defaultFluidState())
                ).filter(fluid -> {
                    // Filter out fluids for mods that aren't loaded, so they don't show up when looking at rune recipes
                    AtomicBoolean mayBeUsed = new AtomicBoolean(true);
                    FluidHelper.HIDDEN_IN_JEI_IF_MOD_NOT_PRESENT
                            .forEach((modid, locList) -> {
                                // Don't un-disable if it's already failed a check
                                if (!ModList.get().isLoaded(modid) && mayBeUsed.get()) {
                                    mayBeUsed.set(!locList.contains(BuiltInRegistries.FLUID.getKey(fluid)));
                                }
                            });
                    
                    return mayBeUsed.get();
                }).forEach(fluid -> {
                    if (mold.validateFluid(new FluidStack(fluid, mold.amountRequired()))) {
                        subList.add(new CastingRecipe(mold, fluid));
                    }
                });
        
        return subList;
    }
}
