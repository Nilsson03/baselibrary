package ru.nilsson03.library.nms;

import org.bukkit.entity.Player;
import ru.nilsson03.library.bukkit.bar.ActionBarHandler;
import ru.nilsson03.library.bukkit.util.ServerVersionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class NMSActionBar  implements ActionBarHandler {
    private static final String NMS_VERSION;
    private static Method PLAYER_CONNECTION_SEND_PACKET;
    private static Method CRAFT_PLAYER_GET_HANDLE;
    private static Class<?> PACKET_CLASS;
    private static Class<?> CHAT_COMPONENT_CLASS;
    private static Class<?> CHAT_SERIALIZER_CLASS;

    static {
        NMS_VERSION = ServerVersionUtils.NMS_VERSION;
        initializeReflection();
    }

    private static void initializeReflection() {
        try {
            String craftPlayerPath = "org.bukkit.craftbukkit." + NMS_VERSION + ".entity.CraftPlayer";
            Class<?> craftPlayerClass = Class.forName(craftPlayerPath);

            CRAFT_PLAYER_GET_HANDLE = craftPlayerClass.getMethod("getHandle");

            String nmsPlayerPath = "net.minecraft.server." + NMS_VERSION + ".EntityPlayer";
            Class<?> nmsPlayerClass = Class.forName(nmsPlayerPath);

            PLAYER_CONNECTION_SEND_PACKET = nmsPlayerClass
                    .getField("playerConnection")
                    .getType()
                    .getMethod("sendPacket", getPacketClass());

            CHAT_COMPONENT_CLASS = Class.forName("net.minecraft.server." + NMS_VERSION + ".IChatBaseComponent");
            CHAT_SERIALIZER_CLASS = Class.forName("net.minecraft.server." + NMS_VERSION + ".IChatBaseComponent$ChatSerializer");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize NMS reflection", e);
        }
    }

    private static Class<?> getPacketClass() {
        if (PACKET_CLASS == null) {
            try {
                PACKET_CLASS = Class.forName("net.minecraft.server." + NMS_VERSION + ".PacketPlayOutChat");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return PACKET_CLASS;
    }

    @Override
    public void send(Player player, String message) {
        try {
            Object craftPlayer = player.getClass().cast(player);
            Object nmsPlayer = CRAFT_PLAYER_GET_HANDLE.invoke(craftPlayer);

            Object component = CHAT_SERIALIZER_CLASS
                    .getMethod("a", String.class)
                    .invoke(null, "{\"text\":\"" + message + "\"}");

            Constructor<?> packetConstructor = getPacketClass()
                    .getConstructor(CHAT_COMPONENT_CLASS, byte.class);

            Object packet = packetConstructor.newInstance(component, (byte) 2);

            Object playerConnection = nmsPlayer.getClass()
                    .getField("playerConnection")
                    .get(nmsPlayer);

            PLAYER_CONNECTION_SEND_PACKET.invoke(playerConnection, packet);
        } catch (Exception e) {
            // Fallback - отправляем обычное сообщение
            player.sendMessage(message);
        }
    }
}
