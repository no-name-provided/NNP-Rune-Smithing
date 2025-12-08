package com.github.no_name_provided.nnp_rune_smithing.client.renderers;

import com.github.no_name_provided.nnp_rune_smithing.common.attachments.RSAttachments;
import com.github.no_name_provided.nnp_rune_smithing.common.entities.RuneBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Vector3f;

import java.util.List;

public class RuneBlockRenderer implements BlockEntityRenderer<RuneBlockEntity> {
    private final BlockEntityRendererProvider.Context CONTEXT;
    private final float PADDING = 0.2f;
    private final float WIDTH = 0.25f;
    private final float HEIGHT = 0.5f;
    private final List<Vector3f> TRANSLATIONS = List.of(
            new Vector3f(PADDING + WIDTH / 2f, 0f, PADDING + HEIGHT / 2f),
            new Vector3f(PADDING, 0f, 1f - PADDING / 2f),
            new Vector3f(1 - PADDING - WIDTH / 2f, 0f, 1 - PADDING / 2f),
            new Vector3f(1 - PADDING - WIDTH / 2f, 0f, PADDING + HEIGHT / 2f)
    );
    
    public RuneBlockRenderer(BlockEntityRendererProvider.Context context) {
        CONTEXT = context;
    }
    
    @Override
    public void render(RuneBlockEntity runes, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemRenderer renderer = CONTEXT.getItemRenderer();
        for (int i = 0; i < runes.getContainerSize(); i++) {
            poseStack.pushPose();
            // Rotation about center of block - last transformation applied
            switch (runes.getBlockState().getValue(BlockStateProperties.FACING)) {
                case DOWN -> {
                }
                case UP -> rotateAboutCenter(poseStack, Axis.XP, 180);
                case NORTH -> rotateAboutCenter(poseStack, Axis.XP, 90);
                case SOUTH -> rotateAboutCenter(poseStack, Axis.XP, -90);
                case WEST -> rotateAboutCenter(poseStack, Axis.ZP, -90);
                case EAST -> rotateAboutCenter(poseStack, Axis.ZP, 90);
            }
            
            Vector3f translation = TRANSLATIONS.get(i);
            poseStack.translate(translation.x, translation.y, translation.z);
            // Rotation about own axis - first transformation applied
            poseStack.mulPose(Axis.XP.rotationDegrees(-90));
            
            ItemStack rune = runes.getItem(i);
            if (!rune.isEmpty()) {
                renderer.renderStatic(
                        runes.getItem(i),
                        ItemDisplayContext.GROUND,
                        LightTexture.FULL_BRIGHT,
                        packedOverlay,
                        poseStack,
                        bufferSource,
                        runes.getLevel(),
                        0
                );
            }
            poseStack.popPose();
            
            int radius = runes.getRadius();
            int height = runes.getHeight();
            BlockPos offset = runes.getOffset();
            // Should be safe since this should only run on the client
            Player player = Minecraft.getInstance().player;
            if (radius * height > 0 && null != player && player.getData(RSAttachments.SHOW_RUNE_BLOCK_BOUNDING_BOXES)) {
                poseStack.pushPose();
                // Adapted from net.minecraft.client.renderer.blockentity.StructureBlockRenderer.render
                // Level renderer seems to use current coords by default, so only offsets should be provided
                LevelRenderer.renderLineBox(
                        poseStack,
                        bufferSource.getBuffer(RenderType.lines()),
                        offset.getX() - radius,
                        offset.getY(),
                        offset.getZ() - radius,
                        offset.getX() + radius,
                        offset.getY() + height,
                        offset.getZ() + radius,
                        0.9F,
                        0.9F,
                        0.9F,
                        1.0F,
                        0.5F,
                        0.5F,
                        0.5F
                );
                poseStack.popPose();
            }
        }
    }
    
    public static void rotateAboutCenter(PoseStack stack, Axis axis, int deg) {
        stack.rotateAround(axis.rotationDegrees(deg), 0.5f, 0.5f, 0.5f);
    }
}
