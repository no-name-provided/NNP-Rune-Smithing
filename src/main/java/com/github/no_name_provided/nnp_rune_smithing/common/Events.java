package com.github.no_name_provided.nnp_rune_smithing.common;

import com.github.no_name_provided.nnp_rune_smithing.common.capabilities.CastingTableCapability;
import com.github.no_name_provided.nnp_rune_smithing.common.capabilities.MelterCapability;
import com.github.no_name_provided.nnp_rune_smithing.common.entities.RSEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber
public class Events {
    @SubscribeEvent
    static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                RSEntities.MELTER_BLOCK_ENTITY.get(),
                (entity, context) -> new MelterCapability.MelterFluidHandler(entity)
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                RSEntities.CASTING_TABLE_BLOCK_ENTITY.get(),
                (entity, context) -> new CastingTableCapability.CastingTableFluidCapability(entity)
        );
    }
}
