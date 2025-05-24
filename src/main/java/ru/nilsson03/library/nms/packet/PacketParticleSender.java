package ru.nilsson03.library.nms.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedParticle;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import ru.nilsson03.library.BaseLibrary;
import ru.nilsson03.library.bukkit.integration.DependencyException;
import ru.nilsson03.library.bukkit.integration.PluginDependency;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

import java.util.Objects;

public class PacketParticleSender {

    private static final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    public PacketParticleSender() {

    }

    @PluginDependency(name = "ProtocolLib", minVersion = "5.3.0", skipIfUnavailable = true)
    public void sendParticle(Player player, Particle particle, int count, Location loc) throws DependencyException {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(particle, "Particle cannot be null");
        Objects.requireNonNull(loc, "Location cannot be null");
        if (count <= 0) {
            throw new IllegalArgumentException("Count of particles must be greater than 0");
        }

        try {
            // Создание и настройка пакета
            PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.WORLD_PARTICLES);

            WrappedParticle<?> wrappedParticle = WrappedParticle.create(particle, null);
            packet.getNewParticles().write(0, wrappedParticle);
            packet.getDoubles()
                    .write(0, loc.getX())
                    .write(1, loc.getY())
                    .write(2, loc.getZ());
            packet.getFloat()
                    .write(0, 0.1f)  // Offset X
                    .write(1, 0.1f)  // Offset Y
                    .write(2, 0.1f); // Offset Z
            packet.getIntegers().write(0, count);

            protocolManager.sendServerPacket(player, packet);

        } catch (Exception e) {
            ConsoleLogger.error(BaseLibrary.getInstance(),
                    "Failed to send particle to %s: %s",
                    player.getName(), e.getMessage());
            throw new DependencyException("Failed to send particle packet", e);
        }
    }
}
