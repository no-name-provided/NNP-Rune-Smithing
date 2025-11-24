package com.github.no_name_provided.nnp_rune_smithing.common.fluids;

import com.google.common.collect.ImmutableSortedMap;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntAVLTreeMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.*;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;
import static com.github.no_name_provided.nnp_rune_smithing.common.blocks.RSBlocks.FLUID_BLOCKS;
import static com.github.no_name_provided.nnp_rune_smithing.common.fluids.RSFluids.FLUIDS;
import static com.github.no_name_provided.nnp_rune_smithing.common.fluids.RSFluids.FLUID_TYPES;
import static com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems.ITEMS;

public class FluidHelper {
    
    public static NavigableMap<Integer, Integer> tempToColor =
            new ImmutableSortedMap.Builder<Integer, Integer>(Comparator.comparingInt(a -> a)).putAll(new Int2IntAVLTreeMap(
                    new int[]{
                            199, 241, 249, 260, 271, 282, 302, 427, 538, 593, 649, 704, 760, 816, 871, 927, 982, 1038, 1093
                    },
                    new int[]{
                            0xF6F7AF, 0xB9875A, 0x7F4141, 0x7C0404, 0x800080, 0x660066, 0x110080, 0x4F4F4F, 0x74605E,
                            0x9B5346, 0xB2331C, 0xBD3324, 0xCF2018, 0xFC1312, 0xF16420,
                            0xFC8412, 0xF1A74E, 0xDEB912, 0xDFE60F
                    }
            )).buildOrThrow();
    
    // Only add to this map
    public static Map<Fluid, Pair<Integer, Integer>> ExtraCastableFluids;
    
    /**
     * Convenience method for making runes from fluids added by other mods or vanilla
     */
    public static synchronized void registerExtraCastableFluids(Fluid fluid, Integer tier, Integer color) {
        ExtraCastableFluids.put(fluid, Pair.of(tier, color));
    }
    
    // Only add to this list
    public static List<FluidSet> FLUID_SETS = Collections.synchronizedList(new ArrayList<>());
    
    /**
     * Convenience method for registering molten metals.
     *
     * @param name Salt for registry string. Should match name of corresponding metal.
     * @param temp Melting point of fluid. Used for a variety of things, including color.
     */
    public static synchronized void registerMoltenMetal(String name, Integer temp) {
        registerMoltenMetal(name, temp, 1, 0);
    }
    
    /**
     * Convenience method for registering molten metals.
     *
     * @param name Salt for registry string. Should match name of corresponding metal.
     * @param temp Melting point of fluid. Used for a variety of things, including color.
     * @param tier Used by runes to determine their quality.
     */
    public static synchronized void registerMoltenMetal(String name, Integer temp, Integer tier, Integer colorWhenCool) {
        DeferredHolder<FluidType, MoltenMetalFluidType> TYPE = FLUID_TYPES.register(
                name,
                () -> new MoltenMetalFluidType(temp, tier, colorWhenCool)
        );
        DeferredHolder<Fluid, MoltenMetalFluid.Flowing> FLOWING = FLUIDS.register(
                "flowing_molten_" + name,
                () -> new MoltenMetalFluid.Flowing(TYPE)
        );
        DeferredHolder<Fluid, MoltenMetalFluid.Source> SOURCE = FLUIDS.register(
                "molten_" + name,
                () -> new MoltenMetalFluid.Source(TYPE)
        );
        DeferredHolder<Item, BucketItem> BUCKET = ITEMS.register(
                "molten_" + name + "_bucket",
                () -> new BucketItem(
                        SOURCE.get(),
                        new Item.Properties()
                                .stacksTo(1)
                                .fireResistant()
                                .craftRemainder(Items.BUCKET)
                )
        );
        DeferredHolder<Block, LiquidBlock> BLOCK = FLUID_BLOCKS.register(
                "molten_" + name,
                () -> new LiquidBlock(
                        SOURCE.get(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA)
                )
        );
        SolidEquivalents EQUIVALENTS = new SolidEquivalents(
                TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "nuggets/" + name)),
                TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "ingots/" + name)),
                TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/" + name)),
                TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/" + name))
        );
        
        FLUID_SETS.add(new FluidSet(
                        TYPE,
                        FLOWING,
                        SOURCE,
                        BUCKET,
                        BLOCK,
                        temp,
                        EQUIVALENTS
                )
        );
    }
    
    public record FluidSet(
            DeferredHolder<FluidType, MoltenMetalFluidType> type,
            DeferredHolder<Fluid, MoltenMetalFluid.Flowing> flowing,
            DeferredHolder<Fluid, MoltenMetalFluid.Source> source,
            DeferredHolder<Item, BucketItem> bucket,
            DeferredHolder<Block, LiquidBlock> block,
            Integer temperature,
            SolidEquivalents equivalents
    ) {
    }
    
    public record SolidEquivalents(
            TagKey<Item> nuggets,
            TagKey<Item> ingots,
            TagKey<Item> blockItems,
            TagKey<Block> blocks
    ) {
    }
    
    public static void register(IEventBus modBus) {
        FLUID_TYPES.register(modBus);
        FLUIDS.register(modBus);
        FLUID_BLOCKS.register(modBus);
    }
}
