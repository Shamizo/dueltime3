package cn.valorin.dueltime.listener.cache;

import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.arena.base.BaseArena;
import cn.valorin.dueltime.cache.LocationCache;
import cn.valorin.dueltime.yaml.message.Msg;
import cn.valorin.dueltime.yaml.message.MsgBuilder;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinServerForArenaCheckListener implements Listener {
    @EventHandler
    public void onJoinServer(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // 检查玩家是否在竞技场中
        checkAndRemovePlayerFromArena(player);
    }
    
    private void checkAndRemovePlayerFromArena(Player player) {
        if (player == null || player.getName() == null) {
            return;
        }
        
        // 检查玩家是否在某个竞技场中
        BaseArena arena = DuelTimePlugin.getInstance().getArenaManager().getOf(player);
        if (arena != null) {
            // 玩家在竞技场中，将其移除并传送到大厅或原来的位置
            try {
                // 获取玩家原来的位置作为备选
                Location originalLocation = null;
                if (arena.getGamerData(player.getName()) != null && arena.getGamerData(player.getName()) instanceof cn.valorin.dueltime.arena.gamer.ClassicGamerData) {
                    originalLocation = ((cn.valorin.dueltime.arena.gamer.ClassicGamerData) arena.getGamerData(player.getName())).getOriginalLocation();
                }
                
                // 结束该玩家在竞技场中的比赛
                arena.end();
                
                // 确保玩家被传送到安全位置
                Location lobby = DuelTimePlugin.getInstance().getCacheManager().getLocationCache().get(LocationCache.InternalType.LOBBY.getId());
                Location destination = lobby != null ? lobby : originalLocation;
                
                if (destination != null) {
                    cn.valorin.dueltime.util.UtilSync.safeTeleport(player, destination);
                }
                
                // 通知玩家
                MsgBuilder.send(Msg.ARENA_TYPE_CLASSIC_RECONNECT_AUTO_LEAVE_ARENA, player, false);
                
            } catch (Exception e) {
                // 如果结束竞技场时出现异常，至少要确保玩家被移出竞技场
                DuelTimePlugin.getInstance().getArenaManager().removeGamerFromMap(player.getName());
                
                // 尝试将玩家传送到大厅或其原来的位置
                try {
                    Location lobby = DuelTimePlugin.getInstance().getCacheManager().getLocationCache().get(LocationCache.InternalType.LOBBY.getId());
                    Location destination = lobby;
                    
                    if (destination == null) {
                        // 如果没有设置大厅，尝试使用玩家的重生点
                        org.bukkit.World world = player.getWorld();
                        destination = player.getBedSpawnLocation();
                        if (destination == null) {
                            // 如果没有床的重生点，使用世界的出生点
                            destination = world.getSpawnLocation();
                        }
                    }
                    
                    if (destination != null) {
                        cn.valorin.dueltime.util.UtilSync.safeTeleport(player, destination);
                    }
                } catch (Exception ex) {
                    // 如果传送失败，至少要确保玩家状态被清理
                    ex.printStackTrace();
                }
            }
        }
        
        // 检查玩家是否在观战某个竞技场
        BaseArena spectateArena = DuelTimePlugin.getInstance().getArenaManager().getSpectate(player);
        if (spectateArena != null) {
            try {
                // 将玩家从观战列表中移除
                DuelTimePlugin.getInstance().getArenaManager().removeSpectator(player);
                
                // 通知玩家
                MsgBuilder.send(Msg.ARENA_TYPE_CLASSIC_RECONNECT_AUTO_LEAVE_SPECTATE, player, false);
                
            } catch (Exception e) {
                // 如果移除观战时出现异常，至少要确保玩家被移出观战列表
                DuelTimePlugin.getInstance().getArenaManager().removeSpectator(player);
            }
        }
        
        // 检查玩家是否在等待队列中
        BaseArena waitingArena = DuelTimePlugin.getInstance().getArenaManager().getWaitingFor(player);
        if (waitingArena != null) {
            try {
                // 将玩家从等待队列中移除
                DuelTimePlugin.getInstance().getArenaManager().removeWaitingPlayer(player);
                
                // 通知玩家
                MsgBuilder.send(Msg.ARENA_TYPE_CLASSIC_RECONNECT_AUTO_LEAVE_QUEUE, player, false);
                
            } catch (Exception e) {
                // 如果移除等待队列时出现异常，至少要确保玩家被移出等待队列
                DuelTimePlugin.getInstance().getArenaManager().removeWaitingPlayer(player);
            }
        }
    }
}