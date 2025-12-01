package com.github.no_name_provided.nnp_rune_smithing.datagen.providers;

import com.github.no_name_provided.nnp_rune_smithing.common.items.CastingTemplate;
import com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems;
import com.github.no_name_provided.nnp_rune_smithing.common.recipes.WhittlingRecipe;
import com.github.no_name_provided.nnp_rune_smithing.datagen.builders.recipes.MeltingRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.concurrent.CompletableFuture;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;
import static com.github.no_name_provided.nnp_rune_smithing.common.fluids.FluidHelper.FLUID_SETS;
import static com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems.*;

public class Recipes extends RecipeProvider {
    public Recipes(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }
    
    @Override
    protected void buildRecipes(RecipeOutput output, HolderLookup.Provider holderLookup) {
        FLUID_SETS.forEach((set) -> {
            String name = set.type().getRegisteredName().split(":")[1];
            TagKey<Item> ingotTagKey = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "ingots/" + name));
            TagKey<Item> blockTagKey = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/" + name));
            TagKey<Item> oreTagKey = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "raw_materials/" + name));
            TagKey<Item> nuggetTagKey = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "nuggets/" + name));
            
            new MeltingRecipeBuilder(
                    new FluidStack(set.source(), 144),
                    Ingredient.of(ingotTagKey),
                    set.temperature()
            ).unlockedBy(set.type().getRegisteredName(), has(ingotTagKey))
                    .save(output, ResourceLocation.fromNamespaceAndPath(MODID, "melt/" + name + "_ingots"));
            new MeltingRecipeBuilder(
                    new FluidStack(set.source(), 1296),
                    Ingredient.of(blockTagKey),
                    set.temperature()
            ).unlockedBy(set.type().getRegisteredName(), has(blockTagKey))
                    .save(output, ResourceLocation.fromNamespaceAndPath(MODID, "melt/" + name + "_blocks"));
            new MeltingRecipeBuilder(
                    new FluidStack(set.source(), 192),
                    Ingredient.of(oreTagKey),
                    set.temperature()
            ).unlockedBy(set.type().getRegisteredName(), has(oreTagKey))
                    .save(output, ResourceLocation.fromNamespaceAndPath(MODID, "melt/" + name + "_ores"));
            new MeltingRecipeBuilder(
                    new FluidStack(set.source(), 16),
                    Ingredient.of(nuggetTagKey),
                    set.temperature()
            ).unlockedBy(set.type().getRegisteredName(), has(nuggetTagKey))
                    .save(output, ResourceLocation.fromNamespaceAndPath(MODID, "melt/" + name + "_nuggets"));
        });
        NUGGETS.getEntries().forEach(nugget -> {
            String name = nugget.getId().getPath().split("_")[0];
            Item ingot = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(MODID, name + "_ingot"));
            new ShapedRecipeBuilder(
                    RecipeCategory.MISC,
                    ingot.getDefaultInstance()
            ).pattern("XXX")
                    .pattern("XXX")
                    .pattern("XXX")
                    .define('X', nugget.get())
                    .unlockedBy("has_" + nugget.getId().getPath(), has(nugget.get()))
                    .save(output, ResourceLocation.fromNamespaceAndPath(MODID, name + "_nugget_to_ingot"));
            new ShapelessRecipeBuilder(
                    RecipeCategory.MISC,
                    nugget.get().getDefaultInstance().copyWithCount(9)
            ).requires(ingot)
                    .unlockedBy("has_" + name + "_ingot", has(ingot))
                    .save(output, name + "_ingot_to_nugget");
        });
        INGOTS.getEntries().forEach(ingot -> {
            String name = ingot.getId().getPath().split("_")[0];
            Item block = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(MODID, name + "_block"));
            new ShapedRecipeBuilder(
                    RecipeCategory.MISC,
                    block.getDefaultInstance()
            ).pattern("XXX")
                    .pattern("XXX")
                    .pattern("XXX")
                    .define('X', ingot.get())
                    .unlockedBy("has_" + ingot.getId().getPath(), has(ingot.get()))
                    .save(output, ResourceLocation.fromNamespaceAndPath(MODID, name + "_ingot_to_block"));
            new ShapelessRecipeBuilder(
                    RecipeCategory.MISC,
                    ingot.get().getDefaultInstance().copyWithCount(9)
            ).requires(block)
                    .unlockedBy("has_" + name + "_block", has(block))
                    .save(output, ResourceLocation.fromNamespaceAndPath(MODID, name + "_block_to_ingot"));
        });
        
        ITEMS.getEntries().stream()
                .filter(holder -> holder.get() instanceof CastingTemplate)
                .forEach(templateHolder -> {
                    String name = templateHolder.getId().getPath().split("_")[0];
                    new SingleItemRecipeBuilder(
                            RecipeCategory.MISC,
                            WhittlingRecipe::new,
                            Ingredient.of(templateHolder.get()),
                            templateHolder.get(),
                            1
                    ).group("template")
                            .unlockedBy("has_" + name +"_template", has(templateHolder.get()))
                            .save(output, ResourceLocation.fromNamespaceAndPath(MODID, "whittling/" + templateHolder.getId().getPath() + "_from_template"));
                    new SingleItemRecipeBuilder(
                            RecipeCategory.MISC,
                            WhittlingRecipe::new,
                            // Might break, if I ever start using the quantity of the fluid to pick the output
                            // Will crash without prefiltering
                            Ingredient.of(((CastingTemplate)templateHolder.get()).getMold().getResult(new FluidStack(FLUID_SETS.getFirst().source(), 1000)).getItem()),
                            templateHolder.get(),
                            1
                    ).group("template")
                            .unlockedBy("has_" + name + "_rune", has(Items.OAK_LOG))
                            .save(output, ResourceLocation.fromNamespaceAndPath(MODID, "whittling/" + templateHolder.getId().getPath() + "_from_rune"));
                });
        
        // One-offs start here
        
        new ShapedRecipeBuilder(
                RecipeCategory.MISC,
                RSItems.MELTER.get().getDefaultInstance()
        ).pattern("DFD")
                .pattern("DGD")
                .pattern("DHD")
                .define('D', Items.COBBLED_DEEPSLATE)
                .define('G', Items.GLASS)
                .define('F', Items.BLAST_FURNACE)
                .define('H', Items.HOPPER)
                .unlockedBy("has_blast_furnace", has(Items.BLAST_FURNACE))
                .save(output, ResourceLocation.fromNamespaceAndPath(MODID, "craft_melter"));
        new ShapedRecipeBuilder(
                RecipeCategory.MISC,
                RSItems.CASTING_TABLE.get().getDefaultInstance()
        ).pattern("DPD")
                .pattern("DSD")
                .pattern("D D")
                .define('D', Items.DEEPSLATE_BRICK_WALL)
                .define('P', Items.SMOOTH_BASALT)
                .define('S', Items.DEEPSLATE_BRICK_SLAB)
                .unlockedBy("has_smooth_basalt", has(Items.SMOOTH_BASALT))
                .save(output, ResourceLocation.fromNamespaceAndPath(MODID, "craft_casting_table"));
        new ShapedRecipeBuilder(
                RecipeCategory.MISC,
                RSItems.RUNE_ANVIL.get().getDefaultInstance()
        ).pattern("SBT")
                .pattern(" I ")
                .pattern("III")
                .define('S', Items.IRON_SWORD)
                .define('B', Tags.Items.STORAGE_BLOCKS_IRON)
                .define('T', Items.IRON_TRAPDOOR)
                .define('I', Tags.Items.INGOTS)
                .unlockedBy("has_iron_block", has(Items.IRON_BLOCK))
                .save(output, ResourceLocation.fromNamespaceAndPath(MODID, "craft_rune_anvil"));
        new ShapedRecipeBuilder(
                RecipeCategory.MISC,
                RSItems.RUNE_SMITH_HAMMER.get().getDefaultInstance()
        ).pattern("BIB")
                .pattern(" S ")
                .pattern(" S ")
                .define('B', Tags.Items.STORAGE_BLOCKS_IRON)
                .define('S', Items.STICK)
                .define('I', Tags.Items.INGOTS)
                .unlockedBy("has_iron_block", has(Items.IRON_BLOCK))
                .save(output, ResourceLocation.fromNamespaceAndPath(MODID, "craft_rune_smith_hammer"));
        new ShapedRecipeBuilder(
                RecipeCategory.MISC,
                RSItems.WHITTLING_KNIFE.get().getDefaultInstance()
        ).pattern(" I")
                .pattern("S ")
                .define('S', Items.STICK)
                .define('I', Tags.Items.INGOTS_IRON)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output, ResourceLocation.fromNamespaceAndPath(MODID, "craft_whittling_knife"));
        
        
        new MeltingRecipeBuilder(
                new FluidStack(Fluids.LAVA, 500),
                Ingredient.of(Tags.Items.COBBLESTONES_NORMAL),
                800
        ).unlockedBy("has_cobblestone", has(Tags.Items.COBBLESTONES_NORMAL))
                .save(output, ResourceLocation.fromNamespaceAndPath(MODID, "melt/cobblestone_to_lava"));
        new MeltingRecipeBuilder(
                new FluidStack(Fluids.LAVA, 500),
                Ingredient.of(Tags.Items.COBBLESTONES_DEEPSLATE),
                800
        ).unlockedBy("has_deepslate_cobblestone", has(Tags.Items.COBBLESTONES_DEEPSLATE))
                .save(output, ResourceLocation.fromNamespaceAndPath(MODID, "melt/deepslate_cobblestone_to_lava"));
        new MeltingRecipeBuilder(
                new FluidStack(Fluids.LAVA, 1000),
                Ingredient.of(Tags.Items.NETHERRACKS),
                400
        ).unlockedBy("has_netherrack", has(Tags.Items.NETHERRACKS))
                .save(output, ResourceLocation.fromNamespaceAndPath(MODID, "melt/netherrack_to_lava"));
    }
}
