package com.github.no_name_provided.nnp_rune_smithing.datagen.providers;

import com.github.no_name_provided.nnp_rune_smithing.common.attachments.RSAttachments;
import com.github.no_name_provided.nnp_rune_smithing.common.items.CastingTemplate;
import com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems;
import com.github.no_name_provided.nnp_rune_smithing.datagen.loot_conditions.BooleanAttachmentTrue;
import com.github.no_name_provided.nnp_rune_smithing.datagen.providers.subproviders.global_loot_modifiers.SingleItemPools;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.loot.AddTableLootModifier;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.concurrent.CompletableFuture;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;
import static com.github.no_name_provided.nnp_rune_smithing.datagen.providers.RSBlockTagProvider.INVENTORY_BLOCKS;

@SuppressWarnings("unused") // We have several helper methods, any of which may be unused in some commits
public class RSGlobalLootModifierProvider extends net.neoforged.neoforge.common.data.GlobalLootModifierProvider {
    public RSGlobalLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, MODID);
    }
    
    /**
     * Call {@link #add} here, which will pass in the necessary information to write the JSONs.
     */
    @Override
    protected void start() {
        // Templates from chests and stuff
        createSimpleAdditionByID("warrior_charm_blacksmith", BuiltInLootTables.VILLAGE_TOOLSMITH.location(), RSItems.WARRIOR_CHARM, 20);
        createSimpleAdditionByID("health_charm_armourer", BuiltInLootTables.VILLAGE_ARMORER.location(), RSItems.WARRIOR_CHARM, 20);
        createSimpleAdditionByID("luck_charm_temple", BuiltInLootTables.VILLAGE_TEMPLE.location(), RSItems.HEALTH_CHARM, 20);
        
        createSimpleAdditionByID("self_template_pyramid", BuiltInLootTables.DESERT_PYRAMID.location(), RSItems.SELF_TEMPLATE, 2);
        createSimpleAdditionByID("collision_template_jungle_temple", BuiltInLootTables.JUNGLE_TEMPLE.location(), RSItems.COLLISION_TEMPLATE);
        createSimpleAdditionByID("wield_template_mineshaft", BuiltInLootTables.ABANDONED_MINESHAFT.location(), RSItems.WIELD_TEMPLATE, 3);
        createSimpleAdditionByID("tunnel_template_sniffer", BuiltInLootTables.SNIFFER_DIGGING.location(), RSItems.TUNNEL_TEMPLATE, 30);
        
        createSimpleAdditionByID("sight_template_trial_chambers_corridor", BuiltInLootTables.TRIAL_CHAMBERS_CORRIDOR.location(), RSItems.SIGHT_TEMPLATE, 7);
        createSimpleAdditionByID("ward_template_woodland_mansion", BuiltInLootTables.WOODLAND_MANSION.location(), RSItems.WARD_TEMPLATE, 5);
        createSimpleAdditionByID("ward_template_desert_well_archaeology", BuiltInLootTables.DESERT_WELL_ARCHAEOLOGY.location(), RSItems.WARD_TEMPLATE, 3);
        createSimpleAdditionByID("serendipity_template_from_cat", BuiltInLootTables.CAT_MORNING_GIFT.location(), RSItems.SERENDIPITY_TEMPLATE, 7);
        
        createSimpleAdditionByID("water_template_buried_treasure", BuiltInLootTables.BURIED_TREASURE.location(), RSItems.WATER_TEMPLATE);
        createSimpleAdditionByID("earth_template_stronghold_library", BuiltInLootTables.STRONGHOLD_LIBRARY.location(), RSItems.EARTH_TEMPLATE, 10);
        createSimpleAdditionByID("fire_template_bastion_treasure", BuiltInLootTables.BASTION_TREASURE.location(), RSItems.FIRE_TEMPLATE, 5);
        createSimpleAdditionByID("air_template_pillager_outpost", BuiltInLootTables.PILLAGER_OUTPOST.location(), RSItems.AIR_TEMPLATE, 10);
        createSimpleAdditionByID("void_template_end_city_treasure", BuiltInLootTables.END_CITY_TREASURE.location(), RSItems.VOID_TEMPLATE, 10);
        createSimpleAdditionByID("light_template_bastion_treasure", BuiltInLootTables.BASTION_TREASURE.location(), RSItems.LIGHT_TEMPLATE, 5);
        
        createSimpleAdditionByID("widen_template_fortress", BuiltInLootTables.NETHER_BRIDGE.location(), RSItems.WIDEN_TEMPLATE, 9);
        createSimpleAdditionByID("narrow_template_fortress", BuiltInLootTables.NETHER_BRIDGE.location(), RSItems.NARROW_TEMPLATE, 9);
        createSimpleAdditionByID("time_template_ominious_trial", BuiltInLootTables.SPAWNER_TRIAL_ITEMS_TO_DROP_WHEN_OMINOUS.location(), RSItems.TIME_TEMPLATE, 10);
        createSimpleAdditionByID("invert_template_ancient_city_icebox", BuiltInLootTables.ANCIENT_CITY_ICE_BOX.location(), RSItems.INVERT_TEMPLATE, 1);
        
        createSimpleAdditionByID("amplify_template_end_treasure", BuiltInLootTables.END_CITY_TREASURE.location(), RSItems.AMPLIFY_TEMPLATE, 2);
        
        // Runes from mobs
        createSimpleAdditionByEntityAttachment("warrior_charm_robust", RSAttachments.ROBUST.get(), RSItems.WARRIOR_CHARM);
        createSimpleAdditionByEntityAttachment("health_charm_poisonous", RSAttachments.POISONOUS.get(), RSItems.HEALTH_CHARM);
        createSimpleAdditionByEntityAttachment("luck_charm_lucky", RSAttachments.LUCKY.get(), RSItems.LUCK_CHARM);
        
        createSimpleAdditionByEntityAttachment("ward_from_blast_proof", RSAttachments.BLAST_PROOF.get(), RSItems.WARD_RUNE);
        createSimpleAdditionByEntityAttachment("sight_from_far_sighted", RSAttachments.FAR_SIGHTED.get(), RSItems.SIGHT_RUNE);
        createSimpleAdditionByEntityAttachment("serendipity_villager", RSAttachments.SERENDIPITOUS_BIPED.get(), RSItems.SERENDIPITY_RUNE);
        createSimpleAdditionByEntityAttachment("water_from_aquatic", RSAttachments.AQUATIC.get(), RSItems.WATER_RUNE);
        createSimpleAdditionByEntityAttachment("earth_from_ravenous", RSAttachments.RAVENOUS.get(), RSItems.EARTH_RUNE);
        createSimpleAdditionByEntityAttachment("fire_from_inflamed", RSAttachments.INFLAMED_BLAZE.get(), RSItems.FIRE_RUNE);
        createSimpleAdditionByEntityAttachment("air_from_gale", RSAttachments.GALE_BREEZE.get(), RSItems.AIR_RUNE);
        
        createSimpleAdditionByEntityAttachment("widen_from_giant", RSAttachments.GIANT.get(), RSItems.WIDEN_RUNE);
        createSimpleAdditionByEntityAttachment("time_from_rapidly_firing", RSAttachments.RAPIDLY_FIRING.get(), RSItems.TIME_RUNE);
        createSimpleAdditionByEntityAttachment("narrow_from_tiny", RSAttachments.TINY.get(), RSItems.NARROW_RUNE);
        createSimpleAdditionByEntityAttachment("invert_from_inverted", RSAttachments.INVERTED.get(), RSItems.INVERT_RUNE);
        
        createSimpleAdditionByEntityAttachment("contained_from_contained", RSAttachments.CONTAINED.get(), RSItems.CONTAIN_RUNE);
    }
    
    /**
     * Item must already have appropriately named loot table generated by another subprovider.
     */
    void createSimpleAdditionByID(String name, ResourceLocation lootTableID, DeferredHolder<Item, ? extends Item> item, int reciprocalOfOdds) {
        add(
                name,
                new AddTableLootModifier(
                        new LootItemCondition[]{
                                LootTableIdCondition.builder(
                                        lootTableID
                                ).and(LootItemRandomChanceCondition.randomChance(1f / reciprocalOfOdds)).build()
                        },
                        SingleItemPools.getSingleItemLootPoolKey(item)
                )
        );
    }
    
    void createSimpleAdditionByID(String name, ResourceLocation lootTableID, DeferredHolder<Item, CastingTemplate> item) {
        this.createSimpleAdditionByID(name, lootTableID, item, 1);
    }
    
    /**
     * Don't trust.
     */
    void createSimpleChestAdditionByStructure(String name, ResourceKey<Structure> structure, DeferredHolder<Item, CastingTemplate> item, int reciprocalOfOdds) {
        add(
                name,
                new AddTableLootModifier(
                        new LootItemCondition[]{
                                LocationCheck.checkLocation(LocationPredicate.Builder.inStructure(
                                                registries.lookupOrThrow(Registries.STRUCTURE).getOrThrow(structure)
                                        ).setBlock(BlockPredicate.Builder.block().of(INVENTORY_BLOCKS))
                                ).build()
//                                new LootItemBlockStatePropertyCondition.Builder(Blocks.CHEST).or(new LootItemBlockStatePropertyCondition.Builder(Blocks.BARREL)).build()
                        },
                        SingleItemPools.getSingleItemLootPoolKey(item)
                )
        );
    }
    
    /**
     * Known to not work.
     */
    void createSimpleStorageEntityAdditionByStructure(String name, ResourceKey<Structure> structure, DeferredHolder<Item, CastingTemplate> item, int ignoredReciprocalOfOdds) {
        add(
                name,
                new AddTableLootModifier(
                        new LootItemCondition[]{
                                LocationCheck.checkLocation(LocationPredicate.Builder.inStructure(
                                                registries.lookupOrThrow(Registries.STRUCTURE).getOrThrow(structure)
                                        )
                                ).and(
                                        LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(EntityType.CHEST_MINECART)))
                                ).build()
//                                new LootItemBlockStatePropertyCondition.Builder(Blocks.CHEST).or(new LootItemBlockStatePropertyCondition.Builder(Blocks.BARREL)).build()
                        },
                        SingleItemPools.getSingleItemLootPoolKey(item)
                )
        );
    }
    
    /**
     * Creates a simple loot table addition by adding a roll from a pregenerated single item loot table. This roll is
     * conditional on a Boolean data attachment being present and true.
     * @param name Loot table ID.
     * @param attachment The attachment to test.
     * @param item Item to drop.
     */
    void createSimpleAdditionByEntityAttachment(String name, AttachmentType<Boolean> attachment, DeferredHolder<Item, ? extends Item> item) {
        add(
                name,
                new AddTableLootModifier(
                        new LootItemCondition[]{
                            new BooleanAttachmentTrue(attachment)
                        },
                        SingleItemPools.getSingleItemLootPoolKey(item)
                )
        );
    }
}


//add(
//        name,
//                new AddTableLootModifier(
//                new LootItemCondition[]{
//    LocationCheck.checkLocation(LocationPredicate.Builder.inStructure(
//                    registries.lookupOrThrow(Registries.STRUCTURE).getOrThrow(structure)
//            )
//    ).build(),
//            new LootItemBlockStatePropertyCondition.Builder(Blocks.CHEST).or(new LootItemBlockStatePropertyCondition.Builder(Blocks.BARREL)).build()
//},
//        SingleItemPools.getTemplateKey(item, reciprocalOfOdds)
//                )
//                        );



//.setBlock(
//        BlockPredicate.Builder.block().of(Tags.Blocks.CHESTS)
//                                        )