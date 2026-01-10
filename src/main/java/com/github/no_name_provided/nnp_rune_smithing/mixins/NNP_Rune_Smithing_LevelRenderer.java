package com.github.no_name_provided.nnp_rune_smithing.mixins;

import com.github.no_name_provided.nnp_rune_smithing.common.attachments.MarkedBlocksFromSightRune;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.no_name_provided.nnp_rune_smithing.common.attachments.RSAttachments.SIGHT_RUNE_MARKED_BLOCKS;

@Mixin(LevelRenderer.class)
public abstract class NNP_Rune_Smithing_LevelRenderer implements ResourceManagerReloadListener, AutoCloseable {
    @Final @Shadow
    private RenderBuffers renderBuffers;
    
    @Shadow @Final private EntityRenderDispatcher entityRenderDispatcher;
    
    @Inject(method = "renderLevel(Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderDebug(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/Camera;)V", shift = At.Shift.BY, by = 4))
    private void nnp_rune_smithing_renderLevel(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci, @Local PoseStack poseStack) {
        if (true) {
            return;
        }
        
        ClientLevel level = Minecraft.getInstance().level;
        if (null != level) {
            Optional<MarkedBlocksFromSightRune> toRenderOptional = level.getExistingData(SIGHT_RUNE_MARKED_BLOCKS);
            if (toRenderOptional.isPresent()) {
                List<BlockPos> toRender = new ArrayList<>();
                toRenderOptional.get().posList().entrySet().stream()
                        .filter(entry -> entry.getKey().distanceSquared(camera.getEntity().chunkPosition()) < 100)
                        .map(Map.Entry::getValue)
                        .forEach(toRender::addAll);
                
                renderBuffers.bufferSource().endBatch();
                poseStack.pushPose();
                renderBuffers.outlineBufferSource().endOutlineBatch();
                VertexConsumer vc = renderBuffers.outlineBufferSource().getBuffer(RenderType.debugFilledBox()); //renderBuffers.bufferSource().getBuffer(RenderType.debugFilledBox());
                
                for (BlockPos pos : toRender) {
                
//                    Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
//                            level.getBlockState(pos),
//
//                    );
                    
                    renderBuffers.outlineBufferSource().setColor(255, 255, 255, 255);
                    for (Direction face : Direction.values()) {
                        LevelRenderer.renderFace(
                                poseStack,
                                vc,
                                face,
                                (pos.getX() - (float) camera.getPosition().x()) - 0.01f,
                                (pos.getY() - (float) camera.getPosition().y()) - 0.01f,
                                (pos.getZ() - (float) camera.getPosition().z()) - 0.01f,
                                (pos.getX() + 1 - (float) camera.getPosition().x()) + 0.01f,
                                (pos.getY() + 1 - (float) camera.getPosition().y()) + 0.01f,
                                (pos.getZ() + 1 - (float) camera.getPosition().z()) + 0.01f,
                                200f / 255,
                                200f / 255,
                                200f / 255,
                                100f / 255
                        );
                    }
                }
                renderBuffers.outlineBufferSource().endOutlineBatch();
                
                poseStack.popPose();
            }
        }
    }
    
}
