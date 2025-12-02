package com.github.no_name_provided.nnp_rune_smithing.datagen.builders.recipes;

import com.github.no_name_provided.nnp_rune_smithing.common.recipes.MoldingRecipe;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class MoldingRecipeBuilder implements RecipeBuilder {
    protected final ItemStack result;
    protected final Ingredient template;
    protected final Ingredient material;
    protected final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    
    public MoldingRecipeBuilder(ItemStack result, Ingredient template, Ingredient material) {
        this.result = result;
        this.template = template;
        this.material = material;
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
        return result.getItem();
    }
    
    @Override
    public void save(RecipeOutput recipeOutput, ResourceLocation id) {
        Advancement.Builder advancement = recipeOutput.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(advancement::addCriterion);
        MoldingRecipe recipe = new MoldingRecipe(template, material, result);
        
        recipeOutput.accept(id, recipe, advancement.build(id.withPrefix("recipe/")));
    }
}
