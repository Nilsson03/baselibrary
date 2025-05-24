package ru.nilsson03.library.bukkit.animation.particle.impl;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import ru.nilsson03.library.bukkit.animation.particle.ParticleAnimation;
import ru.nilsson03.library.bukkit.animation.particle.ParticleAnimator;

public class CircleAnimation implements ParticleAnimation {

    private final ParticleAnimator particleAnimator;

    public CircleAnimation(ParticleAnimator particleAnimator) {
        this.particleAnimator = particleAnimator;
    }

    @Override
    public void draw(Player target, Location center, Particle particle, double size) {
        int points = 30;
        double increment = (2 * Math.PI) / points;
        for (int i = 0; i < points; i++) {
            double angle = i * increment;
            double x = size * Math.cos(angle);
            double z = size * Math.sin(angle);
            particleAnimator.sendParticle(target, particle, center.clone().add(x, 0, z));
        }
    }
}
