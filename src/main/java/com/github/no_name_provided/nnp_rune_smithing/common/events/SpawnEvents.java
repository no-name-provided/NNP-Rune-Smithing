package com.github.no_name_provided.nnp_rune_smithing.common.events;

import com.github.no_name_provided.nnp_rune_smithing.common.RSServerConfig;
import com.github.no_name_provided.nnp_rune_smithing.common.attachments.RSAttachments;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

import java.util.function.Supplier;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;
import static com.github.no_name_provided.nnp_rune_smithing.common.attachments.RSAttachments.*;
import static net.minecraft.world.entity.EntityType.*;
import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.*;

@EventBusSubscriber(modid = MODID)
public class SpawnEvents {
    @SubscribeEvent
    static void onMobSpawn(FinalizeSpawnEvent event) {
        if (RSServerConfig.spawnRunicMobs) {
            Mob toSpawn = event.getEntity();
            ServerLevel level = event.getLevel().getLevel();
            if (level.random.nextInt(RSServerConfig.runicMobPeriod) < 1 && !RSServerConfig.runicMobBiomeBlacklist.contains(level.getBiome(toSpawn.blockPosition()).value())) {
                EntityType<?> type = toSpawn.getType();
                if (type == ZOMBIE) {
                    int random = level.random.nextInt(3);
                    if (random < 2) {
                        makeRobust(toSpawn, level, "Robust Zombie");
                    } else {
                        makePoisonous(toSpawn, level, "Poisonous Zombie");
                        toSpawn.setData(POISONOUS, true);
                    }
                } else if (type == SKELETON && level.dimension() == Level.OVERWORLD) {
                    if (level.random.nextInt(1) < 1) {
                        makeLucky(toSpawn, level, "Lucky Skeleton");
                    } else {
                        makeRapidlyFiring(toSpawn, level, "Skeletal Quickshot");
                    }
                } else if (type == WITHER_SKELETON) {
                    if (level.random.nextInt(1) < 1) {
                        makeInverted(toSpawn, level, "Inverted Skeleton");
                    }
                } else if (type == BLAZE && level.dimension() == Level.NETHER) {
                    if (level.random.nextInt(1) < 2) {
                        makeInflamed(toSpawn, level, "Inflamed Blaze");
                    }
                } else if (type == BREEZE) {
                    if (level.random.nextInt(1) < 2) {
                        makeGale(toSpawn, level, "Gale Force Breeze");
                    }
                } else if (type == CREEPER) {
                    if (level.random.nextInt(1) < 2) {
                        makeBlastProof(toSpawn, level, "Blast Proof Creeper");
                    }
                } else if (type == DROWNED) {
                    if (level.random.nextInt(1) < 2) {
                        makeAquatic(toSpawn, level, "Aquatic Drowned");
                    }
                } else if (type == SLIME) {
                    if (level.random.nextInt(1) < 2) {
                        makeGiant(toSpawn, level, "Giant Slime");
                    }
                } else if (type == PIGLIN_BRUTE) {
                    if (level.random.nextInt(1) < 2) {
                        makeRavenous(toSpawn, level, "Ravenous Piglin");
                    }
                } else if (type == GHAST) {
                    if (level.random.nextInt(1) < 2) {
                        makeFarsighted(toSpawn, level, "Far Sighted Ghast");
                    }
                } else if (type == VILLAGER) {
                    if (level.random.nextInt(1) < 2) {
                        makeLucky(toSpawn, level, "Lucky Villager");
                    }
                } else if (type == VINDICATOR) {
                    if (level.random.nextInt(1) < 2) {
                        makeTiny(toSpawn, level, "Tiny Axeman");
                    }
                } else if (type == ENDERMAN && level.dimension() == Level.END) {
                    if (level.random.nextInt(1) < 2) {
                        makeVoidInfused(toSpawn, level, "Void Fused Enderman");
                    }
                    // Doesn't seem to work on mobs spawned from structures, like vanilla shulkers
                } else if (type == SHULKER && level.dimension() == Level.END) {
                    if (level.random.nextInt(1) < 2) {
                        makeRadiant(toSpawn, level, "Radiance In a Box");
                    }
                }
            }
        }
    }
    
