package ru.nilsson03.library.bukkit.bar;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.bukkit.entity.Player;

public class UniversalActionBar {
    private static final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    public static void send(Player player, String message) {
        try {
            PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.TITLE);
            packet.getTitleActions().write(0, EnumWrappers.TitleAction.ACTIONBAR);
            packet.getChatComponents().write(0, WrappedChatComponent.fromText(message));
            protocolManager.sendServerPacket(player, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
