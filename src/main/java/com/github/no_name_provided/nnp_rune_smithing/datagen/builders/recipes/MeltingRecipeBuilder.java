package com.github.no_name_provided.nnp_rune_smithing.datagen.builders.recipes;

import com.github.no_name_provided.nnp_rune_smithing.common.recipes.MeltRecipe;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class MeltingRecipeBuilder implements RecipeBuilder {
    protected final FluidStack result;
    protected final Ingredient meltable;
    protected final int meltingTemp;
    protected final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    
    public MeltingRecipeBuilder(FluidStack result, Ingredient meltable, int meltingTemp) {
        this.result = result;
        this.meltable = meltable;
        this.meltingTemp = meltingTemp;
    }
    
    @Override
    public RecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        criteria.put(name, criterion);
        return this;
    }
    
    @Override
    public RecipeBuilder group(@Nullable String groupName) {
        return this;
    }
    
    @Override
    public Item getResult() {
        return Items.AIR;
    }
    
    @Override
    public void save(RecipeOutput recipeOutput, ResourceLocation id) {
        Advancement.Builder advancement = recipeOutput.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(advancement::addCriterion);
        MeltRecipe recipe = new MeltRecipe(meltable, meltingTemp, result);
        
        recipeOutput.accept(id, recipe, advancement.build(id.withPrefix("recipe/")));
    }
}