    private static void makeInverted(Mob mob, ServerLevel ignoredLevel, String customName) {
        String name = prepareEnhancements(customName, INVERTED, mob);
        safeAddPermanentModifier(mob, Attributes.MAX_HEALTH, name, 3f, ADD_VALUE);
        mob.setHealth(mob.getMaxHealth());
    }
    
    private static void makeTiny(Mob mob, ServerLevel ignoredLevel, String customName) {
        String name = prepareEnhancements(customName, TINY, mob);
        safeAddPermanentModifier(mob, Attributes.MAX_HEALTH, name, -1f / 3f, ADD_MULTIPLIED_TOTAL);
        safeAddPermanentModifier(mob, Attributes.MOVEMENT_SPEED, name, 0.1f, ADD_VALUE);
    }
    
    static void makeRobust(Mob mob, ServerLevel level, String customName) {
        String name = prepareEnhancements(customName, ROBUST, mob);
        safeAddPermanentModifier(mob, Attributes.ATTACK_DAMAGE, name, level.getDifficulty().getId() + 3, ADD_VALUE);
        safeAddPermanentModifier(mob, Attributes.ARMOR_TOUGHNESS, name, level.getDifficulty().getId() + 3, ADD_VALUE);
        safeAddPermanentModifier(mob, Attributes.ARMOR, name, 2 * level.getDifficulty().getId() + 3, ADD_VALUE);
        safeAddPermanentModifier(mob, Attributes.KNOCKBACK_RESISTANCE, name, level.getDifficulty().getId() + 3, ADD_VALUE);
        safeAddPermanentModifier(mob, Attributes.ATTACK_KNOCKBACK, name, level.getDifficulty().getId() + 3, ADD_VALUE);
        safeAddPermanentModifier(mob, Attributes.MAX_HEALTH, name, 20, ADD_VALUE);
        mob.setHealth(mob.getMaxHealth());
        safeAddPermanentModifier(mob, Attributes.SCALE, name, (float) level.getDifficulty().getId() * 0.1f + 0.5f, ADD_MULTIPLIED_BASE);
    }
    
    static void makePoisonous(Mob mob, ServerLevel ignoredLevel, String customName) {
        prepareEnhancements(customName, POISONOUS, mob);
        mob.addEffect(new MobEffectInstance(MobEffects.REGENERATION, -1));
        mob.addEffect(new MobEffectInstance(MobEffects.POISON, -1));
    }
    
    static void makeLucky(Mob mob, ServerLevel ignoredLevel, String customName) {
        prepareEnhancements(customName, LUCKY, mob);
    }
    
    static void makeInflamed(Mob mob, ServerLevel level, String customName) {
        String name = prepareEnhancements(customName, INFLAMED_BLAZE, mob);
        safeAddPermanentModifier(mob, Attributes.ATTACK_KNOCKBACK, name, level.getDifficulty().getId() + 1f, ADD_VALUE);
        safeAddPermanentModifier(mob, Attributes.SCALE, name, 1f, ADD_MULTIPLIED_TOTAL);
        safeAddPermanentModifier(mob, Attributes.MAX_HEALTH, name, level.getDifficulty().getId() * 3f + 1f, ADD_VALUE);
        mob.setHealth(mob.getMaxHealth());
    }
    
    static void makeGale(Mob mob, ServerLevel level, String customName) {
        String name = prepareEnhancements(customName, GALE_BREEZE, mob);
        safeAddPermanentModifier(mob, Attributes.ATTACK_KNOCKBACK, name, level.getDifficulty().getId() + 1f, ADD_VALUE);
        safeAddPermanentModifier(mob, Attributes.SCALE, name, 1f, ADD_MULTIPLIED_TOTAL);
        safeAddPermanentModifier(mob, Attributes.MAX_HEALTH, name, level.getDifficulty().getId() * 3f + 1f, ADD_VALUE);
        mob.setHealth(mob.getMaxHealth());
    }
    
