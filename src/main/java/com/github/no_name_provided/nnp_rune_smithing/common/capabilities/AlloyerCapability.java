package com.github.no_name_provided.nnp_rune_smithing.common.capabilities;

import com.github.no_name_provided.nnp_rune_smithing.common.entities.AlloyerBlockEntity;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;

import static com.github.no_name_provided.nnp_rune_smithing.common.entities.AlloyerBlockEntity.RESULT;

public class AlloyerCapability {
    public static class FluidHandler implements IFluidHandler {
        private final AlloyerBlockEntity be;
        private final boolean bottom;
        
        public FluidHandler(AlloyerBlockEntity be, @Nullable Direction side) {
            this.be = be;
            if (null == side) {
                this.bottom = false;
            } else {
                this.bottom = Direction.DOWN.equals(side);
            }
        }
        
        @Override
        public int getTanks() {
            return 3;
        }
        
        @Override
        public FluidStack getFluidInTank(int tank) {
            return be.getFluidInTank(tank);
        }
        
        @Override
        public int getTankCapacity(int tank) {
            return AlloyerBlockEntity.TANK_CAPACITY;
        }
        
        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return true;
        }
        
        @Override
        public int fill(FluidStack resource, FluidAction action) {
            
            int tank = getFirstMatchingTank(resource);
            if (tank == -1) {
                tank = getFirstEmptyTank();
            }
            if (tank == -1) {
                
                return 0;
            } else {
                int room = AlloyerBlockEntity.TANK_CAPACITY - getFluidInTank(tank).getAmount();
                if (room <= 0) {
                    
                    return 0;
                } else {
                    int toTransfer = Math.min(room, resource.getAmount());
                    if (!action.simulate()) {
                        // We use resource here because, at this point, the tank will only be empty if the resource is,
                        // but the reverse isn't true
                        be.setTank(tank, resource.copyWithAmount(toTransfer + getFluidInTank(tank).getAmount()));
                    }
                    
                    return toTransfer;
                }
            }
        }
        
        /**
         * Returns the id of the first tank with matching fluid, or returns -1 for failure.
         */
        private int getFirstMatchingTank(FluidStack resource) {
            // The bottom face can only drain the result tank
            if (bottom) {
                
                return getFluidInTank(RESULT).getFluid() == resource.getFluid() ? RESULT : -1;
            } else {
                
                int tank = -1;
                // The last tank is output
                for (int i = 0; i < getTanks() - 1; i++) {
                    if (getFluidInTank(i).getFluid() == resource.getFluid()) {
                        tank = i;
                        break;
                    }
                }
                
                return tank;
            }
        }
        
        /**
         * Returns the id of the first empty tank, or returns -1 for failure.
         */
        private int getFirstEmptyTank() {
            int tank = -1;
            for (int i = 0; i < 2; i++) {
                if (getFluidInTank(i).isEmpty()) {
                    tank = i;
                    break;
                }
            }
            
            return tank;
        }
        
        /**
         * Drains fluid out of internal tanks, distribution is left entirely to the IFluidHandler.
         *
         * @param resource FluidStack representing the Fluid and maximum amount of fluid to be drained.
         * @param action   If SIMULATE, drain will only be simulated.
         * @return FluidStack representing the Fluid and amount that was (or would have been, if simulated) drained.
         */
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
//            if (bottom) {
//                if (resource.getFluid().isSame(getFluidInTank(RESULT).getFluid())) {
//                    int toDrain = Math.min(getFluidInTank(RESULT).getAmount(), resource.getAmount());
//                    if (!action.simulate()) {
//                        be.setResultTank(resource.copyWithAmount(getFluidInTank(RESULT).getAmount() - toDrain));
//                    }
//
//                    return resource.copyWithAmount(toDrain);
//                } else {
//
//                    return FluidStack.EMPTY;
//                }
//            } else {
            
            int tank = getFirstMatchingTank(resource);
            if (tank == -1) {
                
                return FluidStack.EMPTY;
            }
            int toDrain = Math.min(getFluidInTank(tank).getAmount(), resource.getAmount());
            if (!action.simulate()) {
                be.setTank(tank, getFluidInTank(tank).copyWithAmount(getFluidInTank(tank).getAmount() - toDrain));
            }
            
            return resource.copyWithAmount(toDrain);
        }
//        }
        
        /**
         * Drains fluid out of internal tanks, distribution is left entirely to the IFluidHandler.
         * <p>
         * This method is not Fluid-sensitive.
         *
         * @param maxDrain Maximum amount of fluid to drain.
         * @param action   If SIMULATE, drain will only be simulated.
         * @return FluidStack representing the Fluid and amount that was (or would have been, if simulated) drained.
         */
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            int tank = getLastNonEmptyTank();
            if (tank == -1) {
                
                return FluidStack.EMPTY;
            }
            int toDrain = Math.min(maxDrain, getFluidInTank(tank).getAmount());
            // We buffer the tank's fluid here, just in case transferring the fluid empties the tank
            FluidStack whatWasInTank = getFluidInTank(tank).copyWithAmount(toDrain);
            if (!action.simulate()) {
                be.setTank(tank, getFluidInTank(tank).copyWithAmount(getFluidInTank(tank).getAmount() - toDrain));
            }
            
            return whatWasInTank.copyWithAmount(toDrain);
        }
        
        /**
         * We use the last tank first, to prioritize the result tank.
         */
        private int getLastNonEmptyTank() {
            if (bottom) {
                
                return !getFluidInTank(RESULT).isEmpty() ? RESULT : -1;
            }
            
            int tank = -1;
            for (int i = getTanks() - 1; i >= 0; i--) {
                if (!getFluidInTank(i).isEmpty()) {
                    tank = i;
                    break;
                }
            }
            
            return tank;
        }
    }
}
