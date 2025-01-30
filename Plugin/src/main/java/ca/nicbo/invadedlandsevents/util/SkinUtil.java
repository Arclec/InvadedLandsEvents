package ca.nicbo.invadedlandsevents.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;

public class SkinUtil {

    private static void sendUpdatePackets(Player receiver, Player updatedPlayer) {
        CraftPlayer craftReceiver = (CraftPlayer) receiver;
        EntityPlayer entityReceiver = craftReceiver.getHandle();

        PacketPlayOutPlayerInfo removePacket = new PacketPlayOutPlayerInfo(
                PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER,
                ((CraftPlayer) updatedPlayer).getHandle()
        );
        entityReceiver.playerConnection.sendPacket(removePacket);

        PacketPlayOutPlayerInfo addPacket = new PacketPlayOutPlayerInfo(
                PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER,
                ((CraftPlayer) updatedPlayer).getHandle()
        );
        entityReceiver.playerConnection.sendPacket(addPacket);

        receiver.hidePlayer(updatedPlayer);
        receiver.showPlayer(updatedPlayer);
    }

    public static boolean setSkinFromUUID(Player player, UUID uuid) {
        try {
            URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
            InputStreamReader reader = new InputStreamReader(url.openStream());
            JsonObject properties = new JsonParser().parse(reader).getAsJsonObject()
                    .get("properties").getAsJsonArray().get(0).getAsJsonObject();
            String texture = properties.get("value").getAsString();
            String signature = properties.get("signature").getAsString();


            CraftPlayer craftPlayer = (CraftPlayer) player;
            EntityPlayer entityPlayer = craftPlayer.getHandle();
            GameProfile gameProfile = entityPlayer.getProfile();
            gameProfile.getProperties().removeAll("textures");
            gameProfile.getProperties().put("textures", new Property("textures", texture, signature));

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                sendUpdatePackets(onlinePlayer, player);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
