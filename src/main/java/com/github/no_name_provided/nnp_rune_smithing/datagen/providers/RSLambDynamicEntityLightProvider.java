package com.github.no_name_provided.nnp_rune_smithing.datagen.providers;

import dev.lambdaurora.lambdynlights.api.data.EntityLightSourceDataProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

import java.util.concurrent.CompletableFuture;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

public class RSLambDynamicEntityLightProvider extends EntityLightSourceDataProvider {
    public RSLambDynamicEntityLightProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider, MODID);
    }
    
    /**
     * Generates the light sources and adds them to the list.
     *
     * @param context the light source data generation context
     */
    @Override
    protected void generate(Context context) {
//        context.add(EntityType.PLAYER, new ArmoredEntityLuminanceProvider());
    }
}
