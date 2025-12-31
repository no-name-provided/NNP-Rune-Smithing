package com.github.no_name_provided.nnp_rune_smithing.client.renderers;

import com.github.no_name_provided.nnp_rune_smithing.common.blocks.AlloyerBlock;
import com.github.no_name_provided.nnp_rune_smithing.common.entities.AlloyerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Quaternionf;
import org.joml.Vector3i;

public class AlloyerBlockRenderer implements BlockEntityRenderer<AlloyerBlockEntity> {
    private final BlockEntityRendererProvider.Context CONTEXT;
    
    public AlloyerBlockRenderer(BlockEntityRendererProvider.Context context) {
        CONTEXT = context;
    }
    
    @Override
    public void render(AlloyerBlockEntity alloyer, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (null != alloyer.getLevel()) {
            Direction facing = alloyer.getBlockState().getValue(AlloyerBlock.FACING);
            float y0 = 1f / 16f;
            float yf = 1f - y0;
            if (!alloyer.getFluidInTank(0).isEmpty()) {
                // For some reason, the sum of packed lightr and overlay is always really low. Temporarily fixed by hard coding full bright.
                drawTankContent(poseStack, bufferSource, alloyer.getFluidInTank(0), LightTexture.FULL_BRIGHT, y0, yf, 1f / 64, 5f / 16, facing);
            }
            // The last tank (result) is in the center
            if (!alloyer.getFluidInTank(2).isEmpty()) {
                drawTankContent(poseStack, bufferSource, alloyer.getFluidInTank(2), LightTexture.FULL_BRIGHT, y0, yf, 11f / 32, 10f / 16, facing);
            }
            if (!alloyer.getFluidInTank(1).isEmpty()) {
                drawTankContent(poseStack, bufferSource, alloyer.getFluidInTank(1), LightTexture.FULL_BRIGHT, y0, yf, 11f / 16, 63f / 64, facing);
            }
            
        }
    }
    
    public static void drawTankContent(PoseStack poseStack, MultiBufferSource bufferSource, FluidStack contents, int packedLight, float y0, float yf, float x0, float xf, Direction facing) {
        poseStack.pushPose();
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(IClientFluidTypeExtensions.of(contents.getFluid()).getStillTexture());
        int color = IClientFluidTypeExtensions.of(contents.getFluid()).getTintColor(contents);
        
        VertexConsumer vc = bufferSource.getBuffer(ItemBlockRenderTypes.getRenderLayer(contents.getFluid().defaultFluidState()));
        
        // Account for direction of block (model)
        poseStack.rotateAround(getRotationFromDirection(facing), 0.5f, 0.5f, 0.5f);
        
        // Vertexes are apparently rendered automatically, once you draw enough to make a quad.
        // All coordinates are in fractions of a block... which is usually 16 pixels on vanilla textures
        float height = (yf - y0) * contents.getAmount() / (float) AlloyerBlockEntity.TANK_CAPACITY;
        // Top
        drawQuad(vc, poseStack, x0, y0 + height, 1f / 16, xf, y0 + height, 1f - 1f / 16, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(), packedLight, color, new Vector3i(0, 1, 0));
        
        // Prevent compression by only rendering a proportionate fraction of the fluid texture
        float effectiveVf = sprite.getV0() + (sprite.getV1() - sprite.getV0()) * (float) contents.getAmount() / AlloyerBlockEntity.TANK_CAPACITY;
        
        // "Front"
        drawQuad(vc, poseStack, x0, y0, 1f / 16, xf, height + y0, 1f / 16, sprite.getU0(), sprite.getV0(), sprite.getU1(), effectiveVf, packedLight, color, new Vector3i(0, 0, 1));
        
        // "Back" - Only renders from behind if vertices are drawn in reverse order or we "cheat" with rotations. Same problem as sides
        poseStack.rotateAround(Axis.YP.rotationDegrees(180), (x0 + xf) / 2, 0.5f, 0.5f);
        drawQuad(vc, poseStack, x0, y0, 1f / 16, xf, height + y0, 1f / 16, sprite.getU0(), sprite.getV0(), sprite.getU1(), effectiveVf, packedLight, color, new Vector3i(0, 0, 1));

//        poseStack.pushPose();
//        poseStack.rotateAround(Axis.YP.rotationDegrees(180), 0.5f, 0.5f, 0.5f);
//        drawQuad(vc, poseStack, x0, y0, 15f / 16, xf, height + y0, 15f / 16, sprite.getU0(), sprite.getV0(), sprite.getU1(), effectiveVf, packedLight, color, new Vector3i(0, 0, 1));
//        poseStack.popPose();
        
        poseStack.popPose();
    }
    
    public static void drawFluidVertex(VertexConsumer vc, PoseStack poseStack, float x, float y, float z, float u, float v, int packedLight, int color, Vector3i normalVector) {
        vc.addVertex(poseStack.last().pose(), x, y, z).setColor(color).setUv(u, v).setLight(packedLight).setNormal(normalVector.x, normalVector.y, normalVector.z);
    }
    
    public static void drawQuad(VertexConsumer vc, PoseStack poseStack, float x0, float y0, float z0, float xf, float yf, float zf, float u0, float v0, float uf, float vf, int packedLight, int color, Vector3i normalVector) {
        // Vertices must be drawn counterclockwise because normal vector is ignored (still required to avoid crash).
        
        drawFluidVertex(vc, poseStack, x0, y0, z0, u0, v0, packedLight, color, normalVector);
        drawFluidVertex(vc, poseStack, x0, yf, zf, u0, vf, packedLight, color, normalVector);
        drawFluidVertex(vc, poseStack, xf, yf, zf, uf, vf, packedLight, color, normalVector);
        drawFluidVertex(vc, poseStack, xf, y0, z0, uf, v0, packedLight, color, normalVector);
        
    }
    
    /**
     * In vanilla, the default direction is UP. However, horizontal facing blocks default to facing NORTH, so that's
     * what I actually build my graphics around. This method replicates
     * {@link net.minecraft.core.Direction#getRotation()}, but assumes the default direction is NORTH instead of UP.
     * <p></p>
     * Good candidate for either mixing into Direction or adding to a helper class.
     */
    public static Quaternionf getRotationFromDirection(Direction direction) {
        return switch (direction) {
            case DOWN -> new Quaternionf().rotationX((float) Math.PI / 2);
            case UP -> new Quaternionf().rotationX(-(float) Math.PI / 2);
            case NORTH -> new Quaternionf();
            case SOUTH -> new Quaternionf().rotationY((float) (Math.PI));
            case WEST -> new Quaternionf().rotationY((float) (Math.PI / 2));
            case EAST -> new Quaternionf().rotationY(-(float) (Math.PI / 2));
        };
    }
}
