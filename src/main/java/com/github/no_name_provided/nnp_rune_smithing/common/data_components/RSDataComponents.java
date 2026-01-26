package com.github.no_name_provided.nnp_rune_smithing.common.data_components;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

public class RSDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID);
    
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RuneData>> RUNE_DATA =
            DATA_COMPONENTS.registerComponentType(
                    "rune_data",
                    builder -> builder
                            .persistent(RuneData.CODEC)
                            .networkSynchronized(RuneData.STREAM_CODEC)
            );
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RunesAdded>> RUNES_ADDED =
            DATA_COMPONENTS.registerComponentType(
                    "runes_added",
                    builder -> builder
                            .persistent(RunesAdded.CODEC)
                            .networkSynchronized(RunesAdded.STREAM_CODEC)
            );
    
    // Saving BlockEntity data to ItemStacks
    
    // We need this because the NeoForge team decided ItemStackHandlers didn't need codecs, or to be immutable, or to expose their internal list (as a copy or otherwise)
    // #BlameTheNeoForgeTeam
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ItemStack>>> ITEMSTACK_HANDLER_INVENTORY =
            DATA_COMPONENTS.registerComponentType(
                    "itemstack_handler_inventory",
                    builder -> builder
                            .persistent(ItemStack.OPTIONAL_CODEC.listOf())
            );
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SingleTankContents>> SINGLE_TANK_CONTENTS =
            DATA_COMPONENTS.registerComponentType(
                    "single_tank_contents",
                    builder -> builder
                            .persistent(SingleTankContents.CODEC)
            );
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<TripleTankContents>> TRIPLE_TANK_CONTENTS =
            DATA_COMPONENTS.registerComponentType(
                    "triple_tank_contents",
                    builder -> builder
                            .persistent(TripleTankContents.CODEC)
            );
    
    public static void register(IEventBus bus) {
        DATA_COMPONENTS.register(bus);
    }
}
