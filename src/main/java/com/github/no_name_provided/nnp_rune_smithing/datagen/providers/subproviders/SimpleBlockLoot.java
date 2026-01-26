package com.github.no_name_provided.nnp_rune_smithing.datagen.providers.subproviders;

import com.github.no_name_provided.nnp_rune_smithing.common.data_components.RSDataComponents;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.Set;

import static com.github.no_name_provided.nnp_rune_smithing.common.blocks.RSBlocks.*;
import static com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems.RAW_ORES;

public class SimpleBlockLoot extends BlockLootSubProvider {
    public SimpleBlockLoot(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.DEFAULT_FLAGS, registries);
    }
    
    // For whatever reason, it is necessary to list the blocks we'll be handling here.
    // Behaves strangely for abnormal blocks (eg, fluid blocks), so they're kept in a different registry.
    // We return an Iterable over our block registry's values here. Anything we don't use throws an error.
    // Defaults to using the entire vanilla block registry, so you usually want to override it.
    @Override
    protected Iterable<Block> getKnownBlocks() {
        ArrayList<Block> iterableBlocks = new ArrayList<>(); // define empty list
        BLOCKS.getEntries().stream().map(DeferredHolder::get).forEach(iterableBlocks::add);
        METAL_STORAGE_BLOCKS.getEntries().stream().map(DeferredHolder::get).forEach(iterableBlocks::add);
        ORE_BLOCKS.getEntries().stream().map(DeferredHolder::get).forEach(iterableBlocks::add);
        return iterableBlocks;
    }
    
    @Override
    protected void generate() {
        METAL_STORAGE_BLOCKS.getEntries().forEach(entry -> {
            dropSelf(entry.get());
        });
        ORE_BLOCKS.getEntries().forEach(entry -> {
            String name = switch (entry.getId().getPath().split("_")[0]) {
                case "deepslate", "endstone", "netherrack" -> entry.getId().getPath().split("_")[1];
                default -> entry.getId().getPath().split("_")[0];
            };
            String rawOreName = "raw_" + name;
            RAW_ORES.getEntries().stream().filter(itemEntry -> itemEntry.getId().getPath().equals(rawOreName)).forEach(itemEntry ->
                    // Should be exactly one match
                    add(entry.get(), createOreDrop(entry.get(), itemEntry.get()))
            );
        });
        
//        dropSelf(WHITTLING_TABLE.get());
        add(WHITTLING_TABLE.get(), this::createWhittlingTableDrops);
//        dropSelf(MELTER.get());
        add(MELTER.get(), this::createMelterDrops);
//        dropSelf(ALLOYER.get());
        add(ALLOYER.get(), this::createAlloyerDrops);
        dropSelf(CASTING_TABLE.get());
        dropSelf(RUNE_ANVIL.get());
        add(RUNE_BLOCK.get(), noDrop());
    }
    
    // Based off of vanilla shulker behavior
    public LootTable.Builder createWhittlingTableDrops(Block block) {
        return LootTable.lootTable()
                .withPool(
                        applyExplosionCondition(
                                block,
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1.0F))
                                        .add(
                                                LootItem.lootTableItem(block)
                                                        .apply(
                                                                CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY)
                                                                        .include(RSDataComponents.ITEMSTACK_HANDLER_INVENTORY.get())
                                                        )
                                        )
                        )
                );
    }
    
    public LootTable.Builder createMelterDrops(Block block) {
        
        return LootTable.lootTable()
                .withPool(
                        applyExplosionCondition(
                                block,
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1.0F))
                                        .add(
                                                LootItem.lootTableItem(block)
                                                        .apply(
                                                                CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY)
                                                                        .include(DataComponents.CUSTOM_NAME)
                                                                        .include(DataComponents.CONTAINER)
                                                                        .include(DataComponents.LOCK)
                                                                        .include(DataComponents.CONTAINER_LOOT)
                                                                        .include(RSDataComponents.SINGLE_TANK_CONTENTS.get())
                                                        )
                                        )
                        )
                );
    }
    
    public LootTable.Builder createAlloyerDrops(Block block) {
        
        return LootTable.lootTable()
                .withPool(
                        applyExplosionCondition(
                                block,
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1.0F))
                                        .add(
                                                LootItem.lootTableItem(block)
                                                        .apply(
                                                                CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY)
                                                                        .include(RSDataComponents.TRIPLE_TANK_CONTENTS.get())
                                                        )
                                        )
                        )
                );
    }
    
    public LootTable.Builder createRuneAnvilDrops(Block block) {
        
        return LootTable.lootTable()
                .withPool(
                        applyExplosionCondition(
                                block,
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1.0F))
                                        .add(
                                                LootItem.lootTableItem(block)
                                                        .apply(
                                                                CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY)
                                                                        .include(DataComponents.CUSTOM_NAME)
                                                                        .include(DataComponents.CONTAINER)
                                                                        .include(DataComponents.LOCK)
                                                                        .include(DataComponents.CONTAINER_LOOT)
                                                        )
                                        )
                        )
                );
    }
}
