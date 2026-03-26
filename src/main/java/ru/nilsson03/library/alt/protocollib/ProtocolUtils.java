package ru.nilsson03.library.alt.protocollib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

public class ProtocolUtils {

    private static final String VERSION;
    private static final Map<Particle, Object> PARTICLE_CACHE = new HashMap<>();
    
    private static Class<?> packetPlayOutWorldParticlesClass;
    private static Class<?> particleParamClass;
    private static Class<?> craftPlayerClass;
    private static Class<?> craftParticleClass;
    private static Class<?> entityPlayerClass;
    private static Class<?> playerConnectionClass;
    private static Class<?> packetClass;
    
    private static Constructor<?> packetConstructor;
    private static Method getHandleMethod;
    private static Method sendPacketMethod;
    private static Method toNMSMethod;
    private static Field playerConnectionField;
    
    static {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        VERSION = packageName.substring(packageName.lastIndexOf('.') + 1);
        
        try {
            initializeClasses();
            initializeReflection();
        } catch (Exception e) {
            ConsoleLogger.error("baselibrary", "Failed to initialize NMS reflection: %s", e.getMessage());
        }
    }
    
    private static void initializeClasses() throws ClassNotFoundException {
        packetPlayOutWorldParticlesClass = getNMSClass("PacketPlayOutWorldParticles");
        particleParamClass = getNMSClass("ParticleParam");
        packetClass = getNMSClass("Packet");
        craftPlayerClass = getCraftBukkitClass("entity.CraftPlayer");
        craftParticleClass = getCraftBukkitClass("CraftParticle");
        entityPlayerClass = getNMSClass("EntityPlayer");
        playerConnectionClass = getNMSClass("PlayerConnection");
    }
    
    private static void initializeReflection() throws Exception {
        packetConstructor = packetPlayOutWorldParticlesClass.getConstructor(
            particleParamClass,
            boolean.class,
            double.class, double.class, double.class,
            float.class, float.class, float.class,
            float.class,
            int.class
        );
        
        getHandleMethod = craftPlayerClass.getMethod("getHandle");
        playerConnectionField = entityPlayerClass.getField("playerConnection");
        sendPacketMethod = playerConnectionClass.getMethod("sendPacket", packetClass);
        
        try {
            toNMSMethod = craftParticleClass.getMethod("toNMS", Particle.class, Object.class);
        } catch (NoSuchMethodException e) {
            toNMSMethod = craftParticleClass.getMethod("toNMS", Particle.class);
        }
    }
    
    private static Class<?> getNMSClass(String className) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + VERSION + "." + className);
    }
    
    private static Class<?> getCraftBukkitClass(String className) throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit." + VERSION + "." + className);
    }

    public static void sendParticle(Player player, Particle particle, int count, Location location) {
        sendParticle(player, particle, count, location, null);
    }
    
    public static void sendParticle(Player player, Particle particle, int count, Location location, Particle.DustOptions dustOptions) {
        try {
            Object nmsParticle = getParticleParam(particle, dustOptions);
            
            Object packet = packetConstructor.newInstance(
                nmsParticle,
                true,
                location.getX(), location.getY(), location.getZ(),
                0.0f, 0.0f, 0.0f,
                0.0f,
                count
            );
            
            sendPacket(player, packet);
        } catch (Exception e) {
            ConsoleLogger.error("baselibrary", "Failed to send particle to player %s: %s", player.getName(), e.getMessage());
        }
    }
    
    private static Object getParticleParam(Particle particle) throws Exception {
        return getParticleParam(particle, null);
    }
    
    private static Object getParticleParam(Particle particle, Particle.DustOptions dustOptions) throws Exception {
        if (dustOptions != null) {
            return toNMSMethod.invoke(null, particle, dustOptions);
        }
        
        Object cached = PARTICLE_CACHE.get(particle);
        if (cached != null) {
            return cached;
        }
        
        Object result = toNMSMethod.invoke(null, particle);
        PARTICLE_CACHE.put(particle, result);
        return result;
    }
    
    private static void sendPacket(Player player, Object packet) throws Exception {
        Object nmsPlayer = getHandleMethod.invoke(player);
        Object connection = playerConnectionField.get(nmsPlayer);
        sendPacketMethod.invoke(connection, packet);
    }
}
