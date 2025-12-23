package com.github.no_name_provided.nnp_rune_smithing.client;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

@EventBusSubscriber(modid = MODID)
public class RSClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    
    private static final ModConfigSpec.BooleanValue SIMPLE_AOE_BLOCK_HIGHLIGHT =
            BUILDER.comment("Should we use the classic \"hammer\" selection highlight?")
                    .define("Use Simple AOE HitBox Highlight", false);
    
    public static final ModConfigSpec SPEC = BUILDER.build();
    
    public static boolean simpleAOEBlockHighlight;
    
    @SubscribeEvent
    static void onConfigUpdate(final ModConfigEvent event) {
        if (!(event instanceof ModConfigEvent.Unloading) && event.getConfig().getType() == ModConfig.Type.CLIENT) {
            simpleAOEBlockHighlight = SIMPLE_AOE_BLOCK_HIGHLIGHT.get();
        }
    }
}
