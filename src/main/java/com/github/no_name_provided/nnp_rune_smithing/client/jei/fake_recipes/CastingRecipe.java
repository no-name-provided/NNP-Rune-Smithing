package com.github.no_name_provided.nnp_rune_smithing.client.jei.fake_recipes;

import com.github.no_name_provided.nnp_rune_smithing.common.items.interfaces.CastingMold;
import net.minecraft.world.level.material.Fluid;

public class CastingRecipe {
    public CastingMold mold;
    public Fluid fluid;
    public CastingRecipe(CastingMold mold, Fluid fluid) {
        this.mold = mold;
        this.fluid = fluid;
    }
}
