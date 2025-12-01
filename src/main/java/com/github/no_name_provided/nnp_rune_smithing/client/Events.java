package com.github.no_name_provided.nnp_rune_smithing.client;

import com.github.no_name_provided.nnp_rune_smithing.client.gui.MelterScreen;
import com.github.no_name_provided.nnp_rune_smithing.client.gui.WhittlingTableScreen;
import com.github.no_name_provided.nnp_rune_smithing.client.renderers.CastingTableEntityRenderer;
import com.github.no_name_provided.nnp_rune_smithing.client.renderers.MelterBlockRenderer;
import com.github.no_name_provided.nnp_rune_smithing.client.renderers.RuneAnvilBlockRenderer;
import com.github.no_name_provided.nnp_rune_smithing.client.renderers.RuneBlockEntityRenderer;
import com.github.no_name_provided.nnp_rune_smithing.common.blocks.RSBlocks;
import com.github.no_name_provided.nnp_rune_smithing.common.blocks.TintedBlock;
import com.github.no_name_provided.nnp_rune_smithing.common.data_components.RuneAddedData;
import com.github.no_name_provided.nnp_rune_smithing.common.data_components.RuneData;
import com.github.no_name_provided.nnp_rune_smithing.common.data_components.RunesAdded;
import com.github.no_name_provided.nnp_rune_smithing.common.entities.RSEntities;
import com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems;
import com.github.no_name_provided.nnp_rune_smithing.common.items.TintedBlockItem;
import com.github.no_name_provided.nnp_rune_smithing.common.items.TintedItem;
import com.github.no_name_provided.nnp_rune_smithing.common.items.runes.AbstractRuneItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.ArrayList;
import java.util.List;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;
import static com.github.no_name_provided.nnp_rune_smithing.common.data_components.RSDataComponents.RUNES_ADDED;
import static com.github.no_name_provided.nnp_rune_smithing.common.data_components.RSDataComponents.RUNE_DATA;
import static com.github.no_name_provided.nnp_rune_smithing.common.fluids.FluidHelper.FLUID_SETS;
import static com.github.no_name_provided.nnp_rune_smithing.common.fluids.FluidHelper.tempToColor;
import static com.github.no_name_provided.nnp_rune_smithing.common.gui.menus.RSMenus.MELTER_MENU;
import static com.github.no_name_provided.nnp_rune_smithing.common.gui.menus.RSMenus.WHITTLING_TABLE_MENU;
import static com.github.no_name_provided.nnp_rune_smithing.common.items.runes.AbstractRuneItem.Type.PLACE_HOLDER;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
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
                RSItems.RUNES.getEntries().stream().map(e -> (Item) (e.get())).toArray(ItemLike[]::new)
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
        event.register(WHITTLING_TABLE_MENU.get(), WhittlingTableScreen::new);
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
        event.registerBlockEntityRenderer(
                RSEntities.RUNE_ANVIL.get(),
                RuneAnvilBlockRenderer::new
        );
    }
    
    @SubscribeEvent
    static void onItemTooltip(ItemTooltipEvent event) {
        // THis first bit should be moved to AbstractRune#tooltip or whatever
        if (event.getItemStack().getItem() instanceof AbstractRuneItem rune) {
            // This seems to always be an arraylist, but nothing in the documentation or code to guarantee
            // mutability. And there's no other way to edit this (in an event with reliable access to
            // player context). Another #BlameTheNeoForgeTeam blunder.
            event.getToolTip().add(Component.literal(rune.getType().name()).withStyle(ChatFormatting.DARK_PURPLE));
            event.getToolTip().add(Component.literal("Tier " + AbstractRuneItem.getMaterialTier(event.getItemStack())));
        } else {
            RunesAdded runes = event.getItemStack().get(RUNES_ADDED);
            if (null != runes) {
                if (event.getFlags().hasShiftDown()) {
                    runes.getLore().forEach(
                            // This seems to always be an arraylist, but nothing in the documentation or code to guarantee
                            // mutability. And there's no other way to edit this (in an event with reliable access to
                            // player context). Another #BlameTheNeoForgeTeam blunder.
                            event.getToolTip()::add
                    );
                } else {
                    event.getToolTip().add(Component.literal("Hold [SHIFT] to see runes."));
                }
            }
        }
    }
    
    // For some reason, this was loading on dedicated servers...
    // Seems like a neo problem?
    @SubscribeEvent //@OnlyIn(Dist.CLIENT)
    static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        PlayerRenderer renderer = event.getRenderer();
        PlayerModel<AbstractClientPlayer> model = renderer.getModel();
        float runeScale = 0.2f;
        float partialTick = event.getPartialTick();
        ItemRenderer iRenderer = Minecraft.getInstance().getItemRenderer();
        PoseStack poseStack = event.getPoseStack();
        Player player = event.getEntity();
        MultiBufferSource buffer = event.getMultiBufferSource();
        List<ItemStack> armorList = new ArrayList<>();
        armorList.add(player.getItemBySlot(EquipmentSlot.CHEST));
        armorList.add(player.getItemBySlot(EquipmentSlot.LEGS));
        armorList.add(player.getItemBySlot(EquipmentSlot.FEET));
        
        armorList.forEach(armor -> {
                    if (armor != ItemStack.EMPTY) {
                        RunesAdded runes = armor.getOrDefault(RUNES_ADDED, RunesAdded.DEFAULT.get());
                        if (runes != RunesAdded.DEFAULT.get()) {
                            List<RuneAddedData> runeDataList = getAllRuneAddedData(runes);
                            // Leg rotations still aren't quite right. Neither is vertical position while walking.
                            runeDataList.forEach(runeData -> {
                                        if (!(runeData.rune().getType() == PLACE_HOLDER) && armor.getItem() instanceof ArmorItem armorItem) {
                                            boolean isHelm = armorItem.getType() == ArmorItem.Type.HELMET;
                                            boolean isChest = armorItem.getType() == ArmorItem.Type.CHESTPLATE;
                                            boolean isLeggings = armorItem.getType() == ArmorItem.Type.LEGGINGS;
                                            boolean isBoots = armorItem.getType() == ArmorItem.Type.BOOTS;
                                            // Quietly ignore extensions made by other mods
                                            if (isHelm || isChest || isLeggings || isBoots) {
                                                
                                                
                                                poseStack.pushPose();
                                                
                                                poseStack.rotateAround(Axis.YN.rotationDegrees(player.yBodyRot), 0, 1, 0);
                                                
                                                if (player.isCrouching()) {
                                                    if (isChest) {
                                                        poseStack.rotateAround(Axis.XP.rotation(0.5f), 0, 1, 0);
                                                        poseStack.translate(0, 0, -0.1f);
                                                    } else {
                                                        poseStack.translate(0, 0, -0.23f);
                                                    }
                                                }
                                                
                                                // Rotation about hips
                                                if (isLeggings || isBoots) {
                                                    float rotScalingFactor = 0.925f; //isLeggings ? 2.0f * runeScale : 1f -0.2f;
                                                    if (runeData.rune().getType() == AbstractRuneItem.Type.TARGET || runeData.rune().getType() == AbstractRuneItem.Type.MODIFIER) {
                                                        // Model offsets are given in 16ths of a block, then normalized for PoseStack transforms.
                                                        // See: net.minecraft.client.model.HumanoidModel.setupAttackAnimation#L217 and net.minecraft.client.model.geom.ModelPart.translateAndRotate
                                                        poseStack.rotateAround(Axis.XP.rotation(model.rightLeg.xRot * rotScalingFactor), 0, 12.2f / 16, 0);
                                                    } else {
                                                        poseStack.rotateAround(Axis.XP.rotation(model.leftLeg.xRot * rotScalingFactor), 0, 12.2f / 16, 0);
                                                    }
                                                }
                                                
                                                if (isHelm) {
                                                    
                                                    // Handle chest runes
                                                } else if (isChest) {
                                                    // Center of chest
                                                    poseStack.translate(0, 1, 0.180);
                                                    double hOffset = 0.1;
                                                    switch (runeData.rune().getType()) {
                                                        case TARGET -> poseStack.translate(-hOffset, 0.12, 0);
                                                        case EFFECT -> poseStack.translate(hOffset, 0.12, 0);
                                                        case MODIFIER -> poseStack.translate(-hOffset, -.1, 0);
                                                        case AMPLIFIER -> poseStack.translate(hOffset, -.1, 0);
                                                        case PLACE_HOLDER -> {
                                                        }
                                                    }
                                                    // Handle leggings
                                                } else if (isLeggings) {
                                                    poseStack.translate(0, 0.52, 0);
                                                    double hOffset = 0.125;
                                                    switch (runeData.rune().getType()) {
                                                        case TARGET -> poseStack.translate(-hOffset, 0.0, 0.16);
                                                        case EFFECT -> poseStack.translate(hOffset, 0.0, 0.16);
                                                        case MODIFIER -> poseStack.translate(-hOffset, 0.0, -0.16);
                                                        case AMPLIFIER -> poseStack.translate(hOffset, 0.0, -0.16);
                                                        case PLACE_HOLDER -> {
                                                        }
                                                    }
                                                    // Handle boots
                                                } else if (isBoots) {
                                                    poseStack.translate(0, 0.12, 0);
                                                    double hOffset = 0.125;
                                                    switch (runeData.rune().getType()) {
                                                        case TARGET -> poseStack.translate(-hOffset, 0.0, 0.2);
                                                        case EFFECT -> poseStack.translate(hOffset, 0.0, 0.2);
                                                        case MODIFIER -> poseStack.translate(-hOffset, 0.0, -0.2);
                                                        case AMPLIFIER -> poseStack.translate(hOffset, 0.0, -0.2);
                                                        case PLACE_HOLDER -> {
                                                        }
                                                    }
                                                    // Handle Helmet - may need more special casing so it lines up with look vector
                                                }
                                                
                                                if (player.isCrouching()) {
                                                    Vec3 offset = renderer.getRenderOffset((AbstractClientPlayer) player, partialTick);
                                                    if (isChest) {
                                                        poseStack.translate(offset.x(), offset.y() - (double) 1 / 16, offset.z());
                                                    } else {
                                                        poseStack.translate(offset.x(), offset.y(), offset.z());
                                                    }
                                                }
                                                
                                                poseStack.scale(runeScale, runeScale, runeScale);
                                                // Increase my fudge factor
                                                if (isLeggings || isBoots) {
                                                    poseStack.scale(1, 1, 3);
                                                }
                                                
                                                // Rotation about own axis
                                                if (isLeggings || isBoots) {
                                                    if (runeData.rune().getType() == AbstractRuneItem.Type.TARGET || runeData.rune().getType() == AbstractRuneItem.Type.MODIFIER) {
                                                        poseStack.mulPose(Axis.XP.rotation(model.rightLeg.xRot * 0.9f * runeScale / 2));
                                                    } else {
                                                        poseStack.mulPose(Axis.XP.rotation(model.leftLeg.xRot * 0.9f * runeScale / 2));
                                                    }
                                                }
                                                
                                                ItemStack toRender = runeData.rune().getDefaultInstance();
                                                toRender.set(RUNE_DATA, new RuneData(runes.effectiveTier(), runeData.color()));
                                                
                                                iRenderer.renderStatic(
                                                        player,
                                                        toRender,
                                                        ItemDisplayContext.FIXED,
                                                        false,
                                                        poseStack,
                                                        buffer,
                                                        player.level(),
                                                        event.getPackedLight(),
                                                        OverlayTexture.NO_OVERLAY,
                                                        player.getId() + ItemDisplayContext.FIXED.ordinal()
                                                );
                                                
                                                poseStack.popPose();
                                            }
                                        }
                                    }
                            );
                            
                        }
                    }
                }
        );
        
    }
    
    private static ArrayList<RuneAddedData> getAllRuneAddedData(RunesAdded runes) {
        ArrayList<RuneAddedData> runeDataList = new ArrayList<>();
        runeDataList.add(runes.target());
        runeDataList.add(runes.effect());
        runeDataList.add(runes.modifier());
        runeDataList.add(runes.amplifier());
        
        return runeDataList;
    }
}
