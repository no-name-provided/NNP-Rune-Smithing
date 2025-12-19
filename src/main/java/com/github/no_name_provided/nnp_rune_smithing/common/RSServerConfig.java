package com.github.no_name_provided.nnp_rune_smithing.common;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

@EventBusSubscriber(modid = MODID)
public class RSServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    
    private static final ModConfigSpec.BooleanValue SPAWN_RUNIC_MOBS =
            BUILDER.comment("Should rare variants of vanilla mobs that drop runes randomly spawn?")
                    .define("Spawn Runic Mobs", true);
    private static final ModConfigSpec.DoubleValue ABSORPTION_PER_TIER =
            BUILDER.comment("How much absorption should ward runes (on armor) give you?")
                    .defineInRange("Absorption Per Tier", 1.0, 0.5, 10);
    private static final ModConfigSpec.DoubleValue SPEED_PER_TIER =
            BUILDER.comment("How much faster should air runes (on armor) make you?")
                    .defineInRange("Speed Per Tier", 0.05, 0.001, 0.1);
    private static final ModConfigSpec.DoubleValue UW_MINING_SPEED_PER_TIER =
            BUILDER.comment("How much faster should water runes (on armor) make you mine underwater?")
                    .defineInRange("UW Mining Speed Per Tier", 1, 0.001, 10);
    private static final ModConfigSpec.DoubleValue EXTRA_AIR_PER_TIER =
            BUILDER.comment("How much air should water runes (on armor) give you?")
                    .defineInRange("Extra Air Per Tier", 1, 0.5, 100);
    private static final ModConfigSpec.DoubleValue EXTRA_WATER_SPEED_PER_TIER =
            BUILDER.comment("How much faster should water runes (on armor) make you swim?")
                    .defineInRange("Swim Speed Per Tier", 0.5, 0.001, 0.1);
    private static final ModConfigSpec.DoubleValue HEALTH_PER_TIER =
            BUILDER.comment("How much health should earth runes (on armor) give you?")
                    .defineInRange("Health Per Tier", 0.05, 0.001, 0.1);
    private static final ModConfigSpec.DoubleValue BURN_TIME_PER_TIER =
            BUILDER.comment("How much faster should fire runes (on armor) make you go out (decimal percent change)?")
                    .defineInRange("Burn Time Reduction Per Tier", -0.2, -1, 0);
    
    public static final ModConfigSpec SPEC = BUILDER.build();
    
    public static boolean spawnRunicMobs;
    public static float absorptionPerTier;
    public static float speedPerTier;
    public static float underwaterMiningSpeedPerTier;
    public static float extraAirPerTier;
    public static float extraWaterSpeedPerTier;
    public static float healthPerTier;
    public static float burnTimeMultPerTier;
    
    @SubscribeEvent
    static void onConfigUpdate(final ModConfigEvent event) {
        if (!(event instanceof ModConfigEvent.Unloading) && event.getConfig().getType() == ModConfig.Type.SERVER) {
            spawnRunicMobs = SPAWN_RUNIC_MOBS.get();
            // Have to use primitive types or the compiler throws a tantrum
            absorptionPerTier = (float) (double) ABSORPTION_PER_TIER.get();
            speedPerTier = (float) (double) SPEED_PER_TIER.get();
            underwaterMiningSpeedPerTier = (float) (double) UW_MINING_SPEED_PER_TIER.get();
            extraAirPerTier = (float) (double) EXTRA_AIR_PER_TIER.get();
            extraWaterSpeedPerTier = (float) (double) EXTRA_WATER_SPEED_PER_TIER.get();
            healthPerTier = (float) (double) HEALTH_PER_TIER.get();
            burnTimeMultPerTier = (float) (double) BURN_TIME_PER_TIER.get();
        }
    }
}
