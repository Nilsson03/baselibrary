package ru.nilsson03.library.bukkit.animation.builder;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.animation.particle.ParticleAnimator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AnimationBuilder {
    private final List<AnimationStep> steps = new ArrayList<>();
    private boolean loop = false;
    private int delayBetweenSteps = 0;
    private Consumer<Location> completionHandler;
    private Location startLocation;

    private Particle particle;
    private double size;
    private Player targetPlayer;
    private final NPlugin plugin;

    public AnimationBuilder(NPlugin plugin) {
        this.plugin = plugin;
    }

    public static AnimationBuilder create(NPlugin plugin) {
        return new AnimationBuilder(plugin);
    }

    public AnimationBuilder withParticle(Particle particle, double size) {
        this.particle = particle;
        this.size = size;
        return this;
    }

    public AnimationBuilder forPlayer(Player player) {
        this.targetPlayer = player;
        return this;
    }

    public AnimationBuilder addStep(Consumer<AnimationStep> stepConfigurator) {
        AnimationStep step = new AnimationStep();
        stepConfigurator.accept(step);
        steps.add(step);
        return this;
    }

    public AnimationBuilder loop(boolean loop) {
        this.loop = loop;
        return this;
    }

    public AnimationBuilder withDelay(int ticks) {
        this.delayBetweenSteps = ticks;
        return this;
    }

    public AnimationBuilder onComplete(Consumer<Location> handler) {
        this.completionHandler = handler;
        return this;
    }

    public UniversalAnimation buildAt(Location location) {
        this.startLocation = location;

        if (particle != null) {
            return new ParticleAnimationImpl();
        }
        throw new IllegalStateException("Не указан тип анимации (частицы или предмет)");
    }

    private class ParticleAnimationImpl implements UniversalAnimation {
        private BukkitTask task;
        private int currentStep = 0;

        @Override
        public void start() {
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (currentStep >= steps.size()) {
                        if (loop) {
                            currentStep = 0;
                        } else {
                            cancel();
                            if (completionHandler != null) {
                                completionHandler.accept(startLocation);
                            }
                            return;
                        }
                    }

                    AnimationStep step = steps.get(currentStep);
                    executeParticleStep(step);
                    currentStep++;
                }
            }.runTaskTimer(Bukkit.getPluginManager().getPlugins()[0], 0, delayBetweenSteps);
        }

        private void executeParticleStep(AnimationStep step) {
            ParticleAnimator animator = new ParticleAnimator(plugin);

            if (step.shape != null) {
                if (targetPlayer != null) {
                    animator.playAnimation(targetPlayer, step.shape, startLocation,
                            particle, step.duration, size);
                } else {
                    animator.playAnimation(step.shape, startLocation , particle, step.duration, size);
                }
            }
        }

        @Override
        public void stop() {
            if (task != null) task.cancel();
        }

        @Override
        public boolean isRunning() {
            return task != null && !task.isCancelled();
        }
    }

    public static class AnimationStep {
        private String shape;
        private Rotation rotation;
        private Movement movement;
        private Scale scale;
        private int duration = 20;

        public AnimationStep shape(String shape) {
            this.shape = shape;
            return this;
        }

        public AnimationStep rotate(float yaw, float pitch, float roll) {
            this.rotation = new Rotation(yaw, pitch, roll);
            return this;
        }

        public AnimationStep moveTo(Location target) {
            this.movement = new Movement(target);
            return this;
        }

        public AnimationStep scale(float x, float y, float z) {
            this.scale = new Scale(x, y, z);
            return this;
        }

        public AnimationStep duration(int ticks) {
            this.duration = ticks;
            return this;
        }
    }

    private static class Rotation {
        final float yaw, pitch, roll;

        Rotation(float yaw, float pitch, float roll) {
            this.yaw = yaw;
            this.pitch = pitch;
            this.roll = roll;
        }
    }

    private static class Movement {
        final Location target;

        Movement(Location target) {
            this.target = target;
        }
    }

    private static class Scale {
        final float x, y, z;

        Scale(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}
