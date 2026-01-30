package com.github.no_name_provided.nnp_rune_smithing.datagen.builders.recipes;

import com.github.no_name_provided.nnp_rune_smithing.common.recipes.AlloyRecipe;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class AlloyRecipeBuilder implements RecipeBuilder {
    protected final FluidStack result;
    protected final SizedFluidIngredient input1;
    protected final SizedFluidIngredient input2;
    protected final ICondition[] loadConditions;
    protected final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    
    public AlloyRecipeBuilder(FluidStack result, SizedFluidIngredient input1, SizedFluidIngredient input2, ICondition[] loadConditions) {
        this.result = result;
        this.input1 = input1;
        this.input2 = input2;
        this.loadConditions = loadConditions;
    }
    
    /**
     * Constructor for a NeoForge load condition free instance of this recipe (builder).
     */
    public AlloyRecipeBuilder(FluidStack result, SizedFluidIngredient input1, SizedFluidIngredient input2) {
        this(result, input1, input2, new ICondition[0]);
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
        return result.getFluid().getBucket();
    }
    
    @Override
    public void save(RecipeOutput recipeOutput, ResourceLocation id) {
        Advancement.Builder advancement = recipeOutput.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(advancement::addCriterion);
        AlloyRecipe recipe = new AlloyRecipe(input1, input2, result);
        
        if (loadConditions.length != 0) {
            recipeOutput = recipeOutput.withConditions(loadConditions);
        }
        recipeOutput.accept(id, recipe, advancement.build(id.withPrefix("recipe/")));
    }
}
