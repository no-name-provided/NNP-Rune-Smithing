package com.github.no_name_provided.nnp_rune_smithing.client.renderers;

import com.github.no_name_provided.nnp_rune_smithing.common.capabilities.MelterCapability;
import com.github.no_name_provided.nnp_rune_smithing.common.entities.MelterBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import org.joml.Vector3i;

public class MelterBlockRenderer implements BlockEntityRenderer<MelterBlockEntity> {
    private final BlockEntityRendererProvider.Context CONTEXT;
    
    public MelterBlockRenderer(BlockEntityRendererProvider.Context context) {
        CONTEXT = context;
    }
    
    @Override
    public void render(MelterBlockEntity melter, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!melter.output.isEmpty() && null != melter.getLevel()) {
            poseStack.pushPose();
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(IClientFluidTypeExtensions.of(melter.output.getFluid()).getStillTexture());
            int color = IClientFluidTypeExtensions.of(melter.output.getFluid()).getTintColor(melter.output);
            
            VertexConsumer vc = bufferSource.getBuffer(ItemBlockRenderTypes.getRenderLayer(melter.output.getFluid().defaultFluidState()));
            
            // Vertexes are apparently rendered automatically, once you draw enough to make a quad.
            // All coordinates are in fractions of a block... which is usually 16 pixels on vanilla textures
            float y0 = 0.05f;
            float hPadding = 0.1f;
            float height = (9f/20f) * melter.output.getAmount()/ (float) MelterCapability.MelterFluidHandler.MELTER_CAPACITY;
            // Top
            drawQuad(vc, poseStack, hPadding, height, hPadding, 1f - hPadding, height, 1f - hPadding, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(), packedLight, color, new Vector3i(0, 1, 0));
            // "Front"
            drawQuad(vc, poseStack, hPadding, y0, hPadding, 1f - hPadding, height, hPadding, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(), packedLight, color, new Vector3i(0, 0, 1));
            
            
            // "Sides" - don't render if drawn directly because the effective normal vector is defined by the counterclockwise order in which points are drawn, and the sides' vertices aren't drawn in order.
            // As a workaround, we can draw one side and rotate it. For more complex shapes, we'd actually need to change the vertex draw order.
            poseStack.pushPose();
            poseStack.rotateAround(Axis.YP.rotationDegrees(90), 0.5f, 0.5f, 0.5f);
//            poseStack.mulPose(Axis.YP.rotationDegrees(-90));
            drawQuad(vc, poseStack, hPadding, y0, hPadding, 1f - hPadding, height, hPadding, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(), packedLight, color, new Vector3i(-1, 0, 0));
            poseStack.rotateAround(Axis.YP.rotationDegrees(180), 0.5f, 0.5f, 0.5f);
            drawQuad(vc, poseStack, hPadding, y0, hPadding, 1f - hPadding, height, hPadding, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(), packedLight, color, new Vector3i(-1, 0, 0));
            poseStack.popPose();
            
            
            // "Back" - Only renders from behind if vertices are drawn in reverse order or we "cheat" with rotations. Same problem as sides
            poseStack.pushPose();
            poseStack.rotateAround(Axis.YP.rotationDegrees(180), 0.5f, 0f, 0.5f);
            drawQuad(vc, poseStack, hPadding, y0, hPadding, 1f - hPadding, height, hPadding, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(), packedLight, color, new Vector3i(0, 0, 1));
            poseStack.popPose();
            
            poseStack.popPose();
        }
    }
    
    public static void drawFluidVertex(VertexConsumer vc, PoseStack poseStack, float x, float y, float z, float u, float v, int packedLight, int color, Vector3i normalVector) {
        vc.addVertex(poseStack.last().pose(), x, y, z).setColor(color).setUv(u, v).setLight(packedLight).setNormal(normalVector.x, normalVector.y, normalVector.x);
    }
    public static void drawQuad(VertexConsumer vc, PoseStack poseStack, float x0, float y0, float z0, float xf, float yf, float zf, float u0, float v0, float uf, float vf, int packedLight, int color, Vector3i normalVector) {
        // Vertices must be drawn counterclockwise because normal vector is ignored (still required to avoid crash).
     
            drawFluidVertex(vc, poseStack, x0, y0, z0, u0, v0, packedLight, color, normalVector);
            drawFluidVertex(vc, poseStack, x0, yf, zf, u0, vf, packedLight, color, normalVector);
            drawFluidVertex(vc, poseStack, xf, yf, zf, uf, vf, packedLight, color, normalVector);
            drawFluidVertex(vc, poseStack, xf, y0, z0, uf, v0, packedLight, color, normalVector);
        
    }
}
