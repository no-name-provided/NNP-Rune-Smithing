package com.github.no_name_provided.nnp_rune_smithing.datagen.providers.numbers;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

public class RSNumbers {
    public static final DeferredRegister<LootNumberProviderType> LOOT_NUMBER_PROVIDER_TYPES =
            DeferredRegister.create(Registries.LOOT_NUMBER_PROVIDER_TYPE, MODID);
    public static final Supplier<LootNumberProviderType> GIVE_GUIDE =
            LOOT_NUMBER_PROVIDER_TYPES.register("give_guide", () -> new LootNumberProviderType(GiveGuideSubProvider.CODEC));
    public static final Supplier<LootNumberProviderType> ONE_IN_N =
            LOOT_NUMBER_PROVIDER_TYPES.register("one_in_n", () -> new LootNumberProviderType(OneInNSubProvider.CODEC));

    public static void register(IEventBus bus) {
        LOOT_NUMBER_PROVIDER_TYPES.register(bus);
    }
}
