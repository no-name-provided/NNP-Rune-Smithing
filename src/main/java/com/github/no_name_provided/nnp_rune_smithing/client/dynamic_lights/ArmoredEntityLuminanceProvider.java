package com.github.no_name_provided.nnp_rune_smithing.client.dynamic_lights;

import com.github.no_name_provided.nnp_rune_smithing.common.attachments.RSAttachments;
import com.mojang.serialization.MapCodec;
import dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Range;

public class ArmoredEntityLuminanceProvider implements EntityLuminance {
    
    public static final MapCodec<ArmoredEntityLuminanceProvider> CODEC =
            MapCodec.unit(new ArmoredEntityLuminanceProvider()
            );
    
    @Override
    public Type type() {
        return RSLambDynamicLightsInitializer.ARMORED_ENTITY_LUMINANCE;
    }
    
    /**
     * Provides the emissive light level for entities wearing glowing armor.
     *
     * @param itemLightSourceManager Light source manager with item hooks.
     * @param entity                 Entity being checked.
     * @return The light level the entity should emit.
     */
    @Override
    public @Range(from = 0, to = 15) int getLuminance(
            ItemLightSourceManager itemLightSourceManager,
            Entity entity
    ) {
        // Moved light level validation to retrieval. It may be marginally less efficient,
        // but it eliminates an overflow bug that would otherwise require an extra byte be tracked
        // (per entity) and that seems like a less efficient approach
        if (entity.hasData(RSAttachments.LIGHT_FROM_ARMOR)) {
            return Mth.clamp(entity.getData(RSAttachments.LIGHT_FROM_ARMOR), 0, 15);
        } else {
            return 0;
        }
    }
}

