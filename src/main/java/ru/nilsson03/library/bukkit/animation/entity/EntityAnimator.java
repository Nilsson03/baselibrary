package ru.nilsson03.library.bukkit.animation.entity;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class EntityAnimator {
    private static final boolean IS_NEW_VERSION = isNewVersion();

    public static Entity createItemEntity(Location loc, ItemStack item) {
        if (IS_NEW_VERSION) {
            // Для версий 1.19+ (Display entities)
            return createItemDisplay(loc, item);
        } else {
            // Для старых версий (ArmorStand)
            return createArmorStand(loc, item);
        }
    }

    private static Entity createItemDisplay(Location loc, ItemStack item) {
        ItemDisplay display = (ItemDisplay) loc.getWorld().spawnEntity(loc, EntityType.ITEM_DISPLAY);
        display.setItemStack(item);

        // Настройки трансформации
        Transformation transformation = new Transformation(
                new Vector3f(), // translation
                new AxisAngle4f(0, 0, 0, 1), // left rotation
                new Vector3f(1, 1, 1), // scale
                new AxisAngle4f(0, 0, 0, 1) // right rotation
        );

        display.setTransformation(transformation);
        display.setInterpolationDuration(5);
        display.setInterpolationDelay(0);

        return display;
    }

    private static Entity createArmorStand(Location loc, ItemStack item) {
        ArmorStand armorStand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setInvulnerable(true);
        armorStand.setSmall(true);
        armorStand.setMarker(true);
        armorStand.getEquipment().setHelmet(item);

        return armorStand;
    }

    public static void animateRotation(Entity entity, float yaw, float pitch, float roll, int durationTicks) {
        if (IS_NEW_VERSION && entity instanceof ItemDisplay) {
            animateDisplayRotation((ItemDisplay) entity, yaw, pitch, roll, durationTicks);
        } else if (entity instanceof ArmorStand) {
            animateArmorStandRotation((ArmorStand) entity, yaw, pitch, roll);
        }
    }

    private static void animateDisplayRotation(ItemDisplay display, float yaw, float pitch, float roll, int duration) {
        Transformation oldTransformation = display.getTransformation();

        Quaternionf rotation = new Quaternionf().rotateXYZ(roll, pitch, yaw);

        Transformation newTransformation = new Transformation(
                oldTransformation.getTranslation(),
                rotation,
                oldTransformation.getScale(),
                oldTransformation.getRightRotation()
        );

        display.setInterpolationDuration(duration);
        display.setTransformation(newTransformation);
    }

    private static void animateArmorStandRotation(ArmorStand armorStand, float yaw, float pitch, float roll) {
        Location loc = armorStand.getLocation();
        loc.setYaw(yaw * (180 / (float) Math.PI));
        armorStand.teleport(loc);

        armorStand.setHeadPose(new EulerAngle(pitch, 0, 0));
        armorStand.setBodyPose(new EulerAngle(0, 0, roll));
    }

    public static void animateMovement(Entity entity, Location target, int durationTicks) {
        if (IS_NEW_VERSION && entity instanceof ItemDisplay) {
            animateDisplayMovement((ItemDisplay) entity, target, durationTicks);
        } else {
            entity.teleport(target);
        }
    }

    private static void animateDisplayMovement(ItemDisplay display, Location target, int duration) {
        Transformation oldTransformation = display.getTransformation();

        Vector3f newTranslation = new Vector3f(
                (float) (target.getX() - display.getLocation().getX()),
                (float) (target.getY() - display.getLocation().getY()),
                (float) (target.getZ() - display.getLocation().getZ())
        );

        Transformation newTransformation = new Transformation(
                newTranslation,
                oldTransformation.getLeftRotation(),
                oldTransformation.getScale(),
                oldTransformation.getRightRotation()
        );

        display.setInterpolationDuration(duration);
        display.setTransformation(newTransformation);
    }

    public static void animateScale(Entity entity, float scaleX, float scaleY, float scaleZ, int durationTicks) {
        if (IS_NEW_VERSION && entity instanceof ItemDisplay) {
            animateDisplayScale((ItemDisplay) entity, scaleX, scaleY, scaleZ, durationTicks);
        }
        // Для ArmorStand масштабирование не поддерживается
    }

    private static void animateDisplayScale(ItemDisplay display, float x, float y, float z, int duration) {
        Transformation oldTransformation = display.getTransformation();

        Transformation newTransformation = new Transformation(
                oldTransformation.getTranslation(),
                oldTransformation.getLeftRotation(),
                new Vector3f(x, y, z),
                oldTransformation.getRightRotation()
        );

        display.setInterpolationDuration(duration);
        display.setTransformation(newTransformation);
    }

    private static boolean isNewVersion() {
        try {
            Class.forName("org.bukkit.entity.Display");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
