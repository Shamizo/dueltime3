package cn.valorin.dueltime.listener.cache;

import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.arena.ArenaManager;
import cn.valorin.dueltime.arena.base.BaseArena;
import cn.valorin.dueltime.yaml.message.Msg;
import cn.valorin.dueltime.yaml.message.MsgBuilder;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashSet;
import java.util.Set;

public class ArenaStateMonitorListener implements Listener {
    
    private static final Set<String> notifiedPlayers = new HashSet<>();
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // 检查玩家是否拥有OP权限，如果有则跳过检查
        if (player.isOp()) {
            return;
        }
        
        // 检查玩家是否在某个竞技场区域内
        checkPlayerInArena(player);
    }
    
    private void checkPlayerInArena(Player player) {
        if (player == null || player.getName() == null) {
            return;
        }
        
        // 如果玩家已经被通知过，跳过检查
        if (notifiedPlayers.contains(player.getName())) {
            return;
        }
        
        // 获取玩家当前位置
        Location currentLocation = player.getLocation();
        
        ArenaManager arenaManager = DuelTimePlugin.getInstance().getArenaManager();
        // 防御：插件重载/启动早期/异步竞态下 arenaManager 可能尚未初始化
        if (arenaManager == null || arenaManager.getMap() == null) {
            return;
        }
        for (BaseArena arena : arenaManager.getMap().values()) {
            // 检查玩家是否在该竞技场的区域内
            if (isPlayerInArenaRegion(currentLocation, arena)) {
                // 检查竞技场是否处于等待状态（即比赛已经结束）
                if (arena.getState() == BaseArena.State.WAITING) {
                    // 检查玩家是否是该竞技场的参与者或观战者
                    if (arenaManager.getOf(player) != null || 
                        arenaManager.getSpectate(player) != null || 
                        arenaManager.getWaitingFor(player) != null) {
                        // 如果是参与者或观战者，说明可能存在状态清理不及时的情况
                        // 直接杀死玩家以防止非法逗留
                        killPlayerForIllegalStay(player);
                        return;
                    }
                    
                    // 玩家在已完成的竞技场区域内且不是任何相关角色，属于非法逗留
                    killPlayerForIllegalStay(player);
                    return;
                }
                // 检查竞技场是否处于进行中状态但没有活动玩家（可能卡住了）
                else if (isArenaStuck(arena)) {
                    // 强制结束这个卡住的竞技场
                    forceEndArena(arena);
                }
            }
        }
    }
    
    private boolean isArenaStuck(BaseArena arena) {
        // 检查竞技场是否处于进行中状态但长时间没有活动玩家
        if (arena.getState() != BaseArena.State.IN_PROGRESS_CLOSED && 
            arena.getState() != BaseArena.State.IN_PROGRESS_OPENED) {
            return false;
        }
        
        // 检查竞技场是否有参赛者
        if (arena.getGamerDataList() != null && !arena.getGamerDataList().isEmpty()) {
            // 检查参赛者是否在线
            for (Object gamerDataObj : arena.getGamerDataList()) {
                try {
                    cn.valorin.dueltime.arena.base.BaseGamerData gamerData = 
                        (cn.valorin.dueltime.arena.base.BaseGamerData) gamerDataObj;
                    org.bukkit.entity.Player player = gamerData.getPlayer();
                    if (player != null && player.isOnline()) {
                        // 至少有一个玩家在线，竞技场不算卡住
                        return false;
                    }
                } catch (Exception e) {
                    // 类型转换异常等，忽略
                }
            }
            // 所有参赛者都不在线，竞技场可能卡住了
            return true;
        }
        
        return false;
    }
    
    private void forceEndArena(BaseArena arena) {
        // 强制结束卡住的竞技场
        try {
            // 调用ArenaManager的end方法来正确清理竞技场
            cn.valorin.dueltime.arena.ArenaManager arenaManager = 
                DuelTimePlugin.getInstance().getArenaManager();
            arenaManager.end(arena.getId());
            
            // 记录日志
            DuelTimePlugin.getInstance().getLogger().info(
                "强制结束了卡住的竞技场: " + arena.getArenaData().getName() + 
                ", ID: " + arena.getId());
        } catch (Exception e) {
            DuelTimePlugin.getInstance().getLogger().warning(
                "强制结束竞技场失败: " + arena.getArenaData().getName() + 
                ", 错误: " + e.getMessage());
        }
    }
    
    private boolean isPlayerInArenaRegion(Location playerLocation, BaseArena arena) {
        // 获取竞技场数据，检查玩家是否在竞技场区域内
        // 这里假设竞技场有边界区域的概念，实际实现可能需要根据具体竞技场类型调整
        
        // 检查玩家是否在竞技场的玩家位置附近（比如在竞技场的玩家位置1或位置2附近）
        try {
            if (arena.getArenaData() instanceof cn.valorin.dueltime.data.pojo.ClassicArenaData) {
                cn.valorin.dueltime.data.pojo.ClassicArenaData classicArenaData = 
                    (cn.valorin.dueltime.data.pojo.ClassicArenaData) arena.getArenaData();
                
                // 检查玩家是否在竞技场的两个玩家位置之一附近（比如距离小于10格）
                Location loc1 = classicArenaData.getPlayerLocation1();
                Location loc2 = classicArenaData.getPlayerLocation2();
                
                if (loc1 != null && playerLocation.getWorld().getName().equals(loc1.getWorld().getName()) &&
                    playerLocation.distanceSquared(loc1) < 100) { // 100 = 10^2
                    return true;
                }
                
                if (loc2 != null && playerLocation.getWorld().getName().equals(loc2.getWorld().getName()) &&
                    playerLocation.distanceSquared(loc2) < 100) { // 100 = 10^2
                    return true;
                }
            }
        } catch (Exception e) {
            // 如果无法访问竞技场数据，跳过此竞技场的检查
        }
        
        return false;
    }
    
    private void killPlayerForIllegalStay(Player player) {
        String playerName = player.getName();
        
        // 标记玩家已被通知，防止重复操作
        notifiedPlayers.add(playerName);
        
        // 通知玩家
        MsgBuilder.send(Msg.ARENA_STATE_MONITOR_KILL_ILLEGAL_STAY, player, false);
        
        // 延迟杀死玩家，确保消息已发送
        cn.valorin.dueltime.util.SchedulerUtil.runTaskLater(() -> {
            // 使用更隐蔽的方式杀死玩家，避免显示系统死亡消息
            player.damage(1000.0); // 造成大量伤害直接杀死
        }, 5L); // 延迟0.25秒
        
        // 30秒后清除标记，允许再次检测（如果玩家复活并回到竞技场）
        cn.valorin.dueltime.util.SchedulerUtil.runTaskLater(() -> {
            notifiedPlayers.remove(playerName);
        }, 600L); // 30秒后清除标记
    }
}