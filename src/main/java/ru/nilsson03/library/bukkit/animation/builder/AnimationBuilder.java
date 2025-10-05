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

    // Для ParticleAnimator
    private Particle particle;
    private double size;
    private Player targetPlayer;

    private NPlugin plugin;

    public AnimationBuilder(NPlugin plugin) {
        this.plugin = plugin;
    }

    public static AnimationBuilder create(NPlugin plugin) {
        return new AnimationBuilder(plugin);
    }

    // Настройка частиц
    public AnimationBuilder withParticle(Particle particle, double size) {
        this.particle = particle;
        this.size = size;
        return this;
    }

    // Настройка игрока
    public AnimationBuilder forPlayer(Player player) {
        this.targetPlayer = player;
        return this;
    }

    // Добавление шагов анимации
    public AnimationBuilder addStep(Consumer<AnimationStep> stepConfigurator) {
        AnimationStep step = new AnimationStep();
        stepConfigurator.accept(step);
        steps.add(step);
        return this;
    }

    // Управление потоком
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

    // Запуск анимации
    public UniversalAnimation buildAt(Location location) {
        this.startLocation = location;

        if (particle != null) {
            return new ParticleAnimationImpl();
        } 
        throw new IllegalStateException("Не указан тип анимации (частицы или предмет)");
    }

    // Реализация для частиц
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

    // Класс для конфигурации шага
    public static class AnimationStep {
        private String shape;
        private int duration = 20;

        public AnimationStep shape(String shape) {
            this.shape = shape;
            return this;
        }

        public AnimationStep duration(int ticks) {
            this.duration = ticks;
            return this;
        }
    }
}
