package com.github.no_name_provided.nnp_rune_smithing.client.renderers;

import com.github.no_name_provided.nnp_rune_smithing.common.datamaps.CastableFluidData;
import com.github.no_name_provided.nnp_rune_smithing.common.datamaps.RSDataMaps;
import com.github.no_name_provided.nnp_rune_smithing.common.entities.CastingTableBlockEntity;
import com.github.no_name_provided.nnp_rune_smithing.common.fluids.FluidHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.FastColor;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

import java.util.AbstractMap;
import java.util.Map;

@SuppressWarnings("ClassCanBeRecord") // CONTEXT should be private
public class CastingTableEntityRenderer implements BlockEntityRenderer<CastingTableBlockEntity> {
    private final BlockEntityRendererProvider.Context CONTEXT;
    
    public CastingTableEntityRenderer(BlockEntityRendererProvider.Context context) {
        CONTEXT = context;
    }
    
    @Override
    public void render(CastingTableBlockEntity table, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemRenderer renderer = CONTEXT.getItemRenderer();
        
        // Items
        
        ItemStack mold = table.getItem(0);
        if (!mold.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5f, 0.95f, 0.5f);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90));
            poseStack.scale(0.95f, 0.95f, 0.95f);
            renderer.renderStatic(
                    table.getItem(0),
                    ItemDisplayContext.FIXED,
                    LightTexture.FULL_BRIGHT,
                    packedOverlay,
                    poseStack,
                    bufferSource,
                    table.getLevel(),
                    0
            );
            poseStack.popPose();
        }
        
        ItemStack output = table.getItem(1);
        if (!output.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5f, 0.95f, 0.5f);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90));
            poseStack.scale(0.5f, 0.5f, 0.5f);
            renderer.renderStatic(
                    table.getItem(1),
                    ItemDisplayContext.FIXED,
                    LightTexture.FULL_BRIGHT,
                    packedOverlay,
                    poseStack,
                    bufferSource,
                    table.getLevel(),
                    0
            );
            poseStack.popPose();
        }
        
        //Fluids
        
        poseStack.pushPose();
        if (!table.tank.isEmpty() && null != table.getLevel()) {
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(IClientFluidTypeExtensions.of(table.tank.getFluid()).getStillTexture());
            int color = getEffectiveColor(table);
            
            VertexConsumer vc = bufferSource.getBuffer(ItemBlockRenderTypes.getRenderLayer(table.tank.getFluid().defaultFluidState()));

            // Vertexes are apparently rendered automatically, once you draw enough to make a quad
            drawQuad(vc, poseStack, 0f, 0.97f, 0f, 1f, 0.97f, 1f, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(), packedLight, color);
        }
        poseStack.popPose();
    }
    
    private static int getEffectiveColor(CastingTableBlockEntity table) {
        CastableFluidData data = table.tank.getFluidHolder().getData(RSDataMaps.CASTABLE_FLUID_DATA);
        // May want to add an optional field to CastableFluidData for whether or not to use dynamic tints while casting/cooling
        if (null != data) {
            int solidColor = FastColor.ARGB32.color(255, data.colorWhenCool());
            // Only cooling to room temp (20 deg C)
            int temperature = (table.tank.getFluidType().getTemperature() - 20) * table.coolingTime / table.coolingTotalTime + 20;
            
            Map.Entry<Integer, Integer> lowerEntry;
            Map.Entry<Integer, Integer> upperEntry;
            if (temperature <= 199) {
                // The coldest color is decided by the datamap, not our temperature to color map.
                lowerEntry = new AbstractMap.SimpleEntry<>(20, solidColor);
                upperEntry = FluidHelper.tempToColor.floorEntry(200);
            } else {
                lowerEntry = FluidHelper.tempToColor.floorEntry(temperature);
                upperEntry = FluidHelper.tempToColor.ceilingEntry(Math.min(temperature, 1092));
            }
            
            int alpha = 255;
            if (lowerEntry.equals(upperEntry)) {
                
                return FastColor.ARGB32.color(alpha, lowerEntry.getValue());
            }
            
            int changeTemp = temperature - lowerEntry.getKey();
            int rangeTemp = upperEntry.getKey() - lowerEntry.getKey();
            int rangeRed = FastColor.ARGB32.red(upperEntry.getValue()) - FastColor.ARGB32.red(lowerEntry.getValue());
            float slopeRed = (float) (rangeRed) / (rangeTemp);
            int rangeGreen = FastColor.ARGB32.green(upperEntry.getValue()) - FastColor.ARGB32.green(lowerEntry.getValue());
            float slopeGreen = (float) (rangeGreen) / (rangeTemp);
            int rangeBlue = FastColor.ARGB32.blue(upperEntry.getValue()) - FastColor.ARGB32.blue(lowerEntry.getValue());
            float slopeBlue = (float) (rangeBlue) / (rangeTemp);
            
            return FastColor.ARGB32.color(
                    alpha,
                    (int)(FastColor.ARGB32.red(lowerEntry.getValue()) + changeTemp * slopeRed),
                    (int)(FastColor.ARGB32.green(lowerEntry.getValue()) + changeTemp * slopeGreen),
                    (int)(FastColor.ARGB32.blue(lowerEntry.getValue()) + changeTemp * slopeBlue)
            );
        } else {
            
            return IClientFluidTypeExtensions.of(table.tank.getFluid()).getTintColor(table.tank);
        }
    }
    
    public static void drawFluidVertex(VertexConsumer vc, PoseStack poseStack, float x, float y, float z, float u, float v, int packedLight, int color) {
        vc.addVertex(poseStack.last().pose(), x, y, z).setColor(color).setUv(u, v).setLight(packedLight).setNormal(0, 1, 0);
    }
    public static void drawQuad(VertexConsumer vc, PoseStack poseStack, float x0, float y0, float z0, float xf, float yf, float zf, float u0, float v0, float uf, float vf, int packedLight, int color) {
        drawFluidVertex(vc, poseStack, x0, y0, z0, u0, v0, packedLight, color);
        drawFluidVertex(vc, poseStack, x0, yf, zf, u0, vf, packedLight, color);
        drawFluidVertex(vc, poseStack, xf, yf, zf, uf, vf, packedLight, color);
        drawFluidVertex(vc, poseStack, xf, y0, z0, uf, v0, packedLight, color);
    }
}
