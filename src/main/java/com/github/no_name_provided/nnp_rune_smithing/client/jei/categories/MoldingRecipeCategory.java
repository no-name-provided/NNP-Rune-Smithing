package com.github.no_name_provided.nnp_rune_smithing.client.jei.categories;

import com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems;
import com.github.no_name_provided.nnp_rune_smithing.common.recipes.MoldingRecipe;
import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

public class MoldingRecipeCategory implements IRecipeCategory<MoldingRecipe> {
    public static final RecipeType<MoldingRecipe> TYPE =
            RecipeType.create(MODID, "mold_recipe", MoldingRecipe.class);
    final IJeiHelpers helpers;
    ResourceLocation bgLocation = ResourceLocation.fromNamespaceAndPath(MODID, "textures/jei/generic.png");
    final int bgWidth = 176;
    final int bgHeight = 87;
    
    public MoldingRecipeCategory(IJeiHelpers helpers) {
        this.helpers = helpers;
    }
    
    @Override
    public void draw(MoldingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        RenderSystem.enableBlend();
        guiGraphics.blit(bgLocation, 0, 0, 0, 0, bgWidth, bgHeight);
        RenderSystem.disableBlend();
    }
    
    @Override
    public int getWidth() {
        return bgWidth;
    }
    
    @Override public int getHeight() {
        return bgHeight;
    }
    
    @Override
    public RecipeType<MoldingRecipe> getRecipeType() {
        return TYPE;
    }
    
    @Override
    public Component getTitle() {
        return Component.literal("Molding");
    }
    
    @Override
    public @Nullable IDrawable getIcon() {
        return helpers.getGuiHelper().createDrawableItemStack(RSItems.CASTING_TABLE.get().getDefaultInstance());
    }
    
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MoldingRecipe recipe, IFocusGroup focuses) {
        IRecipeSlotBuilder table = builder.addSlot(RecipeIngredientRole.RENDER_ONLY).addItemStack(RSItems.CASTING_TABLE.get().getDefaultInstance());
        table.setPosition((getWidth() - table.getWidth()) / 2, getHeight() / 2 + 10);
        
        IRecipeSlotBuilder material = builder.addInputSlot().addItemStacks(Arrays.stream(recipe.material().getItems()).toList()).setStandardSlotBackground();
        material.setPosition((getWidth() - material.getWidth()) / 2, (getHeight() - material.getHeight()) / 2).addRichTooltipCallback((slotView, tooltip) -> {
            tooltip.add(Component.literal("Place in casting table and right-click with template").withStyle(ChatFormatting.BOLD));
        });
        
        builder.addSlot(RecipeIngredientRole.CATALYST, 31, 20).addItemStacks(Arrays.stream(recipe.template().getItems()).toList())
                .setStandardSlotBackground().addRichTooltipCallback((slotView, tooltip) -> {
                    tooltip.add(Component.literal("Not Consumed").withStyle(ChatFormatting.DARK_RED));
                });
        
        IRecipeSlotBuilder result = builder.addOutputSlot().setOutputSlotBackground().addItemStack(recipe.result());
        result.setPosition(0, 0, getWidth() - result.getWidth() - 5, getHeight(), HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
    }
}
