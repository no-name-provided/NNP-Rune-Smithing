package com.github.no_name_provided.nnp_rune_smithing.client.dynamic_lights;

import dev.lambdaurora.lambdynlights.api.DynamicLightsContext;
import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer;
import dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import net.minecraft.resources.ResourceLocation;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

/**
 * Not loaded by my code.
 */
public class RSLambDynamicLightsInitializer implements DynamicLightsInitializer {
    
    /**
     * Called when LambDynamicLights is initialized to register various objects related to dynamic lighting such as:
     * <ul>
     *     <li>entity luminance providers;</li>
     *     <li>item and entity light sources;</li>
     *     <li>custom dynamic lighting behavior.</li>
     * </ul>
     *
     * @param context the dynamic lights context, containing references to managers for each source type provided by the
     *                API
     */
    @Override
    public void onInitializeDynamicLights(DynamicLightsContext context) {
        DynamicLightsInitializer.super.onInitializeDynamicLights(context);
        
    }
    
    /**
     * Called when LambDynamicLights is initialized to register custom dynamic light handlers and item light sources.
     *
     * @param itemLightSourceManager the manager for item light sources
     * @deprecated Please use the {@link #onInitializeDynamicLights(DynamicLightsContext)} instead, and read
     * <a href="https://lambdaurora.dev/projects/lambdynamiclights/docs/v4/java.html">the official documentation</a>.
     * <p>
     * This is fully removed in LambDynamicLights releases targeting Minecraft 1.21.4 and newer.
     */
    
    @Override
    @SuppressWarnings({"removal", "UnstableApiUsage"})
    // Mandatory override. Compiler whines if I leave it out. Probably missing a default keyword.
    public void onInitializeDynamicLights(ItemLightSourceManager itemLightSourceManager) {
    
    }
    
    public static final EntityLuminance.Type ARMORED_ENTITY_LUMINANCE = EntityLuminance.Type.register(
            ResourceLocation.fromNamespaceAndPath(MODID, "light_from_armor"),
            ArmoredEntityLuminanceProvider.CODEC
    );
}
