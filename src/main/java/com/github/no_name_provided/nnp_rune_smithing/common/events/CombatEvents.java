package com.github.no_name_provided.nnp_rune_smithing.common.events;

import com.github.no_name_provided.nnp_rune_smithing.client.particles.RSParticleTypes;
import com.github.no_name_provided.nnp_rune_smithing.common.RSServerConfig;
import com.github.no_name_provided.nnp_rune_smithing.common.attachments.RSAttachments;
import com.github.no_name_provided.nnp_rune_smithing.common.blocks.RuneBlock;
import com.github.no_name_provided.nnp_rune_smithing.common.data_components.RunesAdded;
import com.github.no_name_provided.nnp_rune_smithing.common.items.runes.AbstractRuneItem;
import com.github.no_name_provided.nnp_rune_smithing.common.saved_data.SerendipityRuneLocations;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.windcharge.BreezeWindCharge;
import net.minecraft.world.entity.projectile.windcharge.WindCharge;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;
import static com.github.no_name_provided.nnp_rune_smithing.common.attachments.RSAttachments.*;
import static com.github.no_name_provided.nnp_rune_smithing.common.data_components.RSDataComponents.RUNES_ADDED;
import static com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems.*;
import static net.minecraft.SharedConstants.TICKS_PER_SECOND;
import static net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge.EXPLOSION_DAMAGE_CALCULATOR;

@EventBusSubscriber(modid = MODID)
public class CombatEvents {
    public static Long BLINDING_FLASH_DURATION = 20 * 10L;
    // All damage events seem to be implicitly behind a serverlevel check in #hurt
    
