package cn.valorin.dueltime.listener.wait;

import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.arena.ArenaManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class WaitingListener implements Listener {

    /*
    在玩家离开服务器后，退出等待状态
     */
    @EventHandler
    public void onPlayerLeaveServer(PlayerQuitEvent event) {
        ArenaManager arenaManager = DuelTimePlugin.getInstance().getArenaManager();
        Player player = event.getPlayer();
        if (arenaManager.getWaitingFor(player) != null) arenaManager.removeWaitingPlayer(player);
    }
}
