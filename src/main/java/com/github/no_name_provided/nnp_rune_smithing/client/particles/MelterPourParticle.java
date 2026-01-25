package com.github.no_name_provided.nnp_rune_smithing.client.particles;

import com.github.no_name_provided.nnp_rune_smithing.client.particles.options.PourParticleOption;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * The base class for every particle emitted by a rune in the world.
 */
public class MelterPourParticle extends TextureSheetParticle {
    private final SpriteSet spriteSet;
    private final int maxBounceTime = 5;
    private int bounceTime = maxBounceTime;
    
    public MelterPourParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet spriteSet, Fluid fluid) {
        super(level, x, y, z);
        
        this.spriteSet = spriteSet;
        Vec3 origin = new Vec3(x, y, z);
        this.setPos(origin.x, origin.y, origin.z);
        // The two vanilla fluids (and maybe milk?) don't implement tint.
        // May still have issues with modded fluids that don't expose tint, since I'm not checking sprites directly
        if (fluid == Fluids.WATER) {
            this.setColor(
                    // Color from net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions#getTintColor
                    FastColor.ARGB32.red(-12618012) / 255f,
                    FastColor.ARGB32.green(-12618012) / 255f,
                    FastColor.ARGB32.blue(-12618012) / 255f
            );
        } else if (fluid == Fluids.LAVA) {
            this.setColor(
                    217 / 255f,
                    102 / 255f,
                    22 / 255f
            );
        } else {
            IClientFluidTypeExtensions extension = IClientFluidTypeExtensions.of(fluid);
            this.setColor(
                    FastColor.ARGB32.red(extension.getTintColor()) / 255f,
                    FastColor.ARGB32.green(extension.getTintColor()) / 255f,
                    FastColor.ARGB32.blue(extension.getTintColor()) / 255f
            );
        }
        
        this.gravity = 0.75f;
        this.friction = 0.999f;
        this.xd = xSpeed / 10d;
        this.yd = ySpeed;
        this.zd = zSpeed / 10d;
        this.quadSize = this.quadSize * (this.random.nextFloat() * 2.0f + 0.2f);
        
        // Set the initial sprite here since ticking is not guaranteed to set the sprite before the render method is called.
        this.setSpriteFromAge(spriteSet);
//        this.setSprite(Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(extension.getFlowingTexture()));
        
        this.lifetime = 20;
    }
    
    /**
     * Runs every tick.
     */
    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        if (age++ < lifetime) {
            xd += random.nextFloat() / 500.0f * (float) (random.nextBoolean() ? 1 : -1);
            zd += random.nextFloat() / 500.0f * (float) (random.nextBoolean() ? 1 : -1);
            if (bounceTime == maxBounceTime) {
                yd -= gravity;
            } else {
                yd = -2 * gravity;
            }
            move(xd, yd, zd);
            if (yo == y) {
                // Finished falling
                if (bounceTime-- < 0) {
                    this.remove();
                } else {
                    // Get last sprite
                    setSprite(spriteSet.get(1, 1));
                }
            }
        } else {
            this.remove();
        }
    }
    
    /**
     * Provides the render type, which determines transparency, visibility behind solids, etc. In some ways analogous to
     * render layers.
     */
    @Override
    public ParticleRenderType getRenderType() {
        
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }
    
    /**
     * A provider for the particle. Uses a spite set and exposes a factory.
     */
    @SuppressWarnings("ClassCanBeRecord") // Want sprite set to be private
    public static class MelterPourParticleProvider implements ParticleProvider<PourParticleOption> {
        // A set of particle sprites.
        private final SpriteSet spriteSet;
        
        // The registration function passes a SpriteSet, so we accept that and store it for further use.
        public MelterPourParticleProvider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }
        
        /**
         * The particle factory. Returns a new particle each time this method is called.
         *
         * @param option Instance of ColorParticleOptions. Contains all nonstandard properties that can (and must be)
         *               specified at runtime.
         * @param level  A valid Level reference.
         * @return The particle created.
         */
        @Override @ParametersAreNonnullByDefault
        public Particle createParticle(
                PourParticleOption option,
                ClientLevel level,
                double x, double y, double z, double xSpeed, double ySpeed, double zSpeed
        ) {
            MelterPourParticle particle = new MelterPourParticle(level, x, y, z, xSpeed, ySpeed + 0.01, zSpeed, spriteSet, option.getFluid());
            particle.setAlpha(0.9f);
            
            return particle;
        }
    }
}
