package ru.nilsson03.library.bukkit.animation.particle;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.nms.packet.PacketParticleSender;

public interface ParticleAnimation {

    void draw(Player target, Location center, Particle particle, double width);
}
