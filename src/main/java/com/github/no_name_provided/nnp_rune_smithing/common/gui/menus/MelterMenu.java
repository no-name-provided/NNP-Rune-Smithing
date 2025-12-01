package com.github.no_name_provided.nnp_rune_smithing.common.gui.menus;

import com.github.no_name_provided.nnp_rune_smithing.common.entities.MelterBlockEntity;
import com.github.no_name_provided.nnp_rune_smithing.common.recipes.MeltRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import static com.github.no_name_provided.nnp_rune_smithing.common.gui.menus.RSMenus.MELTER_MENU;
import static com.github.no_name_provided.nnp_rune_smithing.common.recipes.RSRecipes.MELT;

public class MelterMenu extends AbstractContainerMenu {
    public static final int DATA_COUNT = 7;
    private final Container CONTAINER;
    public final MelterBlockEntity.MelterContainerData DATA;
    protected final Level LEVEL;
    private final RecipeType<MeltRecipe> RECIPE_TYPE;
    private final RecipeBookType RECIPE_BOOK_TYPE;
    
    BlockPos POSITION;
    
    public void setFluid(FluidStack fluid) {
        this.fluid = fluid;
    }
    
    public FluidStack fluid;
    

    // Client menu constructor
    public MelterMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(
                containerId,
                playerInventory,
                pos,
                new MelterBlockEntity.MelterContainerData(DATA_COUNT, (MelterBlockEntity) playerInventory.player.level().getBlockEntity(pos)),
                // Default values passed to client must have same size, or there's a soft crash and the debugger breaks
                new SimpleContainer(ItemStack.EMPTY, ItemStack.EMPTY));
    }
    //Server menu constructor
    public MelterMenu(int containerId, Inventory playerInventory, BlockPos pos, MelterBlockEntity.MelterContainerData data,
                      Container container) {
        super(MELTER_MENU.get(), containerId);
        LEVEL = playerInventory.player.level();
        CONTAINER = container;
        DATA = data;
        RECIPE_TYPE = MELT.get();
        RECIPE_BOOK_TYPE = RecipeBookType.FURNACE;
        POSITION = pos;

        addDataSlots(data);
        addSlot(new Slot(container, 0, 36, 18));
        addSlot(new Slot(container, 1, 36, 56));
        addPlayerInventory(playerInventory);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // Slots are 0-indexed. Human math is not. Remember to only include one boundary value when sizing each range.
        // The last index isn't included, so it should be the start index plus the inventory size. The first index is
        // included, so it should be the first index in the inventory.

        int input = 0;
        int fuelEnd = input + 1;
        int playerInvStart = fuelEnd + 1;
        int playerInvEnd = playerInvStart + 27;
        int useSlotEnd = playerInvEnd + 9;
        Slot slot = slots.get(index);

        ItemStack stackToMove = ItemStack.EMPTY;
        if (slot.hasItem()) {
            ItemStack rawStack = slot.getItem();
            stackToMove = rawStack.copy();
            if (index <= fuelEnd) {
                // moveItemStackTo includes the first index, but doesn't include the second index in its range.
                if (!moveItemStackTo(rawStack, playerInvStart, useSlotEnd, false)) {
                    
                    return ItemStack.EMPTY;
                }
            } else { //Slot is greater than playerInvStart
                if (!moveItemStackTo(rawStack, 0, playerInvStart, false)) {
                    
                    return ItemStack.EMPTY;
                }
            }
            if (rawStack.getCount() != stackToMove.getCount()) {
                slot.setChanged();
                stackToMove = rawStack;
            }
        }
        
        return stackToMove;
    }
    
    @Override
    public boolean stillValid(Player player) {
        // TODO: Should use container access utility or look at interaction distance modifiers
        return player.blockPosition().distManhattan(POSITION) < 10;
    }
    /**Add the player's inventory as slots on the bottom of the GUI.
     * Offsets are based on background image pixel positions.*/
    public void addPlayerInventory(Inventory playerInv) {
        int playerInvOffset = 2 * 18;

        int row_index;
        int column_index;

        for(row_index = 0; row_index < 3; ++row_index) {
            for(column_index = 0; column_index < 9; column_index++) {
                this.addSlot(new Slot(playerInv, column_index + row_index * 9 + 9, 8 + column_index * 18, 48 + row_index * 18 + playerInvOffset));
            }
        }

        for(column_index = 0; column_index < 9; column_index++) {
            this.addSlot(new Slot(playerInv, column_index, 8 + column_index * 18, 106 + playerInvOffset));
        }
    }
}
