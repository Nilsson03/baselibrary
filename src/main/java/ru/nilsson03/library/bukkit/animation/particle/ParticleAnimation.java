package ru.nilsson03.library.bukkit.animation.particle;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public interface ParticleAnimation {

    void draw(Player target, Location center, Particle particle, double width);
}
