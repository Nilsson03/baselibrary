package ru.nilsson03.library.bukkit.animation.particle;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AnimationTask extends BukkitRunnable {
    private final Player targetPlayer;
    private final Location center;
    private final Particle particle;
    private final double size;
    private final ParticleAnimation animation;
    private final long duration;
    private final boolean singleTarget;
    private int ticksPassed = 0;

    public AnimationTask(Player targetPlayer, Location center, Particle particle,
                        double size, ParticleAnimation animation, long duration, boolean singleTarget) {
        this.targetPlayer = targetPlayer;
        this.center = center;
        this.particle = particle;
        this.size = size;
        this.animation = animation;
        this.duration = duration;
        this.singleTarget = singleTarget;
    }

    @Override
    public void run() {
        if (ticksPassed++ >= duration) {
            this.cancel();
            return;
        }

        if (singleTarget) {
            animation.draw(targetPlayer, center, particle, size);
        } else {
            for (Player player : center.getWorld().getPlayers()) {
                if (player.getLocation().distance(center) <= 30) {
                    animation.draw(player, center, particle, size);
                }
            }
        }
    }
}