    static void makeBlastProof(Mob mob, ServerLevel ignoredLevel, String customName) {
        prepareEnhancements(customName, BLAST_PROOF, mob);
    }
    
    static void makeAquatic(Mob mob, ServerLevel level, String customName) {
        String name = prepareEnhancements(customName, AQUATIC, mob);
        safeAddPermanentModifier(mob, Attributes.WATER_MOVEMENT_EFFICIENCY, name, level.getDifficulty().getId() * 0.01f + 0.03f, ADD_MULTIPLIED_BASE);
        safeAddPermanentModifier(mob, Attributes.OXYGEN_BONUS, name, level.getDifficulty().getId() + 1f, ADD_MULTIPLIED_BASE);
        safeAddPermanentModifier(mob, NeoForgeMod.SWIM_SPEED, name, level.getDifficulty().getId() * 0.1f + 0.03f, ADD_MULTIPLIED_BASE);
    }
    
    static void makeGiant(Mob mob, ServerLevel level, String customName) {
        String name = prepareEnhancements(customName, RSAttachments.GIANT, mob);
        safeAddPermanentModifier(mob, Attributes.MAX_HEALTH, name, 2 * level.getDifficulty().getId() + 3, ADD_MULTIPLIED_TOTAL);
        mob.setHealth(mob.getMaxHealth());
        safeAddPermanentModifier(mob, Attributes.SCALE, name, level.getDifficulty().getId() * 0.1f + 0.05f, ADD_MULTIPLIED_BASE);
    }
    
    private static void makeRapidlyFiring(Mob mob, ServerLevel level, String customName) {
        String name = prepareEnhancements(customName, RAPIDLY_FIRING, mob);
        // Probably doesn't do anything. See mixins for actual implementation
        safeAddPermanentModifier(mob, Attributes.ATTACK_SPEED, name, level.getDifficulty().getId() + 3, ADD_MULTIPLIED_TOTAL);
    }
    
    static void makeRavenous(Mob mob, ServerLevel ignoredLevel, String customName) {
        prepareEnhancements(customName, RAVENOUS, mob);
    }
    
    static void makeFarsighted(Mob mob, ServerLevel level, String customName) {
        String name = prepareEnhancements(customName, FAR_SIGHTED, mob);
        safeAddPermanentModifier(mob, Attributes.FOLLOW_RANGE, name, level.getDifficulty().getId() + 3, ADD_MULTIPLIED_BASE);
    }
    
    static void makeVoidInfused(Mob mob, ServerLevel level, String customName) {
        String name = prepareEnhancements(customName, VOID_FUSED, mob);
        safeAddPermanentModifier(mob, Attributes.SCALE, name, level.getDifficulty().getId() * 0.1f, ADD_MULTIPLIED_BASE);
        safeAddPermanentModifier(mob, Attributes.MAX_HEALTH, name, level.getDifficulty().getId() + 1, ADD_MULTIPLIED_BASE);
        mob.setHealth(mob.getMaxHealth());
        safeAddPermanentModifier(mob, Attributes.MOVEMENT_SPEED, name, level.getDifficulty().getId() * 0.01f + 0.03f, ADD_VALUE);
    }
    
    static void makeRadiant(Mob mob, ServerLevel ignoredLevel, String customName) {
        prepareEnhancements(customName, RADIANT, mob);
    }
    
    static String prepareEnhancements(String customName, Supplier<AttachmentType<Boolean>> attachment, Mob mob) {
        mob.setData(attachment, true);
        mob.setCustomName(Component.literal(customName));
        mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, -1));
        
        // Theoretically better to hardcode or buffer this, but who has time for that?
        return customName.toLowerCase().replace(' ', '_');
    }
    
    public static void safeAddPermanentModifier(LivingEntity entity, Holder<Attribute> attribute, String name, float amount, AttributeModifier.Operation operation) {
        AttributeInstance attributeInstance = entity.getAttribute(attribute);
        if (null != attributeInstance) {
            attributeInstance.addOrReplacePermanentModifier(new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath(MODID, name),
                    amount,
                    operation)
            );
        }
    }
}
