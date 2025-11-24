package com.github.no_name_provided.nnp_rune_smithing.common.entities;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;
import static com.github.no_name_provided.nnp_rune_smithing.common.blocks.RSBlocks.*;

public class RSEntities {
        public static DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
                DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MODID);

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<MelterBlockEntity>> MELTER_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "melter_block_entity",
            // The block entity type, created using a builder.
            () -> BlockEntityType.Builder.of(
                            // The supplier to use for constructing the block entity instances.
                            MelterBlockEntity::new,
                            // A vararg of blocks that can have this block entity.
                            // This assumes the existence of the referenced blocks as DeferredBlock<Block>s.
                            MELTER.get() //, MyBlocks.MY_BLOCK_2.get() //optional, to register more than one
                    )
                    // Build using null; vanilla does some dataFixer shenanigans with the parameter that we don't need.
                    .build(null) //Documentation says passing null is correct
    );
    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<CastingTableBlockEntity>> CASTING_TABLE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "casting_table_block_entity",
            // The block entity type, created using a builder.
            () -> BlockEntityType.Builder.of(
                            // The supplier to use for constructing the block entity instances.
                            CastingTableBlockEntity::new,
                            // A vararg of blocks that can have this block entity.
                            // This assumes the existence of the referenced blocks as DeferredBlock<Block>s.
                            CASTING_TABLE.get() //, MyBlocks.MY_BLOCK_2.get() //optional, to register more than one
                    )
                    // Build using null; vanilla does some dataFixer shenanigans with the parameter that we don't need.
                    .build(null) //Documentation says passing null is correct
    );
    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<RuneBlockEntity>> RUNE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "rune_block_entity",
            // The block entity type, created using a builder.
            () -> BlockEntityType.Builder.of(
                            // The supplier to use for constructing the block entity instances.
                            RuneBlockEntity::new,
                            // A vararg of blocks that can have this block entity.
                            // This assumes the existence of the referenced blocks as DeferredBlock<Block>s.
                            RUNE_BLOCK.get() //, MyBlocks.MY_BLOCK_2.get() //optional, to register more than one
                    )
                    // Build using null; vanilla does some dataFixer shenanigans with the parameter that we don't need.
                    .build(null) //Documentation says passing null is correct
    );

    public static void register(IEventBus bus) {
        BLOCK_ENTITY_TYPES.register(bus);
    }
}
