package com.github.no_name_provided.nnp_rune_smithing;

import com.github.no_name_provided.nnp_rune_smithing.client.particles.RSParticleTypes;
import com.github.no_name_provided.nnp_rune_smithing.common.blocks.RSBlocks;
import com.github.no_name_provided.nnp_rune_smithing.common.curios.CuriosHelper;
import com.github.no_name_provided.nnp_rune_smithing.common.data_components.RSDataComponents;
import com.github.no_name_provided.nnp_rune_smithing.common.entities.RSEntities;
import com.github.no_name_provided.nnp_rune_smithing.common.fluids.FluidHelper;
import com.github.no_name_provided.nnp_rune_smithing.common.fluids.RSFluidTags;
import com.github.no_name_provided.nnp_rune_smithing.common.fluids.RunicMetals;
import com.github.no_name_provided.nnp_rune_smithing.common.gui.menus.RSMenus;
import com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems;
import com.github.no_name_provided.nnp_rune_smithing.common.recipes.RSRecipes;
import com.github.no_name_provided.nnp_rune_smithing.datagen.providers.numbers.RSNumbers;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems.*;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(NNPRuneSmithing.MODID)
public class NNPRuneSmithing {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "nnp_rune_smithing";
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "nnp_rune_smithing" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    
    // Creates a creative tab with the id "nnp_rune_smithing:example_tab" for the example item, that is placed after the combat tab
    @SuppressWarnings("unused")
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register(
            "default_category.nnp_rune_smithing",
            () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.nnp_rune_smithing"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(RSItems.MELTER.get()::getDefaultInstance)
                    .displayItems((parameters,
                                   output) -> {
                                ITEMS.getEntries().forEach((entry) ->
                                        output.accept(entry.get())
                                );
                                WOODEN_CHARMS.getEntries().forEach((entry) ->
                                        output.accept(entry.get())
                                );
                                NUGGETS.getEntries().forEach((entry) ->
                                        output.accept(entry.get())
                                );
                                INGOTS.getEntries().forEach((entry) ->
                                        output.accept(entry.get())
                                );
                                METAL_STORAGE_BLOCKS.getEntries().forEach((entry) ->
                                        output.accept(entry.get())
                                );
                                RUNES.getEntries().forEach((entry) ->
                                        output.accept(entry.get())
                                );
                            }
                    ).build());
    
    /**
     * Mod entry point.
     */
    public NNPRuneSmithing(IEventBus modEventBus, ModContainer modContainer) {
        
        FluidHelper.register(modEventBus);
        RSItems.register(modEventBus);
        RSBlocks.register(modEventBus);
        RSMenus.register(modEventBus);
        RSEntities.register(modEventBus);
        RSRecipes.register(modEventBus);
        RSDataComponents.register(modEventBus);
        RSNumbers.register(modEventBus);
        RSParticleTypes.register(modEventBus);
        
        // This check doesn't seem to matter? Not sure how registering a listener for something
        // that doesn't exist isn't causing a crash...
        if (ModList.get().isLoaded("curios")) {
            new CuriosHelper(modEventBus);
        }
        
        new RunicMetals();
        RSFluidTags.createFluidTagKeys();
        
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);
        
        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
//        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
    }
}
