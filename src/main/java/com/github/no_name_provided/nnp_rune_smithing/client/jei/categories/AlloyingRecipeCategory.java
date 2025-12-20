package com.github.no_name_provided.nnp_rune_smithing.client.jei.categories;

import com.github.no_name_provided.nnp_rune_smithing.common.fluids.FluidHelper;
import com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems;
import com.github.no_name_provided.nnp_rune_smithing.common.recipes.AlloyRecipe;
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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

public class AlloyingRecipeCategory implements IRecipeCategory<AlloyRecipe> {
    public static final RecipeType<AlloyRecipe> TYPE =
            RecipeType.create(MODID, "alloy_recipe", AlloyRecipe.class);
    final IJeiHelpers helpers;
    ResourceLocation bgLocation = ResourceLocation.fromNamespaceAndPath(MODID, "textures/jei/generic.png");
    final int bgWidth = 176;
    final int bgHeight = 87;
    
    public AlloyingRecipeCategory(IJeiHelpers helpers) {
        this.helpers = helpers;
    }
    
    @Override
    public void draw(AlloyRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        RenderSystem.enableBlend();
        guiGraphics.blit(bgLocation, 0, 0, 0, 0, bgWidth, bgHeight);
        RenderSystem.disableBlend();
        
        IDrawableStatic arrow = helpers.getGuiHelper().getRecipeArrow();
        arrow.draw(guiGraphics, 45, (bgHeight - arrow.getHeight()) / 2);
        IDrawableStatic leftArrow = helpers.getGuiHelper().drawableBuilder(
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/jei/left_recipe_arrow.png"),
                22,
                16,
                22,
                16
        ).setTextureSize(22, 16).build();
        leftArrow.draw(guiGraphics, 107, (bgHeight - arrow.getHeight()) / 2);
    }
    
    @Override
    public int getWidth() {
        return bgWidth;
    }
    
    @Override public int getHeight() {
        return bgHeight;
    }
    
    @Override
    public RecipeType<AlloyRecipe> getRecipeType() {
        return TYPE;
    }
    
    @Override
    public Component getTitle() {
        return Component.literal("Alloying");
    }
    
    @Override
    public @Nullable IDrawable getIcon() {
        return helpers.getGuiHelper().createDrawableItemStack(RSItems.ALLOYER.get().getDefaultInstance());
    }
    
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AlloyRecipe recipe, IFocusGroup focuses) {
        IRecipeSlotBuilder input1 = builder.addInputSlot().setStandardSlotBackground();
        for (FluidStack fluid : recipe.getInput1().getFluids()) {
            input1.addFluidStack(fluid.getFluid(), fluid.getAmount())
                    .addRichTooltipCallback((view, tBuilder) ->
                            tBuilder.add(Component.literal(FluidHelper.makeQuantityTooltip(fluid.getAmount())))
                    );
        }
        input1.setPosition(20, 0, getWidth() - 20, getHeight(), HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
        ;
        
        IRecipeSlotBuilder input2 = builder.addInputSlot().setStandardSlotBackground();
        for (FluidStack fluid : recipe.getInput2().getFluids()) {
            input2.addFluidStack(fluid.getFluid(), fluid.getAmount())
                    .addRichTooltipCallback((view, tBuilder) ->
                            tBuilder.add(Component.literal(FluidHelper.makeQuantityTooltip(fluid.getAmount())))
                    );
        }
        input2.setPosition(0, 0, getWidth() - 20, getHeight(), HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
        
        builder.addOutputSlot().setOutputSlotBackground()
                .addFluidStack(recipe.getResult().getFluid(), recipe.getResult().getAmount())
                .addRichTooltipCallback((view, tBuilder) ->
                        tBuilder.add(Component.literal(FluidHelper.makeQuantityTooltip(recipe.getResult().getAmount())))
                ).setPosition(0, 0, getWidth(), getHeight(), HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }
}
