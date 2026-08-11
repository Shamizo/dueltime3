package cn.valorin.dueltime.listener.chat;

import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.level.LevelManager;
import cn.valorin.dueltime.level.Tier;
import cn.valorin.dueltime.yaml.configuration.CfgManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    @EventHandler
    public void showTierTitleWhileChatting(AsyncPlayerChatEvent e) {
        CfgManager cfgManager = DuelTimePlugin.getInstance().getCfgManager();
        if (!cfgManager.isTierTitleShowedInChatBoxEnabled()) {
            return;
        }
        Player player = e.getPlayer();
        LevelManager levelManager = DuelTimePlugin.getInstance().getLevelManager();
        Tier tier = levelManager.getTier(player.getName());
        String title = tier != null ? tier.getTitle() : "";
        e.setFormat(cfgManager.getTierTitleShowedInChatBoxFormat().replace("%v", title) + e.getFormat());
    }
}