package com.github.no_name_provided.nnp_rune_smithing.client;

import com.github.no_name_provided.nnp_rune_smithing.common.items.runes.AbstractRuneItem;
import com.mojang.logging.LogUtils;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jspecify.annotations.Nullable;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

/**
 * Wraps references to client only classes, so we can access them in shared code without a missing class definition
 * crash.
 */
public class ClientUtilWrapper {
    public static @Nullable Player getLocalPlayer() {
        
        return Minecraft.getInstance().player;
    }
    
    public static boolean localPlayerKnowsRune(AbstractRuneItem rune) {
        if (!RSClientConfig.hideUnknownRuneNames || DatagenModLoader.isRunningDataGen()) {
            
            // Datagen knows everything
            return true;
        }
        
        try {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.isLocalServer()) {
                IntegratedServer server = minecraft.getSingleplayerServer();
                if (server != null && server.getAdvancements() instanceof ServerAdvancementManager aManager) {
                    AdvancementHolder aHolder = aManager.get(ResourceLocation.fromNamespaceAndPath(MODID, "guide_book/" + rune.builtInRegistryHolder().getKey().location().getPath() + "_in_inventory"));
                    if (null != aHolder) {
                        AdvancementProgress aProgress = server.getPlayerList().getPlayerAdvancements(server.getPlayerList().getPlayers().getFirst()).getOrStartProgress(aHolder);
                        
                        return aProgress.isDone();
                    }
                }
            } else {
                ClientPacketListener listener = minecraft.getConnection();
                if (null != listener) {
                    AdvancementHolder aHolder = listener.getAdvancements().get(ResourceLocation.fromNamespaceAndPath(MODID, "guide_book/" + rune.builtInRegistryHolder().getKey().location().getPath() + "_in_inventory"));
                    if (null != aHolder && ServerLifecycleHooks.getCurrentServer() instanceof MinecraftServer server) {
                        AdvancementProgress aProgress = server.getPlayerList().getPlayer(getLocalPlayer().getUUID()).getAdvancements().getOrStartProgress(aHolder);
                        
                        return aProgress.isDone();
                    }
                }
            }
        } catch (Error e) {
            LogUtils.getLogger().error("Attempt to hide the names of unknown runes failed. Recommend you disable this setting in your config.");
            e.printStackTrace();
        }
        
        return false;
    }
}
