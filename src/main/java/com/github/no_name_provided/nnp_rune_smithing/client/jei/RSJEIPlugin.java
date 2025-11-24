package com.github.no_name_provided.nnp_rune_smithing.client.jei;

import com.github.no_name_provided.nnp_rune_smithing.client.gui.MelterScreen;
import com.github.no_name_provided.nnp_rune_smithing.client.jei.categories.MeltRecipeCategory;
import com.github.no_name_provided.nnp_rune_smithing.common.gui.menus.MelterMenu;
import com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems;
import com.github.no_name_provided.nnp_rune_smithing.common.recipes.RSRecipes;
import com.mojang.logging.LogUtils;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;

import java.util.stream.Collectors;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;
import static com.github.no_name_provided.nnp_rune_smithing.common.gui.menus.RSMenus.MELTER_MENU;

@JeiPlugin
public class RSJEIPlugin implements IModPlugin {
    /**
     * The unique ID for this mod plugin.
     * The namespace should be your mod's modId.
     */
    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(MODID, "jei_plugin");
    }
    
    /**
     * Register the categories handled by this plugin.
     * These are registered before recipes, so they can be checked for validity.
     */
    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IModPlugin.super.registerCategories(registration);
        IJeiHelpers helpers = registration.getJeiHelpers();
        
        registration.addRecipeCategories(
                new MeltRecipeCategory(helpers)
        );
    }
    
    /**
     * Register recipe catalysts.
     * Recipe Catalysts are ingredients that are needed in order to craft other things.
     * Vanilla examples of Recipe Catalysts are the Crafting Table and Furnace.
     */
    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        IModPlugin.super.registerRecipeCatalysts(registration);
        
        registration.addRecipeCatalyst(RSItems.MELTER.get(), MeltRecipeCategory.TYPE);
    }
    
    /**
     * Register modded recipes.
     */
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        Level level = Minecraft.getInstance().level;
        if (null != level) {
            RecipeManager recipes = level.getRecipeManager();
            registration.addRecipes(
                    MeltRecipeCategory.TYPE,
                    recipes.getAllRecipesFor(RSRecipes.MELT.get())
                            .stream().map(RecipeHolder::value).collect(Collectors.toList())
            );
        } else {
            LogUtils.getLogger().error("Level cannot be null during recipe registration.");
        }
    }
    
    /**
     * Register various GUI-related things for your mod.
     * This includes adding clickable areas in your guis to open JEI,
     * and adding areas on the screen that JEI should avoid drawing.
     */
    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(
                MelterScreen.class,
                66,
                32,
                122 - 66,
                54 - 32,
                MeltRecipeCategory.TYPE
        );
    }
    
    /**
     * Register recipe transfer handlers (move ingredients from the inventory into crafting GUIs).
     */
    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(
                MelterMenu.class,
                MELTER_MENU.get(),
                MeltRecipeCategory.TYPE,
                0,
                1,
                2,
                36
        );
    }
}
