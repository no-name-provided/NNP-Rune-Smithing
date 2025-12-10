package com.github.no_name_provided.nnp_rune_smithing.datagen.providers.advancements;

import com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing;
import com.github.no_name_provided.nnp_rune_smithing.common.blocks.RSBlocks;
import com.github.no_name_provided.nnp_rune_smithing.common.data_components.RSDataComponents;
import com.github.no_name_provided.nnp_rune_smithing.common.data_components.RunesAdded;
import com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.extensions.IAdvancementBuilderExtension;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Consumer;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;
import static com.github.no_name_provided.nnp_rune_smithing.datagen.providers.subproviders.GenericLootTables.STARTER_PACKAGE_CARVED_RUNES;
import static com.github.no_name_provided.nnp_rune_smithing.datagen.providers.subproviders.GenericLootTables.STARTER_PACKAGE_CAST_RUNES;

public class RSGuideBookAdvancements implements AdvancementProvider.AdvancementGenerator {

    /**
     * A method used to generate advancements for a mod. Advancements should be built via
     * {@link IAdvancementBuilderExtension#save(Consumer, ResourceLocation, ExistingFileHelper)}.
     *
     * @param registries         a lookup for registries and their objects
     * @param saver              a consumer used to write advancements to a file
     * @param existingFileHelper a helper used to find whether a file exists
     */
    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper) {
        AdvancementHolder root = Advancement.Builder.advancement()
                .requirements(AdvancementRequirements.Strategy.AND)
                .addCriterion("placeholder", PlayerTrigger.TriggerInstance.tick())
                .save(saver, ResourceLocation.fromNamespaceAndPath(MODID, "guide_book/root"), existingFileHelper);
        
        RSItems.RUNES.getEntries().forEach(rune -> {
            String name = rune.getId().getPath().split("_")[0];
            addPlaceInWorldCriterion(Advancement.Builder.advancement(), rune)
                    .requirements(AdvancementRequirements.Strategy.AND)
                    .parent(root)
                    .save(saver, ResourceLocation.fromNamespaceAndPath(MODID, "guide_book/" + rune.getId().getPath() + "_placed"), existingFileHelper);
            
            addPlaceInInventoryCriterion(addPlaceInInventoryCriterion(Advancement.Builder.advancement(), rune), DeferredHolder.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(NNPRuneSmithing.MODID, name + "_template")))
                    .requirements(AdvancementRequirements.Strategy.OR)
                    .parent(root)
                    .save(saver, ResourceLocation.fromNamespaceAndPath(MODID, "guide_book/" + rune.getId().getPath() + "_in_inventory"), existingFileHelper);
        });
        
        // One offs
        
        Advancement.Builder.advancement().parent(root)
                .addCriterion("made_whittling_table", RecipeCraftedTrigger.TriggerInstance.craftedItem(ResourceLocation.fromNamespaceAndPath(MODID, "craft_whittling_table")))
                .requirements(AdvancementRequirements.Strategy.OR)
                .rewards(AdvancementRewards.Builder.loot(STARTER_PACKAGE_CARVED_RUNES))
                .save(saver, ResourceLocation.fromNamespaceAndPath(MODID, "guide_book/starter_templates_carved"), existingFileHelper);
        Advancement.Builder.advancement().parent(root)
                .addCriterion("made_casting_table", RecipeCraftedTrigger.TriggerInstance.craftedItem(ResourceLocation.fromNamespaceAndPath(MODID, "craft_casting_table")))
                .requirements(AdvancementRequirements.Strategy.OR)
                .rewards(AdvancementRewards.Builder.loot(STARTER_PACKAGE_CAST_RUNES))
                .save(saver, ResourceLocation.fromNamespaceAndPath(MODID, "guide_book/starter_templates_cast"), existingFileHelper);
    }
    
    /**
     * Creates criterion that is satisfied when the given rune is used to create a (or added to an existing) RuneBlock.
     */
    Advancement.Builder addPlaceInWorldCriterion(Advancement.Builder builder, DeferredHolder<Item, ? extends Item> blockItem) {
        return builder.addCriterion("placed_" + blockItem.getId().getPath() + "_in_world", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(
                LootItemBlockStatePropertyCondition.hasBlockStateProperties(RSBlocks.RUNE_BLOCK.get()),
                MatchTool.toolMatches(ItemPredicate.Builder.item().of(blockItem.get()))
        ));
    }
    /**
     * Creates criterion that is satisfied when the given rune is used to create a (or added to an existing) RuneBlock.
     */
    Advancement.Builder addPlaceInInventoryCriterion(Advancement.Builder builder, DeferredHolder<Item, ? extends Item> rune) {
        return builder.addCriterion("placed_" + rune.getId().getPath() + "_in_inventory", InventoryChangeTrigger.TriggerInstance.hasItems(
            rune.get()
        ));
    }
    
    /**
     * Not implemented. May need to register my own predicate, since I only need a partial match.
     */
    Advancement.Builder addInlayItemCriterion(Advancement.Builder builder, DeferredHolder<Item, ? extends Item> blockItem) {
        return builder.addCriterion("placed_" + blockItem.getId().getPath() + "_in_world", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item()
                .hasComponents(DataComponentPredicate.builder().expect(RSDataComponents.RUNES_ADDED.get(), RunesAdded.DEFAULT.get()).build())
        ));
    }
}
