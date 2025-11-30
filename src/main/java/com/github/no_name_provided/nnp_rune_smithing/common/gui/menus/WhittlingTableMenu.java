package com.github.no_name_provided.nnp_rune_smithing.common.gui.menus;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import static com.github.no_name_provided.nnp_rune_smithing.common.gui.menus.RSMenus.WHITTLING_TABLE_MENU;

public class WhittlingTableMenu  extends AbstractContainerMenu {
    private static Level LEVEL = null;
    private static BlockPos POSITION = BlockPos.ZERO;
    public final ItemStackHandler INVENTORY;
    
    // Client menu constructor
    public WhittlingTableMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(
                containerId,
                playerInventory,
                pos,
                // Default values passed to client must have same size, or there's a soft crash and the debugger breaks
                new ItemStackHandler(30)
                );
    }
    //Server menu constructor
    public WhittlingTableMenu(int containerId, Inventory playerInventory, BlockPos pos, ItemStackHandler inventory) {
        super(WHITTLING_TABLE_MENU.get(), containerId);
        LEVEL = playerInventory.player.level();
        INVENTORY = inventory;
        POSITION = pos;
        
        addItemStackHandlerSlots(inventory);
        addPlayerInventorySlots(playerInventory);
    }
    
    private void addPlayerInventorySlots(Inventory playerInv) {
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
    
    private void addItemStackHandlerSlots(ItemStackHandler inv) {
        int playerInvOffset = 2 * 18;
        
        int row_index;
        int column_index;
        
        for(row_index = 0; row_index < 3; ++row_index) {
            for(column_index = 0; column_index < 9; column_index++) {
                this.addSlot(new SlotItemHandler(inv, column_index + row_index * 9 + 9, 8 + column_index * 18, 48 + row_index * 18 + playerInvOffset));
            }
        }
        
        for(column_index = 0; column_index < 9; column_index++) {
            this.addSlot(new SlotItemHandler(inv, column_index, 8 + column_index * 18, 106 + playerInvOffset));
        }
    }
    
    /**
     * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player inventory and the other inventory(s).
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // Slots are 0-indexed. Human math is not. Remember to only include one boundary value when sizing each range.
        // The last index isn't included, so it should be the start index plus the inventory size. The first index is
        // included, so it should be the first index in the inventory.
        
        int inventoryStart = 0;
        int inventoryEnd = inventoryStart + 30;
        int playerInvStart = inventoryEnd + 1;
        int playerInvEnd = playerInvStart + 27;
        int useSlotEnd = playerInvEnd + 9;
        
        Slot slot = slots.get(index);
        
        ItemStack stackToMove = ItemStack.EMPTY;
        if (slot.hasItem()) {
            ItemStack rawStack = slot.getItem();
            stackToMove = rawStack.copy();
            if (index <= inventoryEnd) {
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
    
    /**
     * Determines whether supplied player can use this container
     *
     * @param player
     */
    @Override
    public boolean stillValid(Player player) {
        BlockEntity be = LEVEL.getBlockEntity(POSITION);
        if (null != be) {
         
            return Container.stillValidBlockEntity(be, player);
        }
        
        return false;
    }
}
