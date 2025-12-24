package com.github.no_name_provided.nnp_rune_smithing.client.dynamic_lights;

import com.github.no_name_provided.nnp_rune_smithing.common.attachments.RSAttachments;
import com.mojang.serialization.MapCodec;
import dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Range;

public class ArmoredEntityLuminanceProvider implements EntityLuminance {
    
    // The Codec of this entity luminance provider,
    // this describes how to parse the JSON file.
    public static final MapCodec<ArmoredEntityLuminanceProvider> CODEC =
            MapCodec.unit(new ArmoredEntityLuminanceProvider()
            );
    
    @Override
    public Type type() {
        // This is the registered type of this entity luminance provider.
        // We will modify the initializer to reflect this.
        return RSLambDynamicLightsInitializer.ARMORED_ENTITY_LUMINANCE;
    }
    
    @Override
    public @Range(from = 0, to = 15) int getLuminance(
            ItemLightSourceManager itemLightSourceManager,
            Entity entity
    ) {
        // Here we compute the luminance the given entity should emit.
        // We also have access to the item light source manager,
        // in case our luminance depends on the luminance of an item.
        if (entity.hasData(RSAttachments.LIGHT_FROM_ARMOR)) {
            return entity.getData(RSAttachments.LIGHT_FROM_ARMOR);
        } else {
            return 0;
        }
    }
}

