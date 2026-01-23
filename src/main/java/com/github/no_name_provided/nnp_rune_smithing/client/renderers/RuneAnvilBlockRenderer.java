package com.github.no_name_provided.nnp_rune_smithing.client.renderers;

import com.github.no_name_provided.nnp_rune_smithing.common.entities.RuneAnvilBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;

@SuppressWarnings("ClassCanBeRecord")
public class RuneAnvilBlockRenderer implements BlockEntityRenderer<RuneAnvilBlockEntity> {
    final BlockEntityRendererProvider.Context CONTEXT;
    
    public RuneAnvilBlockRenderer(BlockEntityRendererProvider.Context context) {
        CONTEXT = context;
    }
    
    /**
     * Renders the block entity. Called each render tick when not culled.
     *
     * @param anvil         The BlockEntity being rendered.
     * @param partialTick   The partial tick (fraction of the time between render ticks that has elapsed?).
     * @param poseStack     The stack of matrices used to specify the location, orientation, and size of rendered
     *                      things.
     * @param bufferSource  A provider for buffers that can be used to queue up things to be rendered.
     * @param packedLight   A light level at the BlockPos.
     * @param packedOverlay A different light level at the BlockPos?
     */
    @Override
    public void render(RuneAnvilBlockEntity anvil, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = anvil.getLevel();
        // Not using #hasLevel here, because the compiler is stupid and gives me a vacuous dereference warning
        if (null != level) {
            ItemRenderer renderer = CONTEXT.getItemRenderer();
            
            poseStack.pushPose();
            poseStack.translate(0.5f, 1.02f, 0.5f);
            poseStack.scale(0.4f, 0.4f, 0.4f);
            if (!anvil.seeImmutableBase().isEmpty()) {
                poseStack.pushPose();
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                renderer.renderStatic(
                        anvil.seeImmutableBase(),
                        ItemDisplayContext.FIXED,
                        LightTexture.FULL_BRIGHT,
                        packedOverlay,
                        poseStack,
                        bufferSource,
                        level,
                        level.random.nextInt()
                );
                poseStack.popPose();
            }
            if (!anvil.seeImmutableAddition().isEmpty()) {
                poseStack.pushPose();
                poseStack.translate(0f, 0.05f, 0.1f);
                poseStack.scale(0.3f, 0.4f, 0.3f);
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                renderer.renderStatic(
                        anvil.seeImmutableAddition(),
                        ItemDisplayContext.FIXED,
                        LightTexture.FULL_BRIGHT,
                        packedOverlay,
                        poseStack,
                        bufferSource,
                        level,
                        level.random.nextInt()
                );
                poseStack.popPose();
            }
            if (!anvil.seeImmutableResult().isEmpty()) {
                poseStack.pushPose();
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                renderer.renderStatic(
                        anvil.seeImmutableResult(),
                        ItemDisplayContext.FIXED,
                        LightTexture.FULL_BRIGHT,
                        packedOverlay,
                        poseStack,
                        bufferSource,
                        level,
                        level.random.nextInt()
                );
                poseStack.popPose();
            }
            
            poseStack.popPose();
        }
    }
}
