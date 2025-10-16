package ru.nilsson03.library.bukkit.animation.particle;

import org.bukkit.*;
import org.bukkit.entity.Player;
import ru.nilsson03.library.BaseLibrary;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.animation.particle.impl.CircleAnimation;
import ru.nilsson03.library.bukkit.animation.particle.impl.RectangleAnimation;
import ru.nilsson03.library.bukkit.animation.particle.impl.SphereAnimation;
import ru.nilsson03.library.bukkit.animation.particle.impl.SquareAnimation;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;
import ru.nilsson03.library.nms.packet.PacketParticleSender;

import java.util.HashMap;
import java.util.Map;

public class ParticleAnimator {

    private final NPlugin plugin;
    private final PacketParticleSender particleSender;

    private final Map<String, ParticleAnimation> animations = new HashMap<>();

    public ParticleAnimator(NPlugin plugin) {
        this.plugin = plugin;
        this.particleSender = new PacketParticleSender();
        registerDefaultAnimations();
    }

    public void sendParticle(Player player, Particle particle, Location loc) {
        try {
            particleSender.sendParticle(player, particle, 1, loc);
        } catch (Exception e) {
            ConsoleLogger.error(plugin, "Error sending particle, message %s", e.getMessage());
        }
    }

    private void registerDefaultAnimations() {
        animations.put("square", new SquareAnimation(this));
        animations.put("rectangle", new RectangleAnimation(this));
        animations.put("circle", new CircleAnimation(this));
        animations.put("sphere", new SphereAnimation(this));
    }

    public void playAnimation(Player player, String animationName, Location center,
                              Particle particle, int durationSeconds, double size) {
        ParticleAnimation animation = animations.get(animationName.toLowerCase());
        if (animation != null) {
            long durationInTicks = (durationSeconds * 1000L) * 20;
            new AnimationTask(player, center, particle, size, animation, durationInTicks, true)
                    .runTaskTimer(BaseLibrary.getInstance(), 0L, 20L);
        }
    }

    public void playAnimation(String animationName, Location center,
                              Particle particle, int durationSeconds, double size) {
        ParticleAnimation animation = animations.get(animationName.toLowerCase());
        if (animation != null) {
            long durationInTicks = (durationSeconds * 1000L) * 20;
            new AnimationTask(null, center, particle, size, animation, durationInTicks, false)
                    .runTaskTimer(BaseLibrary.getInstance(), 0L, 20L);
        }
    }

    public void registerAnimation(String name, ParticleAnimation animation) {
        animations.put(name.toLowerCase(), animation);
    }
}
