package com.github.no_name_provided.nnp_rune_smithing.datagen.providers;

import com.github.no_name_provided.nnp_rune_smithing.common.datamaps.CastableFluidData;
import com.github.no_name_provided.nnp_rune_smithing.common.datamaps.RSDataMaps;
import com.github.no_name_provided.nnp_rune_smithing.common.fluids.FluidHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.conditions.TrueCondition;
import net.neoforged.neoforge.common.data.DataMapProvider;

import java.util.concurrent.CompletableFuture;

public class RSDataMapProvider extends DataMapProvider {
    
    public RSDataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }
    
    @Override
    protected void gather(HolderLookup.Provider provider) {
        FluidHelper.FLUID_SETS.forEach(set -> {
            builder(RSDataMaps.CASTABLE_FLUID_DATA).add(
                    set.source(),
                    new CastableFluidData(set.type().get().COLOR_WHEN_COOL, set.type().get().TIER),
                    false,
                    TrueCondition.INSTANCE
            );
        });
    }
}
