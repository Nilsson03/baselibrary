package ru.nilsson03.library.bukkit.animation.particle.impl;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import ru.nilsson03.library.bukkit.animation.particle.ParticleAnimation;
import ru.nilsson03.library.bukkit.animation.particle.ParticleAnimator;

public class SphereAnimation implements ParticleAnimation {

    private final ParticleAnimator particleAnimator;

    public SphereAnimation(ParticleAnimator particleAnimator) {
        this.particleAnimator = particleAnimator;
    }

    @Override
    public void draw(Player target, Location center, Particle particle, double size) {
        int points = 15;
        double increment = Math.PI / points;
        for (double phi = 0; phi <= Math.PI; phi += increment) {
            for (double theta = 0; theta <= 2 * Math.PI; theta += increment) {
                double x = size * Math.cos(theta) * Math.sin(phi);
                double y = size * Math.cos(phi);
                double z = size * Math.sin(theta) * Math.sin(phi);
                particleAnimator.sendParticle(target, particle, center.clone().add(x, y, z));
            }
        }
    }
}
