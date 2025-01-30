package ca.nicbo.invadedlandsevents.util;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.nametag.NameTagManager;
import org.bukkit.entity.Player;

public class TabHook {

    public static void setNameTag(Player player, boolean vis) {
        TabPlayer tabPlayer = TabAPI.getInstance().getPlayer(player.getUniqueId());
        NameTagManager tagManager = TabAPI.getInstance().getNameTagManager();
        if(tagManager != null) {
            if(vis) {
                tagManager.showNameTag(tabPlayer);
            } else {
                tagManager.hideNameTag(tabPlayer);
            }
        }
    }
}