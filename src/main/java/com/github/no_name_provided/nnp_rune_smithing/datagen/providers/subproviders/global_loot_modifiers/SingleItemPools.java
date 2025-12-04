package com.github.no_name_provided.nnp_rune_smithing.datagen.providers.subproviders.global_loot_modifiers;

import com.github.no_name_provided.nnp_rune_smithing.common.items.CastingTemplate;
import com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.BiConsumer;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

public class SingleItemPools implements LootTableSubProvider {
    
    // Required by loot table provider
    public SingleItemPools(HolderLookup.Provider ignoredProvider) {
    }
    
    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer) {
        for (DeferredHolder<Item, ? extends Item> holder : RSItems.ITEMS.getEntries()) {
            if (holder.get() instanceof CastingTemplate) {
                consumer.accept(
                        getSingleItemLootPoolKey(holder),
                        new LootTable.Builder().withPool(
                                new LootPool.Builder().setRolls(ConstantValue.exactly(1))
                                        .add(LootItem.lootTableItem(holder.get()))
                        )
                );
            }
        }
        RSItems.WOODEN_CHARMS.getEntries().forEach(holder -> {
            if (holder.get() instanceof CastingTemplate) {
                consumer.accept(
                        getSingleItemLootPoolKey(holder),
                        new LootTable.Builder().withPool(
                                new LootPool.Builder().setRolls(ConstantValue.exactly(1))
                                        .add(LootItem.lootTableItem(holder.get()))
                        )
                );
            }
        });
        
    }
    
    
    public static ResourceKey<LootTable> getSingleItemLootPoolKey(DeferredHolder<Item, ? extends Item> item) {
        return ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(MODID, item.getId().getPath()));
    }
}
