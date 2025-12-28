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

import static com.github.no_name_provided.nnp_rune_smithing.common.entities.WhittlingTableBlockEntity.INVENTORY_SIZE;
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
                new ItemStackHandler(INVENTORY_SIZE)
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
        // Material slot
        this.addSlot(new SlotItemHandler(inv, 0, 25, 16));
        // Knife slot
        this.addSlot(new SlotItemHandler(inv, 1, 25, 56));
        // Pattern slot
        this.addSlot(new SlotItemHandler(inv, 2, 143, 16));
        // Output slot
        this.addSlot(new SlotItemHandler(inv, 3, 80, 38));
        
//        for(int row_index = 0; row_index < 9; ++row_index) {
//            for(int column_index = 0; column_index < 3; ++column_index) {
//                this.addSlot(new SlotItemHandler(inv, column_index + row_index * 3 + 4, 178 + column_index * 18, 5 + row_index * 18));
//            }
//        }
    }
    
    /**
     * Handle when the stack in slot {@code index} is shift-clicked.
     * Normally this moves the stack between the player inventory and the other inventory(s).
     * <p>
     *     We also need to update output slot after shift-click, since this
     *     bypasses the item stack handler's methods
     * </p>
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // Slots are 0-indexed. Human math is not. Remember to only include one boundary value when sizing each range.
        // The last index isn't included, so it should be the start index plus the inventory size. The first index is
        // included, so it should be the first index in the inventory.
        
        int inventoryStart = 0;
        int inventoryEnd = inventoryStart + INVENTORY_SIZE;
        int playerInvStart = inventoryEnd + 1;
        int playerInvEnd = playerInvStart + 27;
        int useSlotEnd = playerInvEnd + 8; //?
        
        Slot slot = slots.get(index);
        
        ItemStack stackToMove = ItemStack.EMPTY;
        
        if (slot.hasItem()) {
            ItemStack rawStack = slot.getItem();
            stackToMove = rawStack.copy();
            if (index <= inventoryEnd) {
                // moveItemStackTo includes the first index, but doesn't include the second index in its range.
                if (!moveItemStackTo(rawStack, playerInvStart, useSlotEnd, false)) {
                    
                    // This forces an update. Otherwise, I'd have to make a proper subclass and expose private methods
                    INVENTORY.setStackInSlot(0, INVENTORY.getStackInSlot(0));
                    return ItemStack.EMPTY;
                }
            } else { //Slot is greater than playerInvStart
                // Split this up into two sections, so we can skip inserting into the result slot
                if (!moveItemStackTo(rawStack, 0, 3, false)) {
                    
                    INVENTORY.setStackInSlot(0, INVENTORY.getStackInSlot(0));
                    return ItemStack.EMPTY;
                } else if (!moveItemStackTo(rawStack, 4, playerInvStart, false)) {
                    
                    INVENTORY.setStackInSlot(0, INVENTORY.getStackInSlot(0));
                    return ItemStack.EMPTY;
                }
            }
            if (rawStack.getCount() != stackToMove.getCount()) {
                slot.setChanged();
                stackToMove = rawStack;
            }
        }
        
        INVENTORY.setStackInSlot(0, INVENTORY.getStackInSlot(0));
        return stackToMove;
    }
    
    /**
     * Determines whether supplied player can use this container
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
