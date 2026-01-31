package com.github.no_name_provided.nnp_rune_smithing.common.events;

import com.github.no_name_provided.nnp_rune_smithing.common.network.payloads.UpdateNamesPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

@EventBusSubscriber(modid = MODID)
public class ServerReloadEvents {
    
    @SubscribeEvent
    private static void onRegisterServerReloadListeners(AddReloadListenerEvent event) {
    
    }
    
    @SubscribeEvent
    static void onAdvancementEarn(AdvancementEvent.AdvancementEarnEvent event) {
        ResourceLocation id = event.getAdvancement().id();
        if (id.getNamespace().equals(MODID) && id.getPath().startsWith("guide_book/") && id.getPath().endsWith("rune_in_inventory")) {
            // This event should only be thrown on the server
            PacketDistributor.sendToPlayer((ServerPlayer) event.getEntity(), new UpdateNamesPayload());
        }
    }
    
    
    
//    private static class MelterReloadListener extends SimplePreparableReloadListener<JsonObject> {
//        private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
//        private static final Logger LOGGER = LogUtils.getLogger();
//        private final HolderLookup.Provider registries;
//        private Multimap<RecipeType<?>, RecipeHolder<?>> byType = ImmutableMultimap.of();
//        private Map<ResourceLocation, RecipeHolder<?>> byName = ImmutableMap.of();
//        private boolean hasErrors;
//
//        public MelterReloadListener() {
//            super(
//
//            )
//        }
//
//        private MelterReloadListener(HolderLookup.Provider registries) {
//            this.registries = registries;
//        }
//
//        /**
//         * Performs any reloading that can be done off-thread, such as file IO
//         *
//         * @param resourceManager
//         * @param profiler
//         */
//        @Override
//        protected JsonObject prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
//
//            return null;
//        }
//
//        /**
//         * Performs any reloading that can be done off-thread, such as file IO
//         *
//         * @param resourceManager
//         * @param profiler
//         */
//        @Override
//        protected void apply(JsonObject object, ResourceManager resourceManager, ProfilerFiller profiler) {
//
//        }
//    }
}
