package com.github.no_name_provided.nnp_rune_smithing.client.gui;

import com.github.no_name_provided.nnp_rune_smithing.common.gui.menus.WhittlingTableMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

public class WhittlingTableScreen extends AbstractContainerScreen<WhittlingTableMenu> {
    private static final ItemStack KNOWLEDGE_BOOK = Items.KNOWLEDGE_BOOK.getDefaultInstance();
    ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/container/whittling_table_screen.png");
    
    public WhittlingTableScreen(WhittlingTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }
    
    /**
     * Renders the graphical user interface (GUI) element.
     */
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics,mouseX, mouseY);
    }
    
    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        graphics.renderFakeItem(KNOWLEDGE_BOOK, 52 + getGuiLeft(), 39 + getGuiTop());
    }
}
