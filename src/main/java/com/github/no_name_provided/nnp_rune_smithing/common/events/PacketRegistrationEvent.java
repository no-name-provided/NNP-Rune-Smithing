package com.github.no_name_provided.nnp_rune_smithing.common.events;

import com.github.no_name_provided.nnp_rune_smithing.client.networking.handlers.UpdateNamesHandler;
import com.github.no_name_provided.nnp_rune_smithing.common.network.payloads.UpdateNamesPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.MainThreadPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

@EventBusSubscriber(modid = MODID)
public class PacketRegistrationEvent {
    @SubscribeEvent
    public static void onPayloadRegistration(final RegisterPayloadHandlersEvent event) {
        // Sets the current network version, which appears to be completely arbitrary (and redundant with version ranges)
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                UpdateNamesPayload.TYPE,
                UpdateNamesPayload.STREAM_CODEC,
                new MainThreadPayloadHandler<>(
                        UpdateNamesHandler::handleDataOnMain
                )
        );
    }
}
