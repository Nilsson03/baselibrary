package ru.nilsson03.library.bukkit.animation.particle.impl;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import ru.nilsson03.library.bukkit.animation.particle.ParticleAnimation;
import ru.nilsson03.library.bukkit.animation.particle.ParticleAnimator;

public class SquareAnimation implements ParticleAnimation {

    private final ParticleAnimator particleAnimator;

    public SquareAnimation(ParticleAnimator particleAnimator) {
        this.particleAnimator = particleAnimator;
    }

    @Override
    public void draw(Player target, Location center, Particle particle, double size) {
        double halfSize = size / 2;
        for (double i = -halfSize; i <= halfSize; i += 0.2) {
            particleAnimator.sendParticle(target, particle, center.clone().add(i, 0, -halfSize));
            particleAnimator.sendParticle(target, particle, center.clone().add(i, 0, halfSize));
            particleAnimator.sendParticle(target, particle, center.clone().add(-halfSize, 0, i));
            particleAnimator.sendParticle(target, particle, center.clone().add(halfSize, 0, i));
        }
    }
}
