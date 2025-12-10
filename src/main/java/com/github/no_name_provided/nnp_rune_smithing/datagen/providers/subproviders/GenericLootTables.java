package com.github.no_name_provided.nnp_rune_smithing.datagen.providers.subproviders;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.function.BiConsumer;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;
import static com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems.*;

public class GenericLootTables implements LootTableSubProvider {
    public static final ResourceKey<LootTable> STARTER_PACKAGE_CARVED_RUNES = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(MODID, "starter_package_carved_runes"));
    public static final ResourceKey<LootTable> STARTER_PACKAGE_CAST_RUNES = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(MODID, "starter_package_cast_runes"));
    
    public GenericLootTables(HolderLookup.Provider ignoredProvider) {
    }
    
    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer) {
        consumer.accept(
                STARTER_PACKAGE_CARVED_RUNES,
                new LootTable.Builder()
                        .withPool(
                                LootPool.lootPool()
                                        .add(LootItem.lootTableItem(WARRIOR_CHARM.get()))
                                        .add(LootItem.lootTableItem(LUCK_CHARM.get()))
                                        .add(LootItem.lootTableItem(HEALTH_CHARM.get()))
                                        .setRolls(ConstantValue.exactly(1))
                        ).withPool(
                                LootPool.lootPool()
                                        .add(LootItem.lootTableItem(SELF_TEMPLATE.get()))
                                        .add(LootItem.lootTableItem(WIELD_TEMPLATE.get()))
                                        .add(LootItem.lootTableItem(COLLISION_TEMPLATE.get()))
                                        .setRolls(ConstantValue.exactly(1))
                        )
        );
        consumer.accept(
                STARTER_PACKAGE_CAST_RUNES,
                new LootTable.Builder().withPool(
                        LootPool.lootPool()
                                .add(LootItem.lootTableItem(WARD_TEMPLATE.get()))
                                .setRolls(ConstantValue.exactly(1))
                )
        );
    }
}
