package ru.nilsson03.library.bukkit.integration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface PluginDependency {
    String name();
    String minVersion() default "1.0.0";
    boolean skipIfUnavailable() default false; // Новый параметр
}
