package com.github.no_name_provided.nnp_rune_smithing.common.blocks;

import net.minecraft.core.Vec3i;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;

import java.util.Arrays;

public class TintedBlock extends Block {
    public final int COLOR;
    
    public TintedBlock(Properties properties, int color) {
        super(properties.mapColor(getClosestMapColor(color)));
        COLOR = color;
    }
    
    public static MapColor getClosestMapColor(int color) {
        int red = FastColor.ARGB32.red(color);
        int green = FastColor.ARGB32.green(color);
        int blue = FastColor.ARGB32.blue(color);
        colorSetHolder closest = new colorSetHolder(MapColor.byId(0), calculateSquaredError(MapColor.byId(0), red, green, blue));
        
        // Requires AT, since hardcoding is brittle and length isn't exposed
        Arrays.stream(MapColor.MATERIAL_COLORS).forEach(mapColor -> {
            // The array has 64 entries, but a couple aren't actually used and no default value is provided
            if (null != mapColor) {
                double newSquaredError = calculateSquaredError(mapColor, red, green, blue);
                if (newSquaredError < closest.getSquaredError()) {
                    closest.setMapColor(mapColor);
                    closest.setSquaredError(newSquaredError);
                }
            }
        });
        
        return closest.getMapColor();
    }
    
    public static double calculateSquaredError(MapColor mapColor, int red, int green, int blue) {
        int mapRed = FastColor.ARGB32.red(mapColor.col);
        int mapGreen = FastColor.ARGB32.green(mapColor.col);
        int mapBlue = FastColor.ARGB32.blue(mapColor.col);
        
        return new Vec3i(mapRed - red, mapGreen - green, mapBlue - blue).distSqr(Vec3i.ZERO);
    }
    
    private static class colorSetHolder {
        private MapColor mapColor;
        private double squaredError;
        
        public colorSetHolder(MapColor mapColor, double squaredError) {
            this.mapColor = mapColor;
            this.squaredError = squaredError;
        }
        
        public MapColor getMapColor() {
            return mapColor;
        }
        
        public void setMapColor(MapColor mapColor) {
            this.mapColor = mapColor;
        }
        
        public double getSquaredError() {
            return squaredError;
        }
        
        public void setSquaredError(double squaredError) {
            this.squaredError = squaredError;
        }
    }
}
