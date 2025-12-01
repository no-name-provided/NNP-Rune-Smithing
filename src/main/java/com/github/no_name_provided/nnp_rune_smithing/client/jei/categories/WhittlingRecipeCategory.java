package com.github.no_name_provided.nnp_rune_smithing.client.jei.categories;

import com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems;
import com.github.no_name_provided.nnp_rune_smithing.common.recipes.MeltRecipe;
import com.github.no_name_provided.nnp_rune_smithing.common.recipes.WhittlingRecipe;
import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

public class WhittlingRecipeCategory implements IRecipeCategory<WhittlingRecipe> {
    public static final RecipeType<WhittlingRecipe> TYPE =
            RecipeType.create(MODID, "whittling_recipe", WhittlingRecipe.class);
    IJeiHelpers helpers;
    ResourceLocation bgLocation = ResourceLocation.fromNamespaceAndPath(MODID, "textures/jei/whittling_table.png");
    int bgWidth = 179;
    int bgHeight = 86;
    
    public WhittlingRecipeCategory(IJeiHelpers helper) {
        this.helpers = helper;
    }
    
    /**
     * @return the type of recipe that this category handles.
     * @since 9.5.0
     */
    @Override
    public RecipeType<WhittlingRecipe> getRecipeType() {
        return TYPE;
    }
    
    /**
     * Returns a text component representing the name of this recipe type.
     * Drawn at the top of the recipe GUI pages for this category.
     *
     * @since 7.6.4
     */
    @Override
    public Component getTitle() {
        return Component.literal("Whittling");
    }
    
    /**
     * Icon for the category tab.
     * You can use {@link IGuiHelper#createDrawableIngredient(IIngredientType, Object)}
     * to create a drawable from an ingredient.
     * <p>
     * If null is returned here, JEI will try to use the first recipe catalyst as the icon.
     *
     * @return icon to draw on the category tab, max size is 16x16 pixels.
     */
    @Override
    public @Nullable IDrawable getIcon() {
        return helpers.getGuiHelper().createDrawableItemStack(RSItems.WHITTLING_KNIFE.get().getDefaultInstance());
    }
    
    /**
     * Sets all the recipe's ingredients by filling out an instance of {@link IRecipeLayoutBuilder}.
     * This is used by JEI for lookups, to figure out what ingredients are inputs and outputs for a recipe.
     *
     * @param builder
     * @param recipe
     * @param focuses
     * @since 9.4.0
     */
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, WhittlingRecipe recipe, IFocusGroup focuses) {
        IRecipeSlotBuilder inSlot = builder.addInputSlot().addItemStack(recipe.getIngredient().getItems()[0]).setStandardSlotBackground();
        inSlot.setPosition(143, 16);
        
        IRecipeSlotBuilder outSlot = builder.addOutputSlot().addItemStack(recipe.getResult()).setOutputSlotBackground();
        outSlot.setPosition(80, 38);
        
        IRecipeSlotBuilder woodSlot = builder.addSlot(RecipeIngredientRole.RENDER_ONLY).setStandardSlotBackground();
        woodSlot.setPosition(25, 16).addItemStack(Items.OAK_LOG.getDefaultInstance().copyWithCount(1));
        
        IRecipeSlotBuilder knifeSlot = builder.addSlot(RecipeIngredientRole.RENDER_ONLY).setStandardSlotBackground();
        knifeSlot.setPosition(25, 56).addItemStack(RSItems.WHITTLING_KNIFE.get().getDefaultInstance());
    }
    
    /**
     * Draw extras or additional info about the recipe.
     * Use the mouse position for things like button highlights.
     * Tooltips are handled by {@link #getTooltip}
     *
     * @param recipe          the current recipe being drawn.
     * @param recipeSlotsView a view of the current recipe slots being drawn.
     * @param guiGraphics     the current {@link GuiGraphics} for rendering.
     * @param mouseX          the X position of the mouse, relative to the recipe.
     * @param mouseY          the Y position of the mouse, relative to the recipe.
     * @see IDrawable for a simple class for drawing things.
     * @see IGuiHelper for useful functions.
     * @see IRecipeSlotsView for information about the ingredients that are currently being drawn.
     * @since 9.3.0
     */
    @Override
    public void draw(WhittlingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        RenderSystem.enableBlend();
        guiGraphics.blit(bgLocation, 0, 0, 0, 0, bgWidth, bgHeight);
        RenderSystem.disableBlend();
    }
    
    /**
     * Returns the width of recipe layouts that are drawn for this recipe category.
     *
     * @since 11.5.0
     */
    @Override
    public int getWidth() {
        return bgWidth;
    }
    
    /**
     * Returns the height of recipe layouts that are drawn for this recipe category.
     *
     * @since 11.5.0
     */
    @Override public int getHeight() {
        return bgHeight;
    }
}
