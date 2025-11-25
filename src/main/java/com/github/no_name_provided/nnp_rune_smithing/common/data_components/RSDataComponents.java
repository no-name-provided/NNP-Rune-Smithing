package com.github.no_name_provided.nnp_rune_smithing.common.data_components;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

public class RSDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID);
    
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RuneData>> RUNE_DATA = DATA_COMPONENTS.registerComponentType(
            "rune_data",
            builder -> builder
                    .persistent(RuneData.CODEC)
                    .networkSynchronized(RuneData.STREAM_CODEC)
    );
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RunesAdded>> RUNES_ADDED = DATA_COMPONENTS.registerComponentType(
            "runes_added",
            builder -> builder
                    .persistent(RunesAdded.CODEC)
                    .networkSynchronized(RunesAdded.STREAM_CODEC)
    );
    
    public static void register(IEventBus bus) {
        DATA_COMPONENTS.register(bus);
    }
}
