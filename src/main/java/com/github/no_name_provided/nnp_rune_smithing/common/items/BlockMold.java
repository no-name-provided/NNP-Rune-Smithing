package com.github.no_name_provided.nnp_rune_smithing.common.items;

import com.github.no_name_provided.nnp_rune_smithing.common.datamaps.CastableFluidData;
import com.github.no_name_provided.nnp_rune_smithing.common.items.interfaces.CastingMold;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static com.github.no_name_provided.nnp_rune_smithing.common.datamaps.RSDataMaps.CASTABLE_FLUID_DATA;

public class BlockMold extends Item implements CastingMold {
    private final boolean CONSUMED;
    
    // Only add entries. Buffer shared between all instances of this mold ItemStack, and discarded on game end
    // For persistent, per mold buffers, use a DataComponent
    Map<Fluid, ItemStack> recipeBuffer = new LinkedHashMap<>();
    private boolean alreadyLoggedErrorThisSession = false;
    
    public BlockMold(Properties properties, boolean consumed) {
        super(properties);
        CONSUMED = consumed;
    }
    
    @Override
    public int amountRequired() {
        
        return 1296;
    }
    
    @Override
    public boolean validateFluid(FluidStack fluid) {
        
        return null != fluid.getFluidHolder().getData(CASTABLE_FLUID_DATA) && !getResult(fluid).isEmpty();
    }
    
    @Override
    public boolean consumed() {
        
        return CONSUMED;
    }
    
    @Override
    public ItemStack getResult(FluidStack fluid) {
        if (recipeBuffer.containsKey(fluid.getFluid())) {
            
            return recipeBuffer.get(fluid.getFluid());
        } else {
            CastableFluidData data = fluid.getFluidHolder().getData(CASTABLE_FLUID_DATA);
            if (null != data) {
                if (data.solidEquivalents().isPresent()) {
                    Optional<TagKey<Item>> castingResult = data.solidEquivalents().get().blockItems();
                    if (castingResult.isPresent()) {
                        Optional<Holder<Item>> block = BuiltInRegistries.ITEM.getOrCreateTag(castingResult.get()).stream().findFirst();
                        if (block.isPresent()) {
                            recipeBuffer.put(fluid.getFluid(), block.get().value().getDefaultInstance());
                            
                            return block.get().value().getDefaultInstance();
                        }
                    }
                }
            }
        }
        
        if (!alreadyLoggedErrorThisSession) {
            LogUtils.getLogger().warn("Failed to process block casting recipe. Make sure there's a block with the correct tag, the fluid datamap exists, and everything is registered! (This message will not repeat.)");
            alreadyLoggedErrorThisSession = true;
        }
        
        return ItemStack.EMPTY;
    }
}
