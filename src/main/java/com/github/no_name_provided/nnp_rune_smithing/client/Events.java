package com.github.no_name_provided.nnp_rune_smithing.client;

import com.github.no_name_provided.nnp_rune_smithing.client.gui.MelterScreen;
import com.github.no_name_provided.nnp_rune_smithing.client.renderers.CastingTableEntityRenderer;
import com.github.no_name_provided.nnp_rune_smithing.client.renderers.MelterBlockRenderer;
import com.github.no_name_provided.nnp_rune_smithing.client.renderers.RuneBlockEntityRenderer;
import com.github.no_name_provided.nnp_rune_smithing.common.blocks.RSBlocks;
import com.github.no_name_provided.nnp_rune_smithing.common.blocks.TintedBlock;
import com.github.no_name_provided.nnp_rune_smithing.common.entities.RSEntities;
import com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems;
import com.github.no_name_provided.nnp_rune_smithing.common.items.TintedBlockItem;
import com.github.no_name_provided.nnp_rune_smithing.common.items.TintedItem;
import com.github.no_name_provided.nnp_rune_smithing.common.items.runes.AbstractRuneItem;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

import static com.github.no_name_provided.nnp_rune_smithing.common.fluids.FluidHelper.FLUID_SETS;
import static com.github.no_name_provided.nnp_rune_smithing.common.fluids.FluidHelper.tempToColor;
import static com.github.no_name_provided.nnp_rune_smithing.common.gui.menus.RSMenus.MELTER_MENU;

@EventBusSubscriber
public class Events {
    @SubscribeEvent
    static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        FLUID_SETS.forEach((set) -> {
                    event.registerFluidType(
                            new MoltenFluidTypeClientExtensions(
                                    null != tempToColor.floorKey(set.temperature()) ?
                                            tempToColor.floorEntry(set.temperature()).getValue() :
                                            tempToColor.ceilingEntry(set.temperature()).getValue()
                            ),
                            set.type()
                    );
                }
        );
    }
    @SubscribeEvent
    static void onRegisterItemColorHandlers(RegisterColorHandlersEvent.Item event) {
        FLUID_SETS.forEach(set ->
                event.register(
                        (stack, index) -> FastColor.ARGB32.color(252, index == 1 ?
                                (null != tempToColor.floorKey(set.temperature()) ?
                                        tempToColor.floorEntry(set.temperature()).getValue() :
                                        tempToColor.ceilingEntry(set.temperature()).getValue()) :
                                event.getItemColors().getColor(Items.BUCKET.getDefaultInstance(), 0)),
                        set.bucket().get()
                )
        );
        RSItems.NUGGETS.getEntries().forEach(nugget -> {
            // For IDE. Should never be false (bar programmer error)
            if (nugget.get() instanceof TintedItem item) {
                event.register((stack, index) -> item.COLOR, nugget.get());
            }
        });
        RSItems.INGOTS.getEntries().forEach(ingot -> {
            // For IDE. Should never be false (bar programmer error)
            if (ingot.get() instanceof TintedItem item) {
                event.register((stack, index) -> item.COLOR, ingot.get());
            }
        });
        RSItems.METAL_STORAGE_BLOCKS.getEntries().forEach(block -> {
            // For IDE. Should never be false (bar programmer error)
            if (block.get() instanceof TintedBlockItem item) {
                event.register((stack, index) -> item.COLOR, block.get());
            }
        });
        event.register(
                (stack, index) -> AbstractRuneItem.getMaterialColor(stack),
                RSItems.RUNES.getEntries().stream().map(e->(Item)(e.get())).toArray(ItemLike[]::new)
        );
    }
    @SubscribeEvent
    static void onRegisterBlockColorHandlers(RegisterColorHandlersEvent.Block event) {
        RSBlocks.METAL_STORAGE_BLOCKS.getEntries().forEach(block -> {
            // For IDE. Should never be false (bar programmer error)
            if (block.get() instanceof TintedBlock storageBlock) {
                event.register((state, getter, pos, index) ->
                        storageBlock.COLOR, block.get());
            }
        });
    }
    
    /**
     * Used in place of a deferred register, since there appears to be no proper registry.
     */
    @SubscribeEvent
    private static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(MELTER_MENU.get(), MelterScreen::new);
    }
    /**
     * Used in place of a deferred register, since there's no proper registry.
     */
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                RSEntities.RUNE_BLOCK_ENTITY.get(),
                // A function of BlockEntityRendererProvider.Context to BlockEntityRenderer.
                RuneBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                RSEntities.CASTING_TABLE_BLOCK_ENTITY.get(),
                CastingTableEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                RSEntities.MELTER_BLOCK_ENTITY.get(),
                MelterBlockRenderer::new
        );
    }
}
