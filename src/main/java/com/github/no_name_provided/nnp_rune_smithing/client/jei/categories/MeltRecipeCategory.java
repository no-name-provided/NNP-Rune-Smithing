package com.github.no_name_provided.nnp_rune_smithing.client.jei.categories;

import com.github.no_name_provided.nnp_rune_smithing.common.fluids.FluidHelper;
import com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems;
import com.github.no_name_provided.nnp_rune_smithing.common.recipes.MeltRecipe;
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
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

public class MeltRecipeCategory implements IRecipeCategory<MeltRecipe> {
    public static final RecipeType<MeltRecipe> TYPE =
            RecipeType.create(MODID, "melt_recipe", MeltRecipe.class);
    IJeiHelpers helpers;
    ResourceLocation bgLocation = ResourceLocation.fromNamespaceAndPath(MODID, "textures/jei/melter.png");
    int bgWidth = 176;
    int bgHeight = 99;
    
    public MeltRecipeCategory(IJeiHelpers helper) {
        this.helpers = helper;
    }
    
    /**
     * @return the type of recipe that this category handles.
     * @since 9.5.0
     */
    @Override
    public RecipeType<MeltRecipe> getRecipeType() {
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
        return Component.literal("Melting");
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
        return helpers.getGuiHelper().createDrawableItemStack(RSItems.MELTER.get().getDefaultInstance());
    }
    
    /**
     * Sets all the recipe's ingredients by filling out an instance of {@link IRecipeLayoutBuilder}.
     * This is used by JEI for lookups, to figure out what ingredients are inputs and outputs for a recipe.
     *
     * @since 9.4.0
     */
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MeltRecipe recipe, IFocusGroup focuses) {
        IRecipeSlotBuilder inSlot = builder.addInputSlot().addItemStack(recipe.getTO_MELT().getItems()[0]).setStandardSlotBackground();
        inSlot.setPosition(30 - inSlot.getWidth()/2, (bgHeight - inSlot.getHeight())/2);
        
        IRecipeSlotBuilder outSlot = builder.addOutputSlot().addFluidStack(recipe.getRESULT().getFluid(), recipe.getRESULT().getAmount()).setOutputSlotBackground();
        outSlot.setPosition(135 - inSlot.getWidth()/2, (bgHeight - inSlot.getHeight())/2)
                .addRichTooltipCallback((view, tBuilder) ->
                        tBuilder.add(Component.literal(FluidHelper.makeQuantityTooltip(recipe.getRESULT().getAmount())))
                );
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
    public void draw(MeltRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        RenderSystem.enableBlend();
        guiGraphics.blit(bgLocation, 0, 0, 0, 0, bgWidth, bgHeight);
        IDrawableStatic arrow = helpers.getGuiHelper().getRecipeArrow();
        arrow.draw(guiGraphics, (bgWidth - arrow.getWidth())/2, (bgHeight - arrow.getHeight())/2);
        
        
        RenderSystem.disableBlend();
    }
    
    /**
     * Create per-recipe extras like {@link IRecipeWidget} and {@link IJeiInputHandler}.
     * <p>
     * These have access to a specific recipe, and will persist as long as a recipe layout is on screen,
     * so they can be used for caching and displaying recipe-specific
     * information more easily than from the recipe category directly.
     *
     * @since 19.6.0
     */
    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, MeltRecipe recipe, IFocusGroup focuses) {
        IRecipeCategory.super.createRecipeExtras(builder, recipe, focuses);
        builder.addText(FormattedText.of("Temperature: " + recipe.getMELTING_TEMP()), 100, 20).setPosition((bgWidth - 100)/2, Mth.floor((bgHeight * 0.75)));
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
