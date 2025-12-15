package com.github.no_name_provided.nnp_rune_smithing.common.capabilities;

import com.github.no_name_provided.nnp_rune_smithing.common.entities.AlloyerBlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class AlloyerCapability {
    public static class FluidHandler implements IFluidHandler {
        private final AlloyerBlockEntity be;
        
        public FluidHandler(AlloyerBlockEntity be) {
            this.be = be;
        }
        
        @Override
        public int getTanks() {
            return 3;
        }
        
        /**
         * Returns the FluidStack in a given tank.
         *
         * <p>
         * <strong>IMPORTANT:</strong> This FluidStack <em>MUST NOT</em> be modified. This method is not for
         * altering internal contents. Any implementers who are able to detect modification via this method should throw
         * an exception. It is ENTIRELY reasonable and likely that the stack returned here will be a copy.
         * </p>
         *
         * <p>
         * <strong><em>SERIOUSLY: DO NOT MODIFY THE RETURNED FLUIDSTACK</em></strong>
         * </p>
         *
         * @param tank Tank to query.
         * @return FluidStack in a given tank. FluidStack.EMPTY if the tank is empty.
         */
        @Override
        public FluidStack getFluidInTank(int tank) {
            return be.getFluidInTank(tank);
        }
        
        /**
         * Retrieves the maximum fluid amount for a given tank.
         *
         * @param tank Tank to query.
         * @return The maximum fluid amount held by the tank.
         */
        @Override
        public int getTankCapacity(int tank) {
            return AlloyerBlockEntity.TANK_CAPACITY;
        }
        
        /**
         * This function is a way to determine which fluids can exist inside a given handler. General purpose tanks will
         * basically always return TRUE for this.
         *
         * @param tank  Tank to query for validity
         * @param stack Stack to test with for validity
         * @return TRUE if the tank can hold the FluidStack, not considering current state. (Basically, is a given fluid
         * EVER allowed in this tank?) Return FALSE if the answer to that question is 'no.'
         */
        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return true;
        }
        
        /**
         * Fills fluid into internal tanks, distribution is left entirely to the IFluidHandler.
         *
         * @param resource FluidStack representing the Fluid and maximum amount of fluid to be filled.
         * @param action   If SIMULATE, fill will only be simulated.
         * @return Amount of resource that was (or would have been, if simulated) filled.
         */
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
            int tank = getFirstNonEmptyTank();
            if (tank == -1) {
                
                return FluidStack.EMPTY;
            }
            int toDrain = Math.min(maxDrain, getFluidInTank(tank).getAmount());
            if (!action.simulate()) {
                be.setTank(tank, getFluidInTank(tank).copyWithAmount(getFluidInTank(tank).getAmount() - toDrain));
            }
            
            return getFluidInTank(tank).copyWithAmount(toDrain);
        }
        
        private int getFirstNonEmptyTank() {
            int tank = -1;
            for (int i = 0; i < 2; i++) {
                if (!getFluidInTank(i).isEmpty()) {
                    tank = i;
                    break;
                }
            }
            
            return tank;
        }
    }
}
