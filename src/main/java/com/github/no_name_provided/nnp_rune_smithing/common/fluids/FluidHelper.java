package com.github.no_name_provided.nnp_rune_smithing.common.fluids;

import com.github.no_name_provided.nnp_rune_smithing.common.blocks.TintedBlock;
import com.github.no_name_provided.nnp_rune_smithing.common.datamaps.CastableFluidData;
import com.google.common.collect.ImmutableSortedMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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

/**
 * Contains shared methods, long methods, and constants used to create and interact with fluids.
 */
public class FluidHelper {
    /**
     * Immutable, sorted map of temperature to RGB values for blackbody fluids at given temperatures. Contains some
     * useful helper methods.
     */
    public static final NavigableMap<Integer, Integer> tempToColor =
            new ImmutableSortedMap.Builder<Integer, Integer>(Comparator.comparingInt(a -> a)).putAll(new Int2IntAVLTreeMap(
                    new int[]{
                            // Temperature in degrees C, as is the Minecraft convention
                            199, 241, 249, 260, 271, 282, 302, 427, 538, 593, 649, 704, 760, 816, 871, 927, 982, 1038, 1093
                    },
                    new int[]{
                            // Only contains RGB values. Alpha defaults to 0, so make sure to set it where you use it
                            0xF6F7AF, 0xB9875A, 0x7F4141, 0x7C0404, 0x800080, 0x660066, 0x110080, 0x4F4F4F, 0x74605E,
                            0x9B5346, 0xB2331C, 0xBD3324, 0xCF2018, 0xFC1312, 0xF16420,
                            0xFC8412, 0xF1A74E, 0xDEB912, 0xDFE60F
                    }
            )).buildOrThrow();
    
    // Only add to this map; unused - use DataMap instead
    public static Map<Fluid, Pair<Integer, Integer>> ExtraCastableFluids;
    
    public static final HashMap<String, List<ResourceLocation>> HIDDEN_IN_JEI_IF_MOD_NOT_PRESENT = new HashMap<>();
    
    /**
     * Convenience method for making runes from fluids added by other mods or vanilla
     */
    @SuppressWarnings("unused")
    public static synchronized void registerExtraCastableFluids(Fluid fluid, Integer tier, Integer color) {
        ExtraCastableFluids.put(fluid, Pair.of(tier, color));
    }
    
    // Only add to this list
    /**
     * This is a de facto registry. While I originally intended to turn this into a datapack registry, I've determined
     * that it's prohibitively difficult to load those before vanilla registries are frozen. In the interest of mod
     * compatibility and modpack support, I've slowly transitioned this to a helper, primarily used to organize
     * information for datagen.
     * <p>
     * Much of its functionality has been supplanted by {@link CastableFluidData}, which does support data packs.
     * </p>
     */
    public static List<FluidSet> FLUID_SETS = Collections.synchronizedList(new ArrayList<>());
    
    /**
     * Convenience method for registering molten metals.
     *
     * @param name Salt for registry string. Should match name of corresponding metal.
     * @param temp Melting point of fluid. Used for a variety of things, including color.
     */
    @SuppressWarnings("unused")
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
                        BlockBehaviour.Properties
                                .ofFullCopy(Blocks.LAVA)
                                .mapColor(TintedBlock.getClosestMapColor(
                                                null != tempToColor.floorKey(temp) ?
                                                        tempToColor.floorEntry(temp).getValue() :
                                                        tempToColor.ceilingEntry(temp).getValue()
                                        )
                                )
                )
        );
        SolidEquivalents EQUIVALENTS = new SolidEquivalents(
                Optional.of(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "nuggets/" + name))),
                Optional.of(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "ingots/" + name))),
                Optional.of(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/" + name))),
                Optional.of(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/" + name)))
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
    
    public static synchronized void registerMoltenMetalForModCompat(String modid, String name, Integer temp, Integer tier, Integer colorWhenCool) {
        HIDDEN_IN_JEI_IF_MOD_NOT_PRESENT.computeIfAbsent(modid, id -> new ArrayList<>())
                // Must be manually updated if we change our naming scheme in registerMoltenMetal
                .add(ResourceLocation.fromNamespaceAndPath(MODID, "molten_" + name));
        registerMoltenMetal(name, temp, tier, colorWhenCool);
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
            Optional<TagKey<Item>> nuggets,
            Optional<TagKey<Item>> ingots,
            Optional<TagKey<Item>> blockItems,
            Optional<TagKey<Block>> blocks
    ) {
        public static final Codec<SolidEquivalents> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        TagKey.codec(Registries.ITEM).optionalFieldOf("nuggets").forGetter(SolidEquivalents::nuggets),
                        TagKey.codec(Registries.ITEM).optionalFieldOf("ingots").forGetter(SolidEquivalents::ingots),
                        TagKey.codec(Registries.ITEM).optionalFieldOf("block_items").forGetter(SolidEquivalents::blockItems),
                        TagKey.codec(Registries.BLOCK).optionalFieldOf("blocks").forGetter(SolidEquivalents::blocks)
                ).apply(inst, SolidEquivalents::new)
        );
    }
    
    public static String makeQuantityTooltip(int amount) {
        
        return amount + (amount == 1 ? " millibucket" : " millibuckets");
    }
    
    public static void register(IEventBus modBus) {
        FLUID_TYPES.register(modBus);
        FLUIDS.register(modBus);
        FLUID_BLOCKS.register(modBus);
    }
    
    // This is only run during "compilation", so I want it to throw an error if a fluid isn't found
    
    /**
     * Will crash if fluid isn't found. Only checks our deferred registry, so name spaces aren't an issue.
     *
     * @param path The path portion of the registry id.
     * @return The registered fluid.
     */
    public static Fluid unsafeGetFluidFromRegistryPath(String path) throws NoSuchElementException {
        
        try {
            return RSFluids.FLUIDS.getEntries().stream().filter(holder -> holder.getId().getPath().equals(path)).findFirst().orElseThrow().get();
        } catch (NoSuchElementException ignoredE) {
            throw new NoSuchElementException("The automatic error fails to actually log the missing element (ridiculous, I know), so we're rethrowing it with this message - \nMissing Entry: " + path);
        }
    }
}
