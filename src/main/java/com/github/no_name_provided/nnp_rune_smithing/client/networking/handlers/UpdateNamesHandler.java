package com.github.no_name_provided.nnp_rune_smithing.client.networking.handlers;

import com.github.no_name_provided.nnp_rune_smithing.client.RSClientConfig;
import com.github.no_name_provided.nnp_rune_smithing.common.network.payloads.UpdateNamesPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class UpdateNamesHandler {
    /**
     * Should be handled on the main thread.
     */
    public static void handleDataOnMain(final UpdateNamesPayload ignoredData, final IPayloadContext ignoredContext) {
        if (RSClientConfig.hideUnknownRuneNames && RSClientConfig.refreshCachesWhenNamesAreRevealed) {
            Minecraft.getInstance().reloadResourcePacks();
        }
    }
}
