package com.github.no_name_provided.nnp_rune_smithing.common.capabilities;

import com.github.no_name_provided.nnp_rune_smithing.common.entities.MelterBlockEntity;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class MelterCapability {
    public static class MelterFluidHandler implements IFluidHandler {
        public static int MELTER_CAPACITY = 10000;
        MelterBlockEntity MELTER;
        
        public MelterFluidHandler(MelterBlockEntity melter) {
            MELTER = melter;
        }
        @Override
        public int getTanks() {
            return 1;
        }
        @Override
        public FluidStack getFluidInTank(int tank) {
            
            return MELTER.output.copy();
        }
        @Override
        public int getTankCapacity(int tank) {
            
            return MELTER_CAPACITY;
        }
        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            
            return true;
        }
        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.getFluid() == MELTER.output.getFluid() || MELTER.output.isEmpty()) {
                int currentLevel = MELTER.output.getAmount();
                int remainingRoom = getTankCapacity(0) - currentLevel;
                int toTransfer = Mth.clamp(resource.getAmount(), 0, remainingRoom);
                if (action.execute()) {
                    MELTER.setOutput(resource.copyWithAmount(currentLevel + toTransfer));
//                    resource.setAmount(resource.getAmount() - toTransfer);
                }
                
                return toTransfer;
            } else {
                
                return 0;
            }
        }
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.getFluid() == MELTER.output.getFluid()) {
                int toDrain = Mth.clamp(resource.getAmount(), 0, MELTER.output.getAmount());
                if (action.execute()) {
                    MELTER.setOutput(MELTER.output.copyWithAmount(MELTER.output.getAmount() - toDrain));
//                    resource.setAmount(resource.getAmount() + toDrain);
                }
                
                return new FluidStack(MELTER.output.getFluid(), toDrain);
            } else {
                
                return new FluidStack(MELTER.output.getFluid(), 0);
            }
        }
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            int toDrain = Mth.clamp(maxDrain, 0, MELTER.output.getAmount());
            if (action.execute()) {
                MELTER.setOutput(MELTER.output.copyWithAmount(MELTER.output.getAmount() - toDrain));
            }
            
            return MELTER.output.copyWithAmount(toDrain);
        }
    }
}
