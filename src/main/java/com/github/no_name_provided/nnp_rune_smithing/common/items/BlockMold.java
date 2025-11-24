package com.github.no_name_provided.nnp_rune_smithing.common.items;

import com.github.no_name_provided.nnp_rune_smithing.common.fluids.FluidHelper;
import com.github.no_name_provided.nnp_rune_smithing.common.fluids.MoltenMetalFluid;
import com.github.no_name_provided.nnp_rune_smithing.common.items.interfaces.CastingMold;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static com.github.no_name_provided.nnp_rune_smithing.common.fluids.FluidHelper.FLUID_SETS;

public class BlockMold extends Item implements CastingMold {
    // Only add entries. Buffer shared between all instances of this mold ItemStack, and discarded on game end
    // For persistent, per mold buffers, use a DataComponent
    Map<Fluid, ItemStack> recipeBuffer = new LinkedHashMap<>();
    private boolean alreadyLoggedErrorThisSession = false;
    
    public BlockMold(Properties properties) {
        super(properties);
    }
    
    @Override
    public int amountRequired() {
        return 1296;
    }
    
    @Override
    public boolean validateFluid(FluidStack fluid) {
        return fluid.getFluid() instanceof MoltenMetalFluid && !getResult(fluid).isEmpty();
    }
    
    @Override
    public boolean consumed() {
        return false;
    }
    
    @Override
    public ItemStack getResult(FluidStack fluid) {
        if (recipeBuffer.containsKey(fluid.getFluid())) {
            
            return recipeBuffer.get(fluid.getFluid());
        } else {
            Optional<FluidHelper.FluidSet> match = FLUID_SETS.stream().filter(set -> set.type().get() == fluid.getFluidType()).findFirst();
            // Should always be true
            if (match.isPresent()) {
                Optional<Holder<Item>> block = BuiltInRegistries.ITEM.getOrCreateTag(match.get().equivalents().blockItems()).stream().findFirst();
                // Should always be true
                if (block.isPresent()) {
                    recipeBuffer.put(fluid.getFluid(), block.get().value().getDefaultInstance());
                    
                    return block.get().value().getDefaultInstance();
                }
            }
            if (!alreadyLoggedErrorThisSession) {
                LogUtils.getLogger().warn("Failed to process casting recipe. Make sure there's a block with the correct tag, the fluid set exists, and everything is registered!");
                alreadyLoggedErrorThisSession = true;
            }
            
            return ItemStack.EMPTY;
        }
    }
}
