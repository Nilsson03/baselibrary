package ru.nilsson03.library.bukkit.animation.particle.impl;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import ru.nilsson03.library.bukkit.animation.particle.ParticleAnimation;
import ru.nilsson03.library.bukkit.animation.particle.ParticleAnimator;

public class RectangleAnimation implements ParticleAnimation {

    private final ParticleAnimator particleAnimator;

    public RectangleAnimation(ParticleAnimator particleAnimator) {
        this.particleAnimator = particleAnimator;
    }

    @Override
    public void draw(Player target, Location center, Particle particle, double width) {
        double height = width * 0.5;
        for (double i = -width/2; i <= width/2; i += 0.2) {
            particleAnimator.sendParticle(target, particle, center.clone().add(i, 0, -height/2));
            particleAnimator.sendParticle(target, particle, center.clone().add(i, 0, height/2));
        }
        for (double i = -height/2; i <= height/2; i += 0.2) {
            particleAnimator.sendParticle(target, particle, center.clone().add(-width/2, 0, i));
            particleAnimator.sendParticle(target, particle, center.clone().add(width/2, 0, i));
        }
    }
}
