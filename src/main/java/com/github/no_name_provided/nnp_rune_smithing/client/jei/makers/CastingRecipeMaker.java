package com.github.no_name_provided.nnp_rune_smithing.client.jei.makers;

import com.github.no_name_provided.nnp_rune_smithing.client.jei.fake_recipes.CastingRecipe;
import com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems;
import com.github.no_name_provided.nnp_rune_smithing.common.items.interfaces.CastingMold;

import java.util.List;

public class CastingRecipeMaker {
    public static List<CastingRecipe> makeRecipes() {
        return RSItems.ITEMS.getEntries().stream()
                .filter(holder -> holder.get() instanceof CastingMold)
                .map(holder -> new CastingRecipe((CastingMold) holder.get()))
                .toList();
    }
}
