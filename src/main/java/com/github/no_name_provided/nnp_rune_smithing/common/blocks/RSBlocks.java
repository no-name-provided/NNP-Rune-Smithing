package com.github.no_name_provided.nnp_rune_smithing.common.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

public class RSBlocks {
    public static DeferredRegister<Block> FLUID_BLOCKS = DeferredRegister.createBlocks(MODID);
    public static DeferredRegister<Block> BLOCKS = DeferredRegister.createBlocks(MODID);
    public static DeferredRegister<Block> METAL_STORAGE_BLOCKS = DeferredRegister.createBlocks(MODID);
    public static DeferredRegister<Block> ORE_BLOCKS = DeferredRegister.createBlocks(MODID);
    
    public static final DeferredHolder<Block, Block> WHITTLING_TABLE = BLOCKS.register(
            "whittling_table",
            () -> new WhittlingTableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE))
    );
    public static final DeferredHolder<Block, MelterBlock> MELTER = BLOCKS.register(
            "melter",
            () -> new MelterBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.FURNACE))
    );
    public static final DeferredHolder<Block, AlloyerBlock> ALLOYER = BLOCKS.register(
            "alloyer",
            () -> new AlloyerBlock( BlockBehaviour.Properties.of())
    );
    public static final DeferredHolder<Block, CastingTableBlock> CASTING_TABLE = BLOCKS.register(
            "casting_table",
            () -> new CastingTableBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(3.5F))
    );
    public static final DeferredHolder<Block, RuneAnvilBlock> RUNE_ANVIL = BLOCKS.register(
            "rune_anvil",
            () -> new RuneAnvilBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL))
    );
    public static final DeferredHolder<Block, RuneBlock> RUNE_BLOCK = BLOCKS.register(
            "runes",
            () -> new RuneBlock(BlockBehaviour.Properties.of())
    );

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        METAL_STORAGE_BLOCKS.register(bus);
        ORE_BLOCKS.register(bus);
    }
}
