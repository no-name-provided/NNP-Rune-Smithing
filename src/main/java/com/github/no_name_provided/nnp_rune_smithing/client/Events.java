package com.github.no_name_provided.nnp_rune_smithing.client;

import com.github.no_name_provided.nnp_rune_smithing.client.gui.MelterScreen;
import com.github.no_name_provided.nnp_rune_smithing.client.gui.WhittlingTableScreen;
import com.github.no_name_provided.nnp_rune_smithing.client.particles.RSParticleTypes;
import com.github.no_name_provided.nnp_rune_smithing.client.particles.RuneParticle;
import com.github.no_name_provided.nnp_rune_smithing.client.renderers.*;
import com.github.no_name_provided.nnp_rune_smithing.common.blocks.RSBlocks;
import com.github.no_name_provided.nnp_rune_smithing.common.blocks.RuneBlock;
import com.github.no_name_provided.nnp_rune_smithing.common.blocks.TintedBlock;
import com.github.no_name_provided.nnp_rune_smithing.common.blocks.TintedDropExperienceBlock;
import com.github.no_name_provided.nnp_rune_smithing.common.data_components.RuneAddedData;
import com.github.no_name_provided.nnp_rune_smithing.common.data_components.RuneData;
import com.github.no_name_provided.nnp_rune_smithing.common.data_components.RunesAdded;
import com.github.no_name_provided.nnp_rune_smithing.common.entities.RSEntities;
import com.github.no_name_provided.nnp_rune_smithing.common.items.LayeredTintedBlockItem;
import com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems;
import com.github.no_name_provided.nnp_rune_smithing.common.items.TintedBlockItem;
import com.github.no_name_provided.nnp_rune_smithing.common.items.TintedItem;
import com.github.no_name_provided.nnp_rune_smithing.common.items.runes.AbstractRuneItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.ParticleUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.ArrayList;
import java.util.List;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;
import static com.github.no_name_provided.nnp_rune_smithing.common.attachments.RSAttachments.VOID_ENDERMAN;
import static com.github.no_name_provided.nnp_rune_smithing.common.data_components.RSDataComponents.RUNES_ADDED;
import static com.github.no_name_provided.nnp_rune_smithing.common.data_components.RSDataComponents.RUNE_DATA;
import static com.github.no_name_provided.nnp_rune_smithing.common.fluids.FluidHelper.FLUID_SETS;
import static com.github.no_name_provided.nnp_rune_smithing.common.fluids.FluidHelper.tempToColor;
import static com.github.no_name_provided.nnp_rune_smithing.common.gui.menus.RSMenus.MELTER_MENU;
import static com.github.no_name_provided.nnp_rune_smithing.common.gui.menus.RSMenus.WHITTLING_TABLE_MENU;
import static com.github.no_name_provided.nnp_rune_smithing.common.items.runes.AbstractRuneItem.Type.PLACE_HOLDER;
import static com.github.no_name_provided.nnp_rune_smithing.common.recipes.RSRecipes.*;

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
        RSItems.ORE_BLOCKS.getEntries().forEach(oreBlock -> {
            if (oreBlock.get() instanceof LayeredTintedBlockItem item) {
                event.register((stack, index) -> item.colors.get(index), oreBlock.get());
            }
        });
        RSItems.RAW_ORES.getEntries().forEach(block -> {
            // For IDE. Should never be false (bar programmer error)
            if (block.get() instanceof TintedItem item) {
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
        RSBlocks.ORE_BLOCKS.getEntries().forEach(block -> {
            // For IDE. Should never be false (bar programmer error)
            if (block.get() instanceof TintedDropExperienceBlock oreBlock) {
                event.register((state, getter, pos, index) ->
                        index == 1 ? oreBlock.color : 0x00000000, block.get());
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
     * Doesn't make recipe book work, but does suppress missing recipe category spam in log. Could extend enum at
     * net.minecraft.client.RecipeBookCategories if I want to use accurate ones.
     */
    @SubscribeEvent
    private static void onRegisterRecipeBookCategories(RegisterRecipeBookCategoriesEvent event) {
        event.registerRecipeCategoryFinder(
                MELT.get(),
                holder -> RecipeBookCategories.BLAST_FURNACE_MISC
        );
        event.registerRecipeCategoryFinder(
                WHITTLING.get(),
                holder -> RecipeBookCategories.STONECUTTER
        );
        event.registerRecipeCategoryFinder(
                MOLDING.get(),
                holder -> RecipeBookCategories.CRAFTING_MISC
        );
    }
    
    /**
     * Used in place of a deferred register, since there's no proper registry.
     */
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                RSEntities.RUNE_BLOCK_ENTITY.get(),
                // A function of BlockEntityRendererProvider.Context to BlockEntityRenderer.
                RuneBlockRenderer::new
        );
        event.registerBlockEntityRenderer(
                RSEntities.CASTING_TABLE_BLOCK_ENTITY.get(),
                CastingTableEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                RSEntities.ALLOYER_BLOCK_ENTITY.get(),
                AlloyerBlockRenderer::new
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
        // This first bit should be moved to AbstractRune#tooltip or whatever
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
    
    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(RSParticleTypes.SELF_RUNE.get(), RuneParticle.RuneParticleProvider::new);
        event.registerSpriteSet(RSParticleTypes.WIELD_RUNE.get(), RuneParticle.RuneParticleProvider::new);
        event.registerSpriteSet(RSParticleTypes.COLLISION_RUNE.get(), RuneParticle.RuneParticleProvider::new);
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
        
        // Make sure we only process armor items we support
        List<ItemStack> armorList = new ArrayList<>();
        armorList.add(player.getItemBySlot(EquipmentSlot.HEAD));
        // We're not supporting elytra, modded or otherwise
        if (!(player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ElytraItem)) {
            armorList.add(player.getItemBySlot(EquipmentSlot.CHEST));
        }
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
                                            // Quietly ignore extensions made by other mods.
                                            // Redundant with the manually adding armor items to the list thing
                                            // I do above
                                            if (isHelm || isChest || isLeggings || isBoots) {
                                                
                                                poseStack.pushPose();
                                                
                                                if (!isHelm) {
                                                    poseStack.rotateAround(Axis.YN.rotationDegrees(player.yBodyRot), 0, 1, 0);
                                                } else {
                                                    poseStack.rotateAround(Axis.YN.rotationDegrees(player.getYHeadRot()), 0, 1, 0);
                                                    poseStack.rotateAround(Axis.XP.rotationDegrees(player.getViewXRot(partialTick)), 0, 1.4f, 0);
                                                }
                                                
                                                if (player.isCrouching()) {
                                                    if (isChest) {
                                                        poseStack.rotateAround(Axis.XP.rotation(0.5f), 0, 1, 0);
                                                        poseStack.translate(0, 0, -0.1f);
                                                    } else if (isHelm) {
                                                        poseStack.translate(0, 0, 0.17);
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
                                                    poseStack.translate(0, 1.75, 0);
                                                    
                                                    switch (runeData.rune().getType()) {
                                                        case TARGET -> poseStack.translate(0, 0.2, 0);
                                                        case EFFECT -> poseStack.translate(0, -0.1, -0.32);
                                                        case MODIFIER -> poseStack.translate(-.3, 0, 0);
                                                        case AMPLIFIER -> poseStack.translate(.3, 0, 0);
                                                        case PLACE_HOLDER -> {
                                                        }
                                                    }
                                                    // Handle chest runes
                                                } else if (isChest) {
                                                    // Rough center of chest
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
                                                }
                                                
                                                if (player.isCrouching()) {
                                                    Vec3 offset = renderer.getRenderOffset((AbstractClientPlayer) player, partialTick);
                                                    if (isChest || isHelm) {
                                                        poseStack.translate(offset.x(), offset.y() - (double) 1 / 16, offset.z());
                                                    } else {
                                                        poseStack.translate(offset.x(), offset.y(), offset.z());
                                                    }
                                                }
                                                
                                                poseStack.scale(runeScale, runeScale, runeScale);
                                                // Increase my fudge factor, since I can't seem to get this quite right
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
                                                } else if (isHelm) {
                                                    if (runeData.rune().getType() == AbstractRuneItem.Type.TARGET) {
                                                        poseStack.rotateAround(Axis.XP.rotationDegrees(90), 0, 0, 0);
                                                    } else if (runeData.rune().getType() != AbstractRuneItem.Type.EFFECT) {
                                                        poseStack.rotateAround(Axis.YP.rotationDegrees(90), 0, 0, 0);
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
    
    @SubscribeEvent
    static void onRenderHand(RenderHandEvent event) {
        // We only support items with a vanilla profile. Don't have a great way to check that,
        // so I'm just settling for a quick inheritance check
        // TODO: add blacklist by item or a gui that lets you dynamically place runes in specific locations
        
        ItemStack wielded = event.getItemStack();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource buffer = event.getMultiBufferSource();
        ItemRenderer iRenderer = Minecraft.getInstance().getItemRenderer();
        if (!wielded.isEmpty()) {
            Item wieldedItem = wielded.getItem();
            boolean isSword = wieldedItem instanceof SwordItem;
            boolean isPick = wieldedItem instanceof PickaxeItem;
            boolean isAxe = wieldedItem instanceof AxeItem;
            boolean isShovel = wieldedItem instanceof ShovelItem;
            boolean isHoe = wieldedItem instanceof HoeItem;
            
            RunesAdded runesAdded = wielded.get(RUNES_ADDED);
            if (null != runesAdded && (isSword || isPick || isAxe || isShovel || isHoe) && event.getHand() == InteractionHand.MAIN_HAND) {
                // Taken from the render arm event docstring, because this one lacks necessary
                // context. Should be safe because this is only fired on the client
                LocalPlayer player = Minecraft.getInstance().player;
                if (null != player) {
                    
                    List<RuneAddedData> runeDataList = getAllRuneAddedData(runesAdded);
                    runeDataList.forEach(runeAddedData -> {
                        if (runeAddedData.rune().getType() != PLACE_HOLDER) {
                            
                            poseStack.pushPose();
                            
                            ItemStack toRender = runeAddedData.rune().getDefaultInstance();
                            toRender.set(RUNE_DATA, new RuneData(runesAdded.effectiveTier(), runeAddedData.color()));
                            
                            // May need to add some version of this for mod compat:
                            // if (!net.neoforged.neoforge.client.extensions.common.IClientItemExtensions.of(stack).applyForgeHandTransform(poseStack, minecraft.player, humanoidarm, stack, partialTicks, equippedProgress, swingProgress)) // FORGE: Allow items to define custom arm animation
                            handleVanillaTransformations(poseStack, player, event.getHand() == InteractionHand.MAIN_HAND, toRender, event.getHand(), event.getEquipProgress(), event.getSwingProgress());
                            
                            // Can't quite understand the coordinate transformations here. Y is vertically up (not
                            // in the wielded frame) and X and Z seem to be roughly the same axis, with X also somewhat
                            // corresponding to depth. As a result of this fudge, offhand rendering doesn't look quite right
                            
                            // Not sure if these are quite staying in sync when an attack first starts...
                            if (isSword) {
                                float runeScale = 0.08f;
                                poseStack.scale(runeScale, runeScale, runeScale);
                                switch (runeAddedData.rune().getType()) {
                                    case TARGET -> poseStack.translate(0.1, 3, 1.5);
                                    case EFFECT -> poseStack.translate(0.1, 4, 1.9);
                                    case MODIFIER -> poseStack.translate(0.1, 5, 2.25);
                                    case AMPLIFIER -> poseStack.translate(0.1, 6, 2.55);
                                    case PLACE_HOLDER -> {
                                    }
                                }
                            } else if (isPick) {
                                float runeScale = 0.07f;
                                poseStack.scale(runeScale, runeScale, runeScale);
                                switch (runeAddedData.rune().getType()) {
                                    case TARGET -> poseStack.translate(0.3, 5.3, 0.45);
                                    case EFFECT -> poseStack.translate(0.3, 5.8, 1.45);
                                    case MODIFIER -> poseStack.translate(0.6, 5.4, 2.45);
                                    case AMPLIFIER -> poseStack.translate(0.6, 4.6, 3.1);
                                    case PLACE_HOLDER -> {
                                    }
                                }
                                // Could use more tweaking
                            } else if (isAxe) {
                                float runeScale = 0.08f;
                                poseStack.scale(runeScale, runeScale, runeScale);
                                switch (runeAddedData.rune().getType()) {
                                    case TARGET -> poseStack.translate(0.3, 4.6, 0.65);
                                    case EFFECT -> poseStack.translate(0.2, 4.4, 1.45);
                                    case MODIFIER -> poseStack.translate(-0.2, 4.2, 2.45);
                                    case AMPLIFIER -> poseStack.translate(-.5, 3.95, 3.6);
                                    case PLACE_HOLDER -> {
                                    }
                                }
                            } else if (isShovel) {
                                float runeScale = 0.08f;
                                poseStack.scale(runeScale, runeScale, runeScale);
                                switch (runeAddedData.rune().getType()) {
                                    case TARGET -> poseStack.translate(0.3, 5.4, 2.1);
                                    case EFFECT -> poseStack.translate(0.3, 5, 3);
                                    case MODIFIER -> poseStack.translate(0.3, 4.5, 1.8);
                                    case AMPLIFIER -> poseStack.translate(0.3, 4.1, 2.7);
                                    case PLACE_HOLDER -> {
                                    }
                                }
                            } else if (isHoe) {
                                float runeScale = 0.08f;
                                poseStack.scale(runeScale, runeScale, runeScale);
                                switch (runeAddedData.rune().getType()) {
                                    case TARGET -> poseStack.translate(0.0, 5, 0.25);
                                    case EFFECT -> poseStack.translate(0.2, 5, 0.9);
                                    case MODIFIER -> poseStack.translate(0.5, 4.6, 1.45);
                                    case AMPLIFIER -> poseStack.translate(0.5, 4.6, 2.2);
                                    case PLACE_HOLDER -> {
                                    }
                                }
                            }
                            
                            iRenderer.renderStatic(
                                    player,
                                    toRender,
                                    ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
                                    false,
                                    poseStack,
                                    buffer,
                                    player.level(),
                                    event.getPackedLight(),
                                    OverlayTexture.NO_OVERLAY,
                                    player.getId() + ItemDisplayContext.FIRST_PERSON_RIGHT_HAND.ordinal()
                            );
                            
                            poseStack.popPose();
                        }
                    });
                }
                
            }
        }
    }
    
    /**
     * Edited from ItemInHandRenderer#renderArmWithItem
     */
    private static void handleVanillaTransformations(PoseStack poseStack, LocalPlayer player, boolean usingRightArm, ItemStack toRender, InteractionHand hand, float equippedProgress, float swingProgress) {
        if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == hand) {
            if (toRender.getUseAnimation() == UseAnim.NONE) {
                applyItemArmTransform(poseStack, usingRightArm, equippedProgress);
            }
        } else if (player.isAutoSpinAttack()) {
            applyItemArmTransform(poseStack, usingRightArm, equippedProgress);
            int j = usingRightArm ? 1 : -1;
            poseStack.translate((float) j * -0.4F, 0.8F, 0.3F);
            poseStack.mulPose(Axis.YP.rotationDegrees((float) j * 65.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) j * -85.0F));
        } else {
            float f5 = -0.4F * Mth.sin(Mth.sqrt(swingProgress) * (float) Math.PI);
            float f6 = 0.2F * Mth.sin(Mth.sqrt(swingProgress) * (float) (Math.PI * 2));
            float f10 = -0.2F * Mth.sin(swingProgress * (float) Math.PI);
            int l = usingRightArm ? 1 : -1;
            poseStack.translate((float) l * f5, f6, f10);
            applyItemArmTransform(poseStack, usingRightArm, equippedProgress);
            applyItemArmAttackTransform(poseStack, usingRightArm, swingProgress);
        }
    }
    
    private static void applyItemArmTransform(PoseStack poseStack, boolean usingRightHand, float equippedProg) {
        int i = usingRightHand ? 1 : -1;
        poseStack.translate((float) i * 0.56F, -0.52F + equippedProg * -0.6F, -0.72F);
    }
    
    private static void applyItemArmAttackTransform(PoseStack poseStack, boolean usingRightHand, float swingProgress) {
        int i = usingRightHand ? 1 : -1;
        float f = Mth.sin(swingProgress * swingProgress * (float) Math.PI);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) i * (45.0F + f * -20.0F)));
        float f1 = Mth.sin(Mth.sqrt(swingProgress) * (float) Math.PI);
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) i * f1 * -20.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(f1 * -80.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees((float) i * -45.0F));
    }
    
    private static ArrayList<RuneAddedData> getAllRuneAddedData(RunesAdded runes) {
        ArrayList<RuneAddedData> runeDataList = new ArrayList<>();
        runeDataList.add(runes.target());
        runeDataList.add(runes.effect());
        runeDataList.add(runes.modifier());
        runeDataList.add(runes.amplifier());
        
        return runeDataList;
    }
    
    /**
     * Box has wrong location/shape. Made for use by onContinueHammering.
     */
    @SuppressWarnings("unused")
    private static void renderBreakingOutline(Pair<BlockPos, BlockPos> posPair) {
        VertexConsumer vc = Minecraft.getInstance().renderBuffers().outlineBufferSource().getBuffer(RenderType.lines());
        PoseStack stack = new PoseStack();
        stack.pushPose();
        LevelRenderer.renderLineBox(
                stack,
                vc,
                AABB.encapsulatingFullBlocks(posPair.getFirst(), posPair.getSecond()),
                0.9F,
                0.9F,
                0.9F,
                1.0F
        );
        
        stack.popPose();
    }
    
    @SubscribeEvent
    static void onClientEntityTick(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof Mob mob) {
            if (mob.getData(VOID_ENDERMAN)) {
                Level level = mob.level();
                spawnRuneEnhancedParticle(mob.getOnPos().above(2), level, RSItems.VOID_RUNE.get());
            }
        }
    }
    
    private static void spawnRuneEnhancedParticle(BlockPos pos, Level level, AbstractRuneItem rune) {
        List<Integer> colors = RuneBlock.effectToColor.get(RSItems.VOID_RUNE.get());
        ParticleUtils.spawnParticles(
                level,
                pos,
                3,
                1,
                2,
                true,
                ColorParticleOption.create(
                        RSParticleTypes.SELF_RUNE.get(),
                        (float) colors.getFirst() / 255,
                        (float) colors.get(1) / 255,
                        (float) colors.getLast() / 255
                )
        );
    }
}