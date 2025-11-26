package com.github.no_name_provided.nnp_rune_smithing.common.capabilities;

import com.github.no_name_provided.nnp_rune_smithing.common.entities.CastingTableBlockEntity;
import com.github.no_name_provided.nnp_rune_smithing.common.items.interfaces.CastingMold;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

public class CastingTableCapability {
    public static class CastingTableFluidCapability implements IFluidHandler {
        CastingTableBlockEntity be;
        
        public CastingTableFluidCapability(CastingTableBlockEntity be) {
            this.be = be;
        }
        
        @Override
        public int getTanks() {
            return 1;
        }
        
        @Override
        public FluidStack getFluidInTank(int tank) {
            return be.tank.copy();
        }
        
        @Override
        public int getTankCapacity(int tank) {
            return be.getFluidCost();
        }
        
        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return true;
        }
        
        @Override
        public int fill(FluidStack resource, FluidAction action) {
            int fluidUsed = be.canAddFluid(resource.copy());
            if (fluidUsed > 0) {
                if (action.execute()) {
                    be.startRecipe(resource);
                }
            }
            
            return fluidUsed;
        }
        
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }
        
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    }
    
    public static class CastingTableItemCapability implements IItemHandler {
        CastingTableBlockEntity be;
        
        public CastingTableItemCapability(CastingTableBlockEntity be) {
            this.be = be;
        }
        
        @Override
        public int getSlots() {
            return 2;
        }
        
        @Override
        public ItemStack getStackInSlot(int slot) {
            return be.getItem(slot).copy();
        }
        
        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot == 0 && be.getItem(slot).isEmpty() && !stack.isEmpty() && stack.getItem() instanceof CastingMold) {
                if (!simulate) {
                    be.setItem(0, stack.copyWithCount(1));
                    Level level = be.getLevel();
                    if (null != level) {
                        level.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), Block.UPDATE_ALL);
                    }
                }
                
                return stack.copyWithCount(stack.getCount() - 1);
            }
            
            return stack.copy();
        }
        
        /**
         * Only interacts with output slot.
         */
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack output = be.getItem(1);
            if (slot == 1 && !output.isEmpty() && amount > 0) {
                int toTransfer = Math.min(amount, output.getCount());
                if (!simulate) {
                    be.setItem(1, output.copyWithCount(output.getCount() - toTransfer));
                    Level level = be.getLevel();
                    if (null != level) {
                        level.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), Block.UPDATE_ALL);
                    }
                }
                
                return output.copyWithCount(toTransfer);
            }
            
            return ItemStack.EMPTY;
        }
        
        @Override
        public int getSlotLimit(int slot) {
            return slot == 0 ? 1 : Item.DEFAULT_MAX_STACK_SIZE;
        }
        
        /**
         * Is this stack ever a valid input? Don't think you're supposed to consider stack size here, so partial
         * transfers are still accepted.
         */
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == 0 && stack.getItem() instanceof CastingMold;
        }
    }
}
