package com.github.no_name_provided.nnp_rune_smithing.client.dynamic_lights;

import com.github.no_name_provided.nnp_rune_smithing.common.entities.RSEntities;
import com.github.no_name_provided.nnp_rune_smithing.common.entities.RuneBlockEntity;
import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Range;

import java.util.Optional;

public class LightRuneWorldIlluminator implements DynamicLightBehavior {
    final BlockPos pos;
    int brightness;
    int radius;
    int height;
    Level level;
    
    BoundingBox boundingBox;
    
    boolean hasChanged = false;
    
    public LightRuneWorldIlluminator(BlockPos runePos, int brightness, int radius, int height, Level level) {
        this.pos = runePos;
        this.brightness = brightness;
        this.radius = radius;
        this.height = height;
        this.level = level;
        
        // Probably pointless, but might as well explicitly calc once and cache this value
        this.boundingBox = new BoundingBox(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    }
    
    
    /**
     * Determine the emitted light level at a point BlockPos the bounding box.
     * @param queriedPos The position being queried, which will be within the bounding box.
     * @param fallOffRatio The ratio at which the light levels decrease beyond the bounding box.
     * @return The light level.
     */
    @Override
    public @Range(from = 0L, to = 15L) double lightAtPos(BlockPos queriedPos, double fallOffRatio) {
        double distanceFromCenter = queriedPos.distToCenterSqr(pos.getX(), pos.getY(), pos.getZ());
        // Simulate lighting a sphere by preventing light falloff in radius, and reducing light falloff beyond radius
        if (distanceFromCenter < radius) {
         
            return brightness;
        } else {
         
            return Mth.clamp(brightness - (distanceFromCenter - radius) / 4 * fallOffRatio, 0, 15);
        }
    }
    
    /**
     * Provides the area that is fully illuminated (no fall off light).
     * @return Special record which unfortunately shares a name with the Minecraft class that represents the same thing,
     * but has more convenience methods.
     */
    @Override
    public BoundingBox getBoundingBox() {
        return boundingBox;
    }
    
    /**
     * {@return {@code true} if this dynamic lighting source has been removed, or {@code false} otherwise} By default,
     * dynamic lighting behavior must be removed explicitly (as-in this returns {@code false}). This method exists for
     * cases in which the removal of the source is not a set event and can only be known for sure through polling its
     * state.
     */
    @Override
    public boolean isRemoved() {
        Optional<RuneBlockEntity> runes = this.level.getBlockEntity(pos, RSEntities.RUNE_BLOCK_ENTITY.get());
        
        return runes.isEmpty();
    }
    
    /**
     * {@return {@code true} if this dynamic lighting source state has changed since the last time this function was
     * called, or {@code false} otherwise}
     */
    @Override
    public boolean hasChanged() {
        
        return hasChanged;
    }
    
    public BlockPos getPos() {
       
        return pos;
    }
    
    @SuppressWarnings("unused")
    public int getBrightness() {
       
        return brightness;
    }
    
    @SuppressWarnings("unused")
    public void setBrightness(int brightness) {
        this.brightness = brightness;
        hasChanged = true;
    }
    
    @SuppressWarnings("unused")
    public int getRadius() {
        
        return radius;
    }
    
    @SuppressWarnings("unused")
    public void setRadius(int radius) {
        this.radius = radius;
        hasChanged = true;
    }
    
    public int getHeight() {
       
        return height;
    }
    
    public void setHeight(int height) {
        this.height = height;
        hasChanged = true;
    }
}
