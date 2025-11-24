package com.github.no_name_provided.nnp_rune_smithing.common.capabilities;

import com.github.no_name_provided.nnp_rune_smithing.common.entities.CastingTableBlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

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
}
