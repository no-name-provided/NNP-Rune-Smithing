package com.github.no_name_provided.nnp_rune_smithing.client.particles;

import com.github.no_name_provided.nnp_rune_smithing.client.particles.options.ColoredParticleType;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;

import javax.annotation.ParametersAreNonnullByDefault;

public class RuneParticle extends TextureSheetParticle {
    
    public RuneParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet spriteSet) {
        super(level, x, y, z);
        this.scale(2.0f);
        this.setSize(0.1f, 0.1f);
        
        this.gravity = 3.0E-6f;
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        
        // Set the initial sprite here since ticking is not guaranteed to set the sprite before the render method is called.
        this.setSpriteFromAge(spriteSet);
        
        this.lifetime = this.random.nextInt(50) + 80;
        
    }
    
    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ < this.lifetime && !(this.alpha <= 0.0F)) {
            this.xd += this.random.nextFloat() / 5000.0f * (float) (this.random.nextBoolean() ? 1 : -1);
            this.zd += this.random.nextFloat() / 5000.0f * (float) (this.random.nextBoolean() ? 1 : -1);
            this.yd *= this.random.nextFloat() / 5000.0f * (float) (this.random.nextBoolean() ? 1 : -1);
            this.move(this.xd, this.yd, this.zd);
            if (this.age >= this.lifetime - 60 && this.alpha > 0.01f) {
                this.alpha -= 0.015f;
            }
        } else {
            this.remove();
        }
    }
    
    
    @Override
    public ParticleRenderType getRenderType() {
        
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }
    
    
    public static class RuneParticleProvider implements ParticleProvider<ColoredParticleType> {
        // A set of particle sprites.
        private final SpriteSet spriteSet;
        
        // The registration function passes a SpriteSet, so we accept that and store it for further use.
        public RuneParticleProvider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }
        
        // This is where the magic happens. We return a new particle each time this method is called!
        // The type of the first parameter matches the generic type passed to the super interface.
        @Override @ParametersAreNonnullByDefault
        public Particle createParticle(
                ColoredParticleType type,
                ClientLevel level,
                double x, double y, double z, double xSpeed, double ySpeed, double zSpeed
        ) {
            RuneParticle particle = new RuneParticle(level, x, y, z, xSpeed, ySpeed + 0.01, zSpeed, spriteSet);
            particle.setAlpha(0.9F);
            particle.setColor(type.getRed(), type.getGreen(), type.getBlue());
            return particle;
        }
        
    }
}
