package com.github.no_name_provided.nnp_rune_smithing.common.network.payloads;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

public record UpdateNamesPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateNamesPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "update_names"));
    
    // Payloads require the registry friendly version
    public static StreamCodec<RegistryFriendlyByteBuf, UpdateNamesPayload> STREAM_CODEC =
            StreamCodec.unit(new UpdateNamesPayload());
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