    @SubscribeEvent
    static void EntityInvulnerabilityCheckEvent(EntityInvulnerabilityCheckEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Mob mob) {
            if (mob.getExistingData(BLAST_PROOF).orElse(false) && (event.getSource().is(DamageTypes.EXPLOSION) || event.getSource().is(DamageTypes.PLAYER_EXPLOSION))) {
                event.setInvulnerable(true);
            }
        }
        Entity attacker = event.getSource().getDirectEntity();
        if (attacker instanceof LivingEntity livingAttacker && !event.getSource().type().equals(attacker.damageSources().fellOutOfWorld().type())) {
            RunesAdded runes = livingAttacker.getWeaponItem().get(RUNES_ADDED);
            if (null != runes && runes.effect().rune() == VOID_RUNE.get()) {
                // Make sure my conditional code in LivingIncomingDamage is reached
                // Hopefully this doesn't run afoul of the "technical limitations" with conditional invulnerability
                event.setInvulnerable(false);
            }
        }
    }
    
    @SubscribeEvent
    static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity attacked = event.getEntity();
        // Handle runic mobs
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
        
        // Handle runic equipment
        Entity attacker = event.getSource().getDirectEntity();
        if (attacker instanceof LivingEntity livingAttacker && !event.getSource().type().equals(attacker.damageSources().fellOutOfWorld().type())) {
            RunesAdded runes = livingAttacker.getWeaponItem().get(RUNES_ADDED);
            if (null != runes && runes.target().rune() == SELF_RUNE.get() && runes.effect().rune() == VOID_RUNE.get() && runes.amplifier().rune() != CONTAIN_RUNE.get()) {
                // Conditionally cancel (reason this isn't in invulnerability check) event and replace with void damage
                attacked.hurt(attacker.damageSources().source(DamageTypes.FELL_OUT_OF_WORLD, livingAttacker), event.getAmount());
                
                event.setCanceled(true);
            }
        }
    }
    
    /**
     * Here we add extra blocked damage when the main hand item wards the wielder and attempt to apply the damage from
     * this extra block only to the that item (and not any shield in use).
     */
    @SubscribeEvent
    static void onLivingShieldBlock(LivingShieldBlockEvent event) {
        ItemStack tool = event.getEntity().getMainHandItem();
        if (!tool.isEmpty()) {
            RunesAdded runes = tool.get(RUNES_ADDED);
            if (null != runes && runes.target().rune() == WIELD_RUNE.get() && runes.effect().rune() == WARD_RUNE.get() && runes.amplifier().rune() != CONTAIN_RUNE.get()) {
                float damageChange = 1f * runes.effectiveTier();
                if (runes.effect().rune() != INVERT_RUNE.get()) {
                    // internally clamped
                    event.setBlockedDamage(event.getBlockedDamage() + damageChange);
                    tool.hurtAndBreak(Math.round(damageChange), event.getEntity(), EquipmentSlot.MAINHAND);
                    // Negative values will apply all blocked damage to shield if entity is actively blocking
                    event.setShieldDamage(Math.max(event.shieldDamage(), 0));
                } else {
                    // internally clamped
                    event.setBlockedDamage(event.getBlockedDamage() - damageChange);
                    // Internally clamped. Since we're inverting the benefit, we might as well invert the cost
                    tool.setDamageValue(tool.getDamageValue() - Math.round(damageChange));
                    // Negative values will apply all blocked damage to shield if entity is actively blocking
                    event.setShieldDamage(Math.max(event.shieldDamage(), 0));
                }
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
                if (null != runesAdded && runesAdded.effectiveTier() > 0 && runesAdded.amplifier().rune() != CONTAIN_RUNE.get()) {
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
                                    !isInverted ? container.getNewDamage() + runesAdded.effectiveTier() :
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
                if (null != runesAdded && runesAdded.effectiveTier() > 0 && runesAdded.amplifier().rune() != CONTAIN_RUNE.get()) {
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
            AtomicBoolean alreadyTeleportedAttacked = new AtomicBoolean(false);
            AtomicBoolean alreadyTeleportedAttacker = new AtomicBoolean(false);
            attacked.getArmorSlots().forEach(armor -> {
                RunesAdded runesAdded = armor.get(RUNES_ADDED);
                if (null != runesAdded && runesAdded.amplifier().rune() != CONTAIN_RUNE.get()) {
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
                            } else if (effect == SERENDIPITY_RUNE.get()) {
                                Vec3 attackerLocation = attacker.getEyePosition();
                                attacked.lookAt(EntityAnchorArgument.Anchor.EYES, !isInverted ? attackerLocation : attackerLocation.reverse());
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
                                    // TODO: think of a good inverted effect
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
                            } else if (runesAdded.effect().rune() == LIGHT_RUNE.get() && attacker instanceof ServerPlayer player) {
                                player.setData(BLINDING_FLASH_TIME, BLINDING_FLASH_DURATION * tier);
                            } else if (runesAdded.effect().rune() == VOID_RUNE.get()) {
                                if (!attacked.level().isClientSide()) {
                                    int range = 3 + 5 * runesAdded.effectiveTier();
                                    if (!isInverted && !alreadyTeleportedAttacker.get() && attacker instanceof LivingEntity livingAttacker) {
                                        randomTeleport(livingAttacker, range);
                                        alreadyTeleportedAttacker.set(true);
                                    } else if (!alreadyTeleportedAttacked.get()) {
                                        randomTeleport(attacked, range);
                                        alreadyTeleportedAttacked.set(true);
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }
    }
    
    /**
     * Randomly teleports target to a nearby location, with some safeguards.
     *
     * <p>
     * Reference: net.minecraft.world.item.ChorusFruitItem.finishUsingItem. We don't throw the chorus fruit event
     * because we aren't actually feeding players chorus
     * </p>
     *
     * @param toTeleport The living entity to teleport.
     */
    private static void randomTeleport(LivingEntity toTeleport, int range) {
        Level level = toTeleport.level();
        for (int i = 0; i < 16; i++) {
            double targetX = toTeleport.getX() + (toTeleport.getRandom().nextDouble() - 0.5) * 2 * range;
            double targetY = Mth.clamp(
                    toTeleport.getY() + (double) (toTeleport.getRandom().nextInt(2 * range) - range),
                    level.getMinBuildHeight(),
                    (level.getMinBuildHeight() + ((ServerLevel) level).getLogicalHeight() - 1)
            );
            double targetZ = toTeleport.getZ() + (toTeleport.getRandom().nextDouble() - 0.5) * 2 * range;
            if (toTeleport.isPassenger()) {
                toTeleport.stopRiding();
            }
            
            Vec3 vec3 = toTeleport.position();
            if (toTeleport.randomTeleport(targetX, targetY, targetZ, true)) {
                level.gameEvent(GameEvent.TELEPORT, vec3, GameEvent.Context.of(toTeleport));
                
                SoundEvent soundevent;
                soundevent = SoundEvents.CHORUS_FRUIT_TELEPORT;
                SoundSource soundsource;
                if (toTeleport instanceof Player) {
                    soundsource = SoundSource.PLAYERS;
                } else if (toTeleport instanceof Monster) {
                    soundsource = SoundSource.HOSTILE;
                } else {
                    soundsource = SoundSource.NEUTRAL;
                }
                
                level.playSound(null, toTeleport.getX(), toTeleport.getY(), toTeleport.getZ(), soundevent, soundsource);
                toTeleport.resetFallDistance();
                break;
            }
        }
        
        if (toTeleport instanceof Player player) {
            player.resetCurrentImpulseContext();
        }
    }
    
    @SubscribeEvent
    static void onLivingDrops(LivingDropsEvent event) {
        ItemStack weapon = event.getSource().getWeaponItem();
        if (null != weapon && !weapon.isEmpty()) {
            RunesAdded runes = weapon.get(RUNES_ADDED.get());
            if (null != runes && runes.amplifier().rune() != CONTAIN_RUNE.get()) {
                if (runes.getByType(AbstractRuneItem.Type.TARGET).rune() == COLLISION_RUNE.get()) {
                    if (runes.getByType(AbstractRuneItem.Type.EFFECT).rune() == VOID_RUNE.get()) {
                        // Void drops if void rune collision is activated
                        event.setCanceled(true);
                    }
                }
            }
        }
        
        if (event.getEntity().level() instanceof ServerLevel level) {
            LivingEntity died = event.getEntity();
            SerendipityRuneLocations locations = SerendipityRuneLocations.get(level);
            AtomicReference<Float> effectiveStrength = new AtomicReference<>((float) 0);
            locations.getLocationsAndStrengths().keySet().stream()
                    .filter(location -> location.distanceSquared(died.chunkPosition()) < 64)
                    .forEach(
                            location -> locations.getLocationsAndStrengths().get(location).stream()
                                    // TODO: find a way to check in a square or write my own helper
                                    .filter(pair -> pair.getFirst().distToCenterSqr(died.position().x, died.position().y, died.position().z) < Math.pow(pair.getSecond().get(1), 2))
                                    .forEach(pair -> effectiveStrength.updateAndGet(v -> v + pair.getSecond().getFirst())));
            // The first point of strength is required to activate the extra loot. Any overflow becomes luck.
            if (effectiveStrength.get() > 1f) {
                // Reference: net.minecraft.world.entity.LivingEntity.dropFromLootTable
                LootParams params = new LootParams.Builder(level)
                        .withParameter(LootContextParams.THIS_ENTITY, died)
                        .withParameter(LootContextParams.ORIGIN, died.position())
                        .withParameter(LootContextParams.DAMAGE_SOURCE, event.getSource())
                        .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, event.getSource().getEntity())
                        .withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, event.getSource().getDirectEntity())
                        .withLuck(effectiveStrength.get() - 1)
                        .create(LootContextParamSets.ENTITY);
                level.getServer().reloadableRegistries().getLootTable(died.getLootTable())
                        .getRandomItems(params, died.getLootTableSeed(), died::spawnAtLocation);
                List<Integer> color = RuneBlock.effectToColor.get(SELF_RUNE.get());
                level.sendParticles(
                        ColorParticleOption.create(RSParticleTypes.SELF_RUNE.get(), (float) color.getFirst() / 255, (float) color.get(1) / 255, (float) color.get(2) / 255),
                        died.position().x(),
                        died.position().y() + died.getBbHeight() / 2,
                        died.position().z(),
                        20,
                        0.5,
                        0.5,
                        0.5,
                        0.2
                );
            }
        }
    }
    
    @SubscribeEvent
    static void onLivingXP(LivingExperienceDropEvent event) {
        Player player = event.getAttackingPlayer();
        if (null != player) {
            ItemStack weapon = player.getWeaponItem();
            if (!weapon.isEmpty()) {
                RunesAdded runes = weapon.get(RUNES_ADDED);
                if (null != runes && runes.amplifier().rune() != CONTAIN_RUNE.get()) {
                    if (runes.effect().rune() == LIGHT_RUNE.get()) {
                        event.setDroppedExperience(event.getDroppedExperience() + RSServerConfig.weaponXPPerTier * runes.effectiveTier());
                    }
                }
            }
        }
    }
    
    @SubscribeEvent
    static void onEndermanAnger(EnderManAngerEvent event) {
        if (event.getPlayer().getExistingData(RSAttachments.HIDDEN_BY_VOID).orElse(false)) {
            
            event.setCanceled(true);
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
            if (null != slot && null != runes && runes.target().rune() == SELF_RUNE.get() && runes.effect().rune() == WARD_RUNE.get() && runes.amplifier().rune() != CONTAIN_RUNE.get()) {
                event.setNewDamage(slot, Mth.clamp(map.get(slot).originalDamage, 0, Math.max(0, map.get(slot).originalDamage - runes.effectiveTier() * damageReductionPerTier)));
            }
        });
    }
    
    @SubscribeEvent
    static void onPlayerPickupXP(PlayerXpEvent.PickupXp event) {
        event.getOrb().value *= event.getEntity().getData(PLAYER_XP_MULTIPLIER);
    }
}
