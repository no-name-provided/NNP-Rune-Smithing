package com.github.no_name_provided.nnp_rune_smithing.client.jei.categories;

import com.github.no_name_provided.nnp_rune_smithing.client.jei.fake_recipes.CastingRecipe;
import com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems;
import com.github.no_name_provided.nnp_rune_smithing.common.items.interfaces.CastingMold;
import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

public class CastingRecipeCategory implements IRecipeCategory<CastingRecipe> {
    public static final RecipeType<CastingRecipe> TYPE =
            RecipeType.create(MODID, "casting_recipe", CastingRecipe.class);
    final IJeiHelpers helpers;
    ResourceLocation bgLocation = ResourceLocation.fromNamespaceAndPath(MODID, "textures/jei/generic.png");
    final int bgWidth = 176;
    final int bgHeight = 87;
    
    public CastingRecipeCategory(IJeiHelpers helpers) {
        this.helpers = helpers;
    }
    
    @Override
    public void draw(CastingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        RenderSystem.enableBlend();
        guiGraphics.blit(bgLocation, 0, 0, 0, 0, bgWidth, bgHeight);
        RenderSystem.disableBlend();
        
        IDrawableStatic arrow1 = helpers.getGuiHelper().getRecipeArrow();
        arrow1.draw(guiGraphics, 45, (bgHeight - arrow1.getHeight()) / 2);
        
        IDrawableStatic arrow2 = helpers.getGuiHelper().getRecipeArrow();
        arrow2.draw(guiGraphics, 107, (bgHeight - arrow1.getHeight()) / 2);
    }
    
    @Override
    public int getWidth() {
        return bgWidth;
    }
    
    @Override public int getHeight() {
        return bgHeight;
    }
    
    @Override
    public RecipeType<CastingRecipe> getRecipeType() {
        return TYPE;
    }
    
    @Override
    public Component getTitle() {
        return Component.literal("Casting");
    }
    
    @Override
    public @Nullable IDrawable getIcon() {
        return helpers.getGuiHelper().createDrawableItemStack(RSItems.ALLOYER.get().getDefaultInstance());
    }
    
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CastingRecipe recipe, IFocusGroup focuses) {
        CastingMold mold = recipe.mold;
        
        IRecipeSlotBuilder input = builder.addInputSlot().setStandardSlotBackground();
        input.setPosition(20, 0, bgWidth - 20, bgHeight, HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
        
        IRecipeSlotBuilder cast = builder.addInputSlot().setStandardSlotBackground();
        cast.setPosition(0, 0, bgWidth, bgHeight, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        if (mold instanceof Item moldItem) {
            cast.addItemStack(moldItem.getDefaultInstance());
        }
        
        IRecipeSlotBuilder output = builder.addOutputSlot().setOutputSlotBackground();
        output.setPosition(0, 0, bgWidth - 20, bgHeight, HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
        
        input.addFluidStack(recipe.fluid, mold.amountRequired());
        input.addRichTooltipCallback((view, tBuilder) ->
                tBuilder.add(Component.literal(mold.amountRequired() + " millibuckets"))
        );
        output.addItemStack(mold.getResult(new FluidStack(recipe.fluid, mold.amountRequired())));
        
        if (mold.consumed()) {
            cast.addRichTooltipCallback((view, tBuilder) ->
                    tBuilder.add(Component.literal("CONSUMED").withStyle(ChatFormatting.DARK_RED))
            );
        }
    }
}
