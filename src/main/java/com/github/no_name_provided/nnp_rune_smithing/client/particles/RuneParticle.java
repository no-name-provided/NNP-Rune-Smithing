package com.github.no_name_provided.nnp_rune_smithing.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.world.phys.Vec3;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * The base class for every particle emitted by a rune in the world.
 */
public class RuneParticle extends TextureSheetParticle {
    private final Vec3 center;
    
    public RuneParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet spriteSet) {
        super(level, x, y, z);
        this.scale(0.25f);
        this.setSize(0.1f, 0.1f);
        
        this.center = new Vec3(x, y, z);
        
        this.gravity = 3.0E-6f;
        this.xd = xSpeed / 5;
        this.yd = ySpeed / 5;
        this.zd = zSpeed / 5;
        
        this.friction = 3.0f;
        
        // Set the initial sprite here since ticking is not guaranteed to set the sprite before the render method is called.
        this.pickSprite(spriteSet);
        
        this.lifetime = this.random.nextInt(50) + 20;
    }
    
    /**
     * Runs every tick.
     */
    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ < this.lifetime && !(this.alpha <= 0.0F)) {
            this.xd += this.random.nextFloat() / 500.0f * (float) (this.random.nextBoolean() ? 1 : -1);
            this.zd += this.random.nextFloat() / 500.0f * (float) (this.random.nextBoolean() ? 1 : -1);
            this.yd *= this.random.nextFloat() / 500.0f * (float) (this.random.nextBoolean() ? 1 : -1);
            this.move(this.xd, this.yd, this.zd);
            if (!center.closerThan(new Vec3(this.x, this.y, this.z), 0.5)) {
                this.xd = -0.5f * this.xd;
                this.yd = -0.5f * this.yd;
                this.zd = -0.5f * this.zd;
            }
            if (this.age >= this.lifetime - 60 && this.alpha > 0.01f) {
                this.alpha -= 0.015f;
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
    public static class RuneParticleProvider implements ParticleProvider<ColorParticleOption> {
        // A set of particle sprites.
        private final SpriteSet spriteSet;
        
        // The registration function passes a SpriteSet, so we accept that and store it for further use.
        public RuneParticleProvider(SpriteSet spriteSet) {
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
                ColorParticleOption option,
                ClientLevel level,
                double x, double y, double z, double xSpeed, double ySpeed, double zSpeed
        ) {
            RuneParticle particle = new RuneParticle(level, x, y, z, xSpeed, ySpeed + 0.01, zSpeed, spriteSet);
            particle.setAlpha(0.9F);
            particle.setColor(option.getRed(), option.getGreen(), option.getBlue());
            return particle;
        }
        
    }
}
