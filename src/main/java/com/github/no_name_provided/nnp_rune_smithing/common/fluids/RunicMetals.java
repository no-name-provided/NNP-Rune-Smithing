package com.github.no_name_provided.nnp_rune_smithing.common.fluids;

import com.github.no_name_provided.nnp_rune_smithing.common.blocks.RSBlocks;
import com.github.no_name_provided.nnp_rune_smithing.common.blocks.TintedBlock;
import com.github.no_name_provided.nnp_rune_smithing.common.blocks.TintedDropExperienceBlock;
import com.github.no_name_provided.nnp_rune_smithing.common.items.LayeredTintedBlockItem;
import com.github.no_name_provided.nnp_rune_smithing.common.items.TintedBlockItem;
import com.github.no_name_provided.nnp_rune_smithing.common.items.TintedItem;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.List;
import java.util.function.Supplier;

import static com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems.*;

/**
 * Centralized place to register molten metals. Would be nice to replace list with json registry.
 */
public class RunicMetals {
    public RunicMetals() {
        
        register("aluminum", 660, 0xDADCDB);
        register("tin", 232, 0xAEAFAB);
        register("bronze", 913, 0xA6652F, 2);
        register("steel", 1510, 0x1C1D21, 2);
        register("titanium", 1670, 0x9C9A91, 3);
        register("phosphorus", 44, 0x656C74);
        register("sodium", 98, 0xF9F9F7);
        register("solder", 215, 0xA29890);
        register("bismuth", 272, 0xB4ABA0, 2);
        register("lead", 328, 0x5E5A87);
        register("zinc", 420, 0xCBCDC4);
        register("silver", 890, 0xD7D7D7, 2);
        register("electrum", 961, 0x8C7D44, 3);
        
        FluidHelper.registerMoltenMetal("copper", 1085, 1, 0xC87456);
        FluidHelper.registerMoltenMetal("iron", 1538, 1, 0xE0E0E0);
        FluidHelper.registerMoltenMetal("gold", 1063, 2, 0xFFEC4F);
    }
    
    synchronized public static void register(String name, int meltingPoint, int color) {
        register(name, meltingPoint, color, 1);
    }
    
    synchronized public static void register(String name, int meltingPoint, int color, int tier) {
        NUGGETS.register(name + "_nugget", () -> new TintedItem(new Item.Properties(), color));
        INGOTS.register(name + "_ingot", () -> new TintedItem(new Item.Properties(), color));
        Supplier<Block> block = RSBlocks.METAL_STORAGE_BLOCKS.register(
                name + "_block",
                // TODO: find out how to make custom MapColors. Class currently uses a hardcoded list which errors if the index is too large
                () -> new TintedBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK), color)
        );
        METAL_STORAGE_BLOCKS.register(name + "_block", () -> new TintedBlockItem(block.get(), new Item.Properties(), color));
        
        RAW_ORES.register("raw_" + name, () ->
                new TintedItem(new Item.Properties(), color)
        );
        Supplier<Block> ore_block = RSBlocks.ORE_BLOCKS.register(name + "_ore", () ->
                new TintedDropExperienceBlock(
                        ConstantInt.of(0),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE),
                        color
                )
        );
        ORE_BLOCKS.register(name + "_ore", () ->
                new LayeredTintedBlockItem(
                        ore_block.get(),
                        new Item.Properties(),
                        // Use fully transparent tint for base layer, just in case I can't figure out how
                        // to datagen a tint index of -1
                        List.of(0x00ffffff, color)
                )
        );
        Supplier<Block> deepslate_ore_block = RSBlocks.ORE_BLOCKS.register("deepslate_" + name + "_ore", () ->
                new TintedDropExperienceBlock(
                        ConstantInt.of(0),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE),
                        color
                )
        );
        ORE_BLOCKS.register("deepslate_" + name + "_ore", () ->
                new LayeredTintedBlockItem(
                        deepslate_ore_block.get(),
                        new Item.Properties(),
                        // Use fully transparent tint for base layer, just in case I can't figure out how
                        // to datagen a tint index of -1
                        List.of(0x00ffffff, color)
                )
        );
        Supplier<Block> netherrack_ore_block = RSBlocks.ORE_BLOCKS.register("netherrack_" + name + "_ore", () ->
                new TintedDropExperienceBlock(
                        ConstantInt.of(0),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE),
                        color
                )
        );
        ORE_BLOCKS.register("netherrack_" + name + "_ore", () ->
                new LayeredTintedBlockItem(
                        netherrack_ore_block.get(),
                        new Item.Properties(),
                        // Use fully transparent tint for base layer, just in case I can't figure out how
                        // to datagen a tint index of -1
                        List.of(0x00ffffff, color)
                )
        );
        Supplier<Block> endstone_ore_block = RSBlocks.ORE_BLOCKS.register("endstone_" + name + "_ore", () ->
                new TintedDropExperienceBlock(
                        ConstantInt.of(0),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE),
                        color
                )
        );
        ORE_BLOCKS.register("endstone_" + name + "_ore", () ->
                new LayeredTintedBlockItem(
                        endstone_ore_block.get(),
                        new Item.Properties(),
                        // Use fully transparent tint for base layer, just in case I can't figure out how
                        // to datagen a tint index of -1
                        List.of(0x00ffffff, color)
                )
        );
        
        FluidHelper.registerMoltenMetal(name, meltingPoint, tier, color);
    }
}
