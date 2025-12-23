package com.github.no_name_provided.nnp_rune_smithing.common.events;

import com.github.no_name_provided.nnp_rune_smithing.common.data_components.RunesAdded;
import com.github.no_name_provided.nnp_rune_smithing.common.items.runes.AbstractRuneItem;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.windcharge.BreezeWindCharge;
import net.minecraft.world.entity.projectile.windcharge.WindCharge;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.ArmorHurtEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.Map;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;
import static com.github.no_name_provided.nnp_rune_smithing.common.attachments.RSAttachments.*;
import static com.github.no_name_provided.nnp_rune_smithing.common.data_components.RSDataComponents.RUNES_ADDED;
import static com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems.*;
import static net.minecraft.SharedConstants.TICKS_PER_SECOND;
import static net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge.EXPLOSION_DAMAGE_CALCULATOR;

@EventBusSubscriber(modid = MODID)
public class CombatEvents {
    @SubscribeEvent
    static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity attacked = event.getEntity();
        if (attacked.getExistingData(LUCKY.get()).orElse(false) && attacked.level().random.nextInt(2) < 1) {
            Vec3 sourcePosition = event.getSource().getSourcePosition();
            if (null != sourcePosition) {
                attacked.knockback(0.6, sourcePosition.x() - attacked.getX(), sourcePosition.z() - attacked.getZ());
            }
            
            event.setCanceled(true);
        }
        if (attacked.getExistingData(INVERTED.get()).orElse(false)) {
            if (!(attacked instanceof Player) && event.getSource().getDirectEntity() instanceof Player player) {
                // Reverse damage direction
                player.hurt(new DamageSource(event.getSource().typeHolder(), attacked, player, attacked.position()), event.getAmount());
                // Reverse knockback direction (lazy implementation that doesn't throw event or check player (knockback) strength
                Vec3 sourcePosition = event.getSource().getSourcePosition();
                if (null != sourcePosition) {
                    attacked.knockback(0.6, attacked.getX() - sourcePosition.x(), attacked.getZ() - sourcePosition.z());
                }
                
                event.setCanceled(true);
            }
        }
    }
    
    @SubscribeEvent
    static void onLivingDamagePre(LivingDamageEvent.Pre event) {
        DamageSource source = event.getSource();
        if (source.isDirect()) {
            // Handle attacker using runic weapon
            ItemStack weapon = source.getWeaponItem();
            if (null != weapon) {
                RunesAdded runesAdded = weapon.get(RUNES_ADDED);
                if (null != runesAdded && runesAdded.effectiveTier() > 0) {
                    boolean isInverted = runesAdded.modifier().rune() == INVERT_RUNE.get();
                    DamageContainer container = event.getContainer();
                    
                    // Collisions
                    if (runesAdded.target().rune() == COLLISION_RUNE.get()) {
                        AbstractRuneItem effect = runesAdded.effect().rune();
                        if (effect == EARTH_RUNE.get()) {
                            container.addModifier(
                                    DamageContainer.Reduction.ARMOR,
                                    (con, old) ->
                                            !isInverted ? old / runesAdded.effectiveTier() : old * runesAdded.effectiveTier()
                            );
                        }
                    }
                    
                    // Wield effects
                    if (runesAdded.target().rune() == WIELD_RUNE.get()) {
                        AbstractRuneItem effect = runesAdded.effect().rune();
                        if (effect == FIRE_RUNE.get()) {
                            // Consider moving this to incoming damage, so armor effects apply
                            container.setNewDamage(
                                    isInverted ? container.getNewDamage() + runesAdded.effectiveTier() :
                                            Mth.clamp(
                                                    container.getNewDamage() - runesAdded.effectiveTier(),
                                                    0, container.getNewDamage()
                                            )
                            );
                        }
                    }
                }
            }
        }
    }
    
    @SubscribeEvent
    static void onLivingDamagePost(LivingDamageEvent.Post event) {
        DamageSource source = event.getSource();
        if (source.isDirect()) {
            LivingEntity attacked = event.getEntity();
            
            // Handle runic mobs as attackers
            if (source.getEntity() instanceof Mob attacker) {
                if (attacker.getExistingData(RAVENOUS).orElse(false)) {
                    attacker.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 5, 3));
                    attacker.level().playLocalSound(attacker, SoundEvents.PANDA_BITE, SoundSource.HOSTILE, 1.0f, 1.0f);
                } else if (attacker.getExistingData(POISONOUS).orElse(false)) {
                    attacked.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 3, 2));
                } else if (attacker.getExistingData(VOID_FUSED).orElse(false)) {
                    if (event.getNewDamage() > 1) {
                        attacker.addEffect(new MobEffectInstance(MobEffects.HEAL, 1, 2));
                    }
                    attacker.level().playLocalSound(attacker, SoundEvents.WARDEN_ATTACK_IMPACT, SoundSource.HOSTILE, 5.0f, (attacker.getRandom().nextFloat() - attacker.getRandom().nextFloat()) * 0.2f + 1.0f);
                    attacked.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20 * 10, 2));
                    attacked.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20 * 10, 2, false, false));
                    attacked.addEffect(new MobEffectInstance(MobEffects.WITHER, 20 * 5));
                    attacked.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20 * 10, 2));
                }
            }
            
            
            // Handle attacker using runic weapon
            ItemStack weapon = source.getWeaponItem();
            if (null != weapon) {
                RunesAdded runesAdded = weapon.get(RUNES_ADDED);
                if (null != runesAdded && runesAdded.effectiveTier() > 0) {
                    boolean isInverted = runesAdded.modifier().rune() == INVERT_RUNE.get();
                    // Collisions
                    if (runesAdded.target().rune() == COLLISION_RUNE.get()) {
                        AbstractRuneItem effect = runesAdded.effect().rune();
                        if (effect == WARD_RUNE.get()) {
                            attacked.addEffect(
                                    new MobEffectInstance(
                                            MobEffects.ABSORPTION,
                                            TICKS_PER_SECOND * 60 * runesAdded.effectiveTier(),
                                            runesAdded.effectiveTier()
                                    )
                            );
                        } else if (effect == SIGHT_RUNE.get() && attacked instanceof LivingEntity livingAttacked) {
                            livingAttacked.addEffect(
                                    new MobEffectInstance(
                                            !isInverted ? MobEffects.BLINDNESS : MobEffects.GLOWING,
                                            TICKS_PER_SECOND * 3 * runesAdded.effectiveTier(),
                                            runesAdded.effectiveTier()
                                    )
                            );
                        } else if (effect == WATER_RUNE.get()) {
                            attacked.addEffect(
                                    new MobEffectInstance(
                                            !isInverted ? MobEffects.WATER_BREATHING : MobEffects.FIRE_RESISTANCE,
                                            TICKS_PER_SECOND * 60 * runesAdded.effectiveTier(),
                                            runesAdded.effectiveTier()
                                    )
                            );
                        } else if (effect == FIRE_RUNE.get() && !attacked.fireImmune()) {
                            if (!isInverted) {
                                attacked.setRemainingFireTicks(20 * 10 * runesAdded.effectiveTier());
                            } else {
                                attacked.setRemainingFireTicks(0);
                            }
                        }
                    }
                }
            }
            
            // Handle attacked having runic armor
            attacked.getArmorSlots().forEach(armor -> {
                RunesAdded runesAdded = armor.get(RUNES_ADDED);
                if (null != runesAdded) {
                    boolean isInverted = runesAdded.modifier().rune() == INVERT_RUNE.get();
                    int tier = runesAdded.effectiveTier();
                    if (runesAdded.target().rune() == COLLISION_RUNE.get()) {
                        Entity attacker = source.getEntity();
                        if (null != attacker) {
                            AbstractRuneItem effect = runesAdded.effect().rune();
                            if (effect == WARD_RUNE.get() && attacker instanceof LivingEntity livingAttacker) {
                                livingAttacker.addEffect(
                                        new MobEffectInstance(
                                                MobEffects.ABSORPTION,
                                                TICKS_PER_SECOND * 60 * runesAdded.effectiveTier(),
                                                runesAdded.effectiveTier()
                                        )
                                );
                            } else if (effect == SIGHT_RUNE.get() && attacker instanceof LivingEntity livingAttacker) {
                                livingAttacker.addEffect(
                                        new MobEffectInstance(
                                                !isInverted ? MobEffects.GLOWING : MobEffects.DARKNESS,
                                                TICKS_PER_SECOND * 10 * tier
                                        )
                                );
                            } else if (effect == AIR_RUNE.get() && attacked.level() instanceof Level level) {
                                //noinspection StatementWithEmptyBody
                                if (!isInverted) {
                                    level.explode(
                                            attacked,
                                            null,
                                            EXPLOSION_DAMAGE_CALCULATOR,
                                            attacked.getX(),
                                            attacked.getY(),
                                            attacked.getZ(),
                                            tier,
                                            false,
                                            Level.ExplosionInteraction.TRIGGER,
                                            ParticleTypes.GUST_EMITTER_SMALL,
                                            ParticleTypes.GUST_EMITTER_LARGE,
                                            SoundEvents.BREEZE_WIND_CHARGE_BURST
                                    );
                                } else {
                                    // TODO: think of a good inversion effect
                                }
                            } else if (effect == WATER_RUNE.get() && attacker.canFreeze()) {
                                if (!isInverted) {
                                    attacker.setTicksFrozen(attacker.getTicksRequiredToFreeze());
                                } else {
                                    attacked.setRemainingFireTicks(20 * 10 * runesAdded.effectiveTier());
                                }
                            } else if (effect == FIRE_RUNE.get() && !attacker.fireImmune()) {
                                if (!isInverted) {
                                    attacker.setRemainingFireTicks(20 * 10 * runesAdded.effectiveTier());
                                } else {
                                    attacker.setTicksFrozen(attacker.getTicksRequiredToFreeze());
                                }
                            } else if (effect == EARTH_RUNE.get() && attacker instanceof LivingEntity livingAttacker) {
                                livingAttacker.addEffect(
                                        new MobEffectInstance(
                                                !isInverted ? MobEffects.POISON : MobEffects.REGENERATION,
                                                20 * 2 * tier,
                                                tier
                                        )
                                );
                            }
                        }
                    }
                }
            });
        }
    }
    
    @SubscribeEvent
    static void EntityInvulnerabilityCheckEvent(EntityInvulnerabilityCheckEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Mob mob) {
            if (mob.getExistingData(BLAST_PROOF).orElse(false) && (event.getSource().is(DamageTypes.EXPLOSION) || event.getSource().is(DamageTypes.PLAYER_EXPLOSION))) {
                event.setInvulnerable(true);
            }
        }
    }
    
    /**
     * Could stand to be reworked. Makes Runic Breeze wind charges fire secondary charges on impact... technically.
     * There aren't many, and they tend to disappear quickly.
     */
    @SubscribeEvent
    static void onProjectileImpact(ProjectileImpactEvent event) {
        if (event.getProjectile() instanceof BreezeWindCharge charge &&
                charge.getOwner() instanceof Breeze breeze &&
                breeze.getExistingData(GALE_BREEZE.get()).orElse(false)) {
            Entity pEntity = event.getEntity();
            Level level = pEntity.level();
            Direction.allShuffled(breeze.getRandom()).forEach(direction -> {
                WindCharge newCharge = new WindCharge(level, pEntity.getX(), pEntity.getY(), pEntity.getZ(), new Vec3(0, 0, 0));
                newCharge.shoot(direction.getStepX(), direction.getStepY(), direction.getStepZ(), 0.7f, (float) (5 - level.getDifficulty().getId() * 4));
                level.addFreshEntity(newCharge);
            });
        }
    }
    
    @SubscribeEvent
    static void onArmorHurt(ArmorHurtEvent event) {
        // Flat reductions of even 1/tier are too powerful
        float damageReductionPerTier = Mth.randomBetween(event.getEntity().getRandom(), 0f, 1.0f);
        Map<EquipmentSlot, ArmorHurtEvent.ArmorEntry> map = event.getArmorMap();
        map.keySet().forEach(slot -> {
            RunesAdded runes = map.get(slot).armorItemStack.get(RUNES_ADDED);
            if (null != slot && null != runes && runes.target().rune() == SELF_RUNE.get() && runes.effect().rune() == WARD_RUNE.get()) {
                event.setNewDamage(slot, Mth.clamp(map.get(slot).originalDamage, 0, Math.max(0, map.get(slot).originalDamage - runes.effectiveTier() * damageReductionPerTier)));
            }
        });
    }
}
