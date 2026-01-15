package com.github.no_name_provided.nnp_rune_smithing.client;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import org.joml.Vector3f;

import javax.annotation.Nullable;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

public class MoltenFluidTypeClientExtensions implements IClientFluidTypeExtensions {

    int TINT_COLOR;

    MoltenFluidTypeClientExtensions(int ARGBColor) {
        TINT_COLOR = ARGBColor;
    }

    @Override
    public ResourceLocation getFlowingTexture(
            @Nullable FluidState state,
            @Nullable BlockAndTintGetter getter,
            @Nullable BlockPos pos
    ) {
        return ResourceLocation.fromNamespaceAndPath(MODID, "block/molten_metal_flow");
    }

    @Override
    public ResourceLocation getFlowingTexture() {
        return getFlowingTexture(null, null, null);
    }

    @Override
    public ResourceLocation getStillTexture(
            @Nullable FluidState state,
            @Nullable BlockAndTintGetter getter,
            @Nullable BlockPos pos
    ) {
        return ResourceLocation.fromNamespaceAndPath(MODID, "block/molten_metal_still");
    }
    @Override
    public ResourceLocation getStillTexture() {
        return getStillTexture(null, null, null);
    }

    /**
     * Modifies how the fog is currently being rendered when the camera is
     * within a fluid.
     *
     * @param camera         the camera instance
     * @param mode           the type of fog being rendered
     * @param renderDistance the render distance of the client
     * @param partialTick    the delta time of where the current frame is within a tick
     * @param nearDistance   the near plane of where the fog starts to render
     * @param farDistance    the far plane of where the fog ends rendering
     * @param shape          the shape of the fog being rendered
     */
    @Override
    public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape) {
//        FogRenderer.setupFog(camera, FogRenderer.FogMode.FOG_TERRAIN, farDistance, true, partialTick);
        RenderSystem.setShaderFogStart(0.25f);
        RenderSystem.setShaderFogEnd(1.0f);
        RenderSystem.setShaderFogShape(shape);
        // Following causes crash? Socket disconnect?
//        net.neoforged.neoforge.client.ClientHooks.onFogRender(
//                FogRenderer.FogMode.FOG_TERRAIN,
//                FogType.NONE,
//                camera,
//                partialTick,
//                renderDistance,
//                0.25f,
//                1.0f,
//                shape
//        );
    }

    /**
     * Modifies the color of the fog when the camera is within the fluid.
     *
     * <p>The result expects a three float vector representing the red, green,
     * and blue channels respectively. Each channel should be between [0,1].
     *
     * @param camera            the camera instance
     * @param partialTick       the delta time of where the current frame is within a tick
     * @param level             the level the camera is located in
     * @param renderDistance    the render distance of the client
     * @param darkenWorldAmount the amount to darken the world by
     * @param fluidFogColor     the current color of the fog
     * @return the color of the fog
     */
    @Override
    public Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
        return new Vector3f(FastColor.ARGB32.red(TINT_COLOR), FastColor.ARGB32.green(TINT_COLOR), FastColor.ARGB32.blue(TINT_COLOR)).div(256.0f);
    }

    @Override
    public int getTintColor() {
        return FastColor.ARGB32.color(252, TINT_COLOR);
    }
}
