package com.github.no_name_provided.nnp_rune_smithing.common;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

@EventBusSubscriber(modid = MODID)
public class RSServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    
    private static final ModConfigSpec.BooleanValue SPAWN_RUNIC_MOBS =
            BUILDER.comment("Should rare variants of vanilla mobs that drop runes randomly spawn?")
                    .define("Spawn Runic Mobs", true);
    private static final ModConfigSpec.IntValue RUNIC_MOB_PERIOD =
            BUILDER.comment("On average, one out of every [this many] eligible mobs will spawn with runic buffs.")
                    .defineInRange("Runic Mob Period", 100, 1, Integer.MAX_VALUE);
    private static final ModConfigSpec.ConfigValue<List<? extends String>> RUNIC_MOB_BIOME_BLACKLIST =
            BUILDER.comment("Which biomes should runic mobs NOT spawn in? (Some runic mobs are biome locked, " +
                            "and won't spawn at all if your blacklist is too broad).")
                    .defineListAllowEmpty("Runic Mob Blacklist", List.of(), RSServerConfig::validateItemName);
    
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
    public static int runicMobPeriod;
    public static List<Biome> runicMobBiomeBlacklist;
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
            runicMobPeriod = RUNIC_MOB_PERIOD.getAsInt();
            // convert the list of strings into a list of biomes
            runicMobBiomeBlacklist = RUNIC_MOB_BIOME_BLACKLIST.get().stream().map(RSServerConfig::getBiomeFromResourceLocation).collect(Collectors.toList());
            
            // Have to use primitive types or the compiler throws a tantrum. A double cast also works.
            absorptionPerTier = (float) ABSORPTION_PER_TIER.getAsDouble();
            speedPerTier = (float) SPEED_PER_TIER.getAsDouble();
            underwaterMiningSpeedPerTier = (float) UW_MINING_SPEED_PER_TIER.getAsDouble();
            extraAirPerTier = (float) EXTRA_AIR_PER_TIER.getAsDouble();
            extraWaterSpeedPerTier = (float) EXTRA_WATER_SPEED_PER_TIER.getAsDouble();
            healthPerTier = (float) HEALTH_PER_TIER.getAsDouble();
            burnTimeMultPerTier = (float) BURN_TIME_PER_TIER.getAsDouble();
        }
    }
    
    private static boolean validateItemName(final Object object) {
        if (object instanceof String biome) {
            // May need to replace this with a Minecraft#instance check on some clients. We'll see.
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (null != server) {
                Optional<Registry<Biome>> biomes = server.registryAccess().registry(Registries.BIOME);
                if (biomes.isPresent()) {
                    
                    return biomes.get().containsKey(ResourceLocation.parse(biome));
                }
            }
            
            throw new ExceptionInInitializerError(
                    "Attempted to access registry before server was available. " +
                            "Unable to validate biome blacklist from config."
            );
        } else {
            
            return false;
        }
    }
    
    private static Biome getBiomeFromResourceLocation(final String location) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (null != server) {
            Optional<Registry<Biome>> biomes = server.registryAccess().registry(Registries.BIOME);
            if (biomes.isPresent()) {
                Biome biome = biomes.get().get(ResourceLocation.parse(location));
                // This should always be true, if our validator did its job.
                if (null != biome) {
                    
                    return biome;
                } else {
                    throw new ExceptionInInitializerError("Couldn't find biome in runic mob spawn blacklist.");
                }
            }
        }
        
        throw new ExceptionInInitializerError(
                "Attempted to access registry before server was available. " +
                        "Unable to parse biome blacklist from config."
        );
    }
    
}
