package cn.valorin.dueltime.listener.arena;

import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.arena.ArenaManager;
import cn.valorin.dueltime.arena.ClassicArena;
import cn.valorin.dueltime.arena.base.BaseArena;
import cn.valorin.dueltime.arena.base.BaseGamerData;
import cn.valorin.dueltime.arena.gamer.ClassicGamerData;
import cn.valorin.dueltime.arena.spectator.ClassicSpectatorData;
import cn.valorin.dueltime.arena.type.ArenaType;
import cn.valorin.dueltime.command.sub.CommandPermission;
import cn.valorin.dueltime.data.pojo.ClassicArenaRecordData;
import cn.valorin.dueltime.event.arena.*;
import cn.valorin.dueltime.gui.simple.ItemDetailInventoryHolder;
import cn.valorin.dueltime.util.UtilGeometry;
import cn.valorin.dueltime.util.SchedulerUtil;
import cn.valorin.dueltime.util.UtilSync;
import cn.valorin.dueltime.yaml.message.Msg;
import cn.valorin.dueltime.yaml.message.MsgBuilder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.valorin.dueltime.arena.type.ArenaType.FunctionInternalType.*;

public class ClassicArenaListener implements Listener {
    /*
    经典模式玩家伤害事件：
    更新打击数据
    更新观战附加功能中的实时血量
    阻止倒计时期间的伤害
     */
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!((event.getEntity()) instanceof Player)) {
            return;
        }
        Entity damagerEntity = event.getDamager();
        Player attacker;
        if (damagerEntity instanceof Player) {
            attacker = (Player) damagerEntity;
        } else if (damagerEntity instanceof Projectile) {
            ProjectileSource projectileSource = ((Projectile) damagerEntity).getShooter();
            if (!(projectileSource instanceof Player)) {
                return;
            }
            attacker = (Player) projectileSource;
        } else {
            return;
        }
        ArenaManager arenaManager = DuelTimePlugin.getInstance().getArenaManager();
        Player target = (Player) event.getEntity();
        BaseArena arena = arenaManager.getOf(target);
        if (!(arena instanceof ClassicArena)) {
            return;
        }
        BaseArena attackerArena = arenaManager.getOf(attacker);
        if (attackerArena == null || !arena.getId().equals(attackerArena.getArenaData().getId())) {
            return;
        }
        if (((ClassicArena) arena).getStage() == ClassicArena.Stage.COUNTDOWN) {
            event.setCancelled(true);
            MsgBuilder.sendActionBar(Msg.ARENA_TYPE_CLASSIC_DAMAGE_DURING_COUNTDOWN, attacker, true);
        } else {
            ClassicGamerData gamerData = (ClassicGamerData) arena.getGamerData(attacker.getName());
            if (gamerData != null) {
                gamerData.addHitTime();
                gamerData.addDamage(event.getDamage());
                gamerData.checkAndSetMaxDamage(event.getDamage());
            }
        }
        if (arena.getArenaData().hasFunction(ArenaType.FunctionInternalType.CLASSIC_SPECTATE) && (boolean) (arena.getArenaData().getFunctionData(CLASSIC_SPECTATE)[3])) {
            ((ClassicArena) arena).updateGamerHealthSpectated(target);
        }
    }

    /*
    经典模式玩家死亡事件：
    若有一方死亡，无论什么原因，直接视为这一方输掉游戏
     */
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        BaseArena arena = DuelTimePlugin.getInstance().getArenaManager().getOf(event.getEntity());
        if (arena == null) return;
        if (!(arena instanceof ClassicArena)) return;
        ruleAsLoss((ClassicArena) arena, event.getEntity(), null);
    }

    /*
    经典模式玩家下线事件：
    下线一方直接视为输掉游戏
     */
    @EventHandler
    public void onLeaveServer(PlayerQuitEvent event) {
        BaseArena arena = DuelTimePlugin.getInstance().getArenaManager().getOf(event.getPlayer());
        if (arena == null) return;
        if (!(arena instanceof ClassicArena)) return;
        Player player = event.getPlayer();
        Player opponent = ((ClassicArena) arena).getOpponent(player);
        ruleAsLoss((ClassicArena) arena, player, () -> MsgBuilder.send(Msg.ARENA_TYPE_CLASSIC_LEAVE_SERVER_INFORM_OPPONENT, opponent));
    }

    /*
    经典模式玩家请求退出场比赛事件
     */
    @EventHandler
    public void onTryToQuit(ArenaTryToQuitEvent event) {
        BaseArena arena = event.getArena();
        if (arena == null) return;
        if (!(arena instanceof ClassicArena)) return;
        Player player = event.getPlayer();
        Player opponent = ((ClassicArena) arena).getOpponent(player);
        ruleAsLoss((ClassicArena) arena, player, () -> {
            MsgBuilder.send(Msg.COMMAND_SUB_QUIT_SUCCESSFULLY, player, arena.getArenaData().getName());
            MsgBuilder.send(Msg.ARENA_TYPE_CLASSIC_QUIT_INFORM_OPPONENT, opponent);
        });

    }

    //仅仅用于减少重复代码，用于处理那些必定判为输的情况，如死亡、下线等
    private void ruleAsLoss(ClassicArena arena, Player player, Action action) {
        if (action != null) {
            action.run();
        }
        Player opponent = arena.getOpponent(player);
        if (opponent == null) {
            return;
        }
        arena.confirmResult(ClassicArena.Result.CLEAR, opponent);
        BaseGamerData playerGamerData = arena.getGamerData(player.getName());
        BaseGamerData opponentGamerData = arena.getGamerData(opponent.getName());
        if (playerGamerData != null) {
            ((ClassicGamerData) playerGamerData).confirmResult(ClassicArenaRecordData.Result.LOSE);
        }
        if (opponentGamerData != null) {
            ((ClassicGamerData) opponentGamerData).confirmResult(ClassicArenaRecordData.Result.WIN);
        }
        DuelTimePlugin.getInstance().getArenaManager().end(arena.getId());
    }

    private interface Action {
        void run();
    }

    /*
    经典模式玩家移动事件：
    根据附加功能数据判定倒计时期间禁止移动与否
    比赛开始后参赛者禁止离开竞技场区域
    比赛开始后观众禁止离开观众席区域
     */
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        // 防御：插件重载/启动早期/异步竞态下 arenaManager 可能尚未初始化
        ArenaManager arenaManager = DuelTimePlugin.getInstance().getArenaManager();
        if (arenaManager == null) {
            return;
        }
        BaseArena baseArena = arenaManager.getOf(player);
        if (baseArena instanceof ClassicArena) {
            ClassicArena arena = (ClassicArena) baseArena;
            if (arena.getStage() == null || arena.getStage().equals(ClassicArena.Stage.COUNTDOWN)) {
                if (!(boolean) arena.getArenaData().getFunctionData(CLASSIC_COUNTDOWN)[1]) {
                    UtilSync.safeTeleport(player, arena.getPlayerStartLocationMap(player.getName()));
                }
            } else {
                ClassicGamerData gamerData = ((ClassicGamerData) arena.getGamerData(player.getName()));
                if (gamerData != null) {
                    if (!UtilGeometry.inArena(event.getTo(), arena)) {
                        UtilSync.safeTeleport(player, gamerData.getRecentLocation());
                    } else {
                        gamerData.updateRecentLocation(player.getLocation());
                    }
                }
            }
            return;
        }
        baseArena = DuelTimePlugin.getInstance().getArenaManager().getSpectate(player);
        if (baseArena instanceof ClassicArena) {
            Object[] spectateFunctionData = baseArena.getArenaData().getFunctionData(ArenaType.FunctionInternalType.CLASSIC_SPECTATE);
            if (spectateFunctionData == null) {
                return;
            }
            Location spectateZoneD1 = (Location) spectateFunctionData[0];
            Location spectateZoneD2 = (Location) spectateFunctionData[1];
            ClassicSpectatorData spectatorData = (ClassicSpectatorData) baseArena.getSpector(player.getName());
            if (spectatorData != null) {
                if (!UtilGeometry.inZone(event.getTo(), spectateZoneD1, spectateZoneD2)) {
                    UtilSync.safeTeleport(player, spectatorData.getRecentLocation());
                    MsgBuilder.sendActionBar(Msg.ARENA_TYPE_CLASSIC_FUNCTION_SPECTATE_SPECTATOR_MOVE_OUT_ZONE, player, true);
                } else {
                    spectatorData.updateRecentLocation(player.getLocation());
                }
            }
        }
    }

    /*
    经典模式输入指令事件
    禁止非op玩家使用除了quit以外的所有指令
     */
    @EventHandler
    public void onEnterCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        BaseArena arena = DuelTimePlugin.getInstance().getArenaManager().getOf(player);
        if (arena == null) {
            return;
        }
        if (!(arena instanceof ClassicArena)) {
            return;
        }
        String commandEntered = event.getMessage();
        List<String> commandAliases = DuelTimePlugin.getInstance().getCommand("dueltime").getAliases();
        boolean isQuitCommand = false;
        for (String commandAlias : commandAliases) {
            for (String labelAlias : DuelTimePlugin.getInstance().getCommandHandler().getSubCommand("quit").getAliases()) {
                if (commandEntered.equals("/" + commandAlias + " " + labelAlias)) {
                    isQuitCommand = true;
                    break;
                }
            }
        }
        if (!isQuitCommand) {
            if (player.hasPermission(CommandPermission.ADMIN)) {
                return;
            }
            MsgBuilder.send(Msg.ARENA_TYPE_CLASSIC_USE_COMMAND_IN_GAME, player);
            event.setCancelled(true);
        }
    }

    /*
    经典模式请求开赛事件
    检测各种开赛条件，如物品检测（附加功能）等
     */
    @EventHandler
    public void onTryToStart(ArenaTryToStartEvent event) {
        BaseArena baseArena = event.getArena();
        if (!(baseArena instanceof ClassicArena)) {
            return;
        }
        ClassicArena arena = (ClassicArena) baseArena;
        boolean checkKeyword = arena.getArenaData().hasFunction(ArenaType.FunctionInternalType.CLASSIC_INVENTORY_CHECK_KEYWORD);
        boolean checkMaterial = arena.getArenaData().hasFunction(ArenaType.FunctionInternalType.CLASSIC_INVENTORY_CHECK_TYPE);
        if (!checkKeyword && !checkMaterial) {
            return;
        }
        String checkRange = null;
        List<String> keywords = null;
        List<String> types = null;
        if (checkKeyword) {
            Object[] checkKeywordFuncData = arena.getArenaData().getFunctionData(ArenaType.FunctionInternalType.CLASSIC_INVENTORY_CHECK_KEYWORD);
            checkRange = (String) checkKeywordFuncData[0];
            keywords = (List<String>) checkKeywordFuncData[1];
        }
        if (checkMaterial) {
            types = (List<String>) arena.getArenaData().getFunctionData(ArenaType.FunctionInternalType.CLASSIC_INVENTORY_CHECK_TYPE)[0];
        }
        Player playerDetected = null;
        ItemStack itemStackDetected = null;
        boolean isKeywordDetected = false;
        Player[] players = event.getPlayers();
        check:
        for (Player player : players) {
            Inventory inventory = player.getInventory();
            for (int slot = -1; slot < 40; slot++) {
                //-1代表光标上的物品，防止玩家把物品藏在光标上逃避检测
                ItemStack itemStack = slot == -1 ? player.getItemOnCursor() : inventory.getItem(slot);
                if (itemStack == null) continue;
                if (keywords != null && itemStack.hasItemMeta()) {
                    if (checkRange.equals("name") || checkRange.equals("all")) {
                        String displayName = itemStack.getItemMeta().getDisplayName();
                        if (displayName == null) continue;
                        for (String keyword : keywords) {
                            if (displayName.contains(keyword)) {
                                playerDetected = player;
                                itemStackDetected = itemStack;
                                isKeywordDetected = true;
                                break check;
                            }
                        }
                    }
                    if (checkRange.equals("lore") || checkRange.equals("all")) {
                        List<String> lores = itemStack.getItemMeta().getLore();
                        if (lores == null) continue;
                        for (String lore : lores) {
                            for (String keyword : keywords) {
                                if (lore.contains(keyword)) {
                                    playerDetected = player;
                                    itemStackDetected = itemStack;
                                    isKeywordDetected = true;
                                    break check;
                                }
                            }
                        }
                    }
                }
                if (types != null) {
                    for (String type : types) {
                        String[] clips = type.split(":");
                        String material = clips[0];
                        byte subId = clips.length == 1 ? (byte) 0 : Byte.parseByte(clips[1]);
                        if (itemStack.getType().name().equalsIgnoreCase(material) && itemStack.getData().getData() == subId) {
                            playerDetected = player;
                            itemStackDetected = itemStack;
                            break check;
                        }
                    }
                }
            }
        }
        if (itemStackDetected != null) {
            event.setCancelled(true);
            String name = itemStackDetected.getItemMeta().getDisplayName();
            if (name == null) name = itemStackDetected.getType().name();
            List<String> lore = itemStackDetected.getItemMeta().getLore();
            String loreStr = name + (lore != null ? "||" + String.join("||", lore) : "");
            ItemDetailInventoryHolder.itemMap.put(playerDetected.getName(), itemStackDetected);
            if (isKeywordDetected) {
                MsgBuilder.sendClickable(Msg.ARENA_TYPE_CLASSIC_FUNCTION_INVENTORY_CHECK_KEYWORD_DETECTED_TIP, playerDetected, true,
                        arena.getName(), name, loreStr);
                for (Player player : players)
                    MsgBuilder.send(Msg.ARENA_TYPE_CLASSIC_FUNCTION_INVENTORY_CHECK_KEYWORD_DETECTED, player,
                            playerDetected.getName(), arena.getName());
            } else {
                MsgBuilder.sendClickable(Msg.ARENA_TYPE_CLASSIC_FUNCTION_INVENTORY_CHECK_TYPE_DETECTED_TIP, playerDetected, true,
                        arena.getName(), name, loreStr);
                for (Player player : players)
                    MsgBuilder.send(Msg.ARENA_TYPE_CLASSIC_FUNCTION_INVENTORY_CHECK_TYPE_DETECTED, player,
                            playerDetected.getName(), arena.getName());
            }
        }
    }

    /*
    经典模式正式开赛事件（具体点来说是竞技场通过start()初始化完毕后，而不是倒计时结束后）
    用于处理赛前指令（即入场后的一瞬间执行的指令）
     */
    @EventHandler
    public void onStart(ArenaStartEvent event) {
        BaseArena baseArena = event.getArena();
        if (!(baseArena instanceof ClassicArena)) {
            return;
        }
        ClassicArena arena = (ClassicArena) baseArena;
        if (arena.getArenaData().hasFunction(ArenaType.FunctionInternalType.CLASSIC_PRE_GAME_COMMAND)) {
            List<String> commandDataList = (List<String>) (arena.getArenaData().getFunctionData(ArenaType.FunctionInternalType.CLASSIC_PRE_GAME_COMMAND)[0]);
            for (String commandData : commandDataList) {
                String identity = commandData.split(":")[0];
                String content = commandData.substring(identity.length() + 1)
                        .replace("{world}", arena.getArenaData().getDiagonalPointLocation1().getWorld().getName());
                if (identity.equalsIgnoreCase("single_console")) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), content);
                } else {
                    for (BaseGamerData gamerData : arena.getGamerDataList()) {
                        Player player = gamerData.getPlayer();
                        content = content.replace("{player}", player.getName());
                        switch (identity) {
                            case "player":
                                Bukkit.dispatchCommand(gamerData.getPlayer(), content);
                                break;
                            case "op":
                                boolean isOpBefore = player.isOp();
                                if (!isOpBefore) player.setOp(true);
                                Bukkit.dispatchCommand(gamerData.getPlayer(), content);
                                if (!isOpBefore) player.setOp(false);
                                break;
                            case "console":
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), content);
                        }
                    }
                }
            }
        }
    }

    /*
    经典模式请求观战事件
     */
    @EventHandler
    public void onTryToSpectate(ArenaTryToSpectateEvent event) {
        BaseArena baseArena = event.getArena();
        if (!(baseArena instanceof ClassicArena)) {
            return;
        }
        ClassicArena arena = (ClassicArena) baseArena;
        Player player = event.getPlayer();
        if (!arena.getArenaData().hasFunction(ArenaType.FunctionInternalType.CLASSIC_SPECTATE)) {
            MsgBuilder.send(Msg.ARENA_TYPE_CLASSIC_FUNCTION_SPECTATE_FAIL_UNAVAILABLE, player,
                    arena.getArenaData().getName());
            return;
        }
        arena.addSpectator(player);
        UtilSync.safeTeleport(player, (Location) (arena.getArenaData().getFunctionData(ArenaType.FunctionInternalType.CLASSIC_SPECTATE)[2]));
        ClassicSpectatorData spectatorData = (ClassicSpectatorData) arena.getSpector(player.getName());
        if (DuelTimePlugin.serverVersionInt >= 8) {
            SchedulerUtil.runTaskLater(() ->
                    player.setGameMode(GameMode.SPECTATOR), 1);
        }
        spectatorData.updateRecentLocation(player.getLocation());
        MsgBuilder.send(Msg.ARENA_TYPE_CLASSIC_FUNCTION_SPECTATE_SUCCESSFULLY, player,
                arena.getArenaData().getName());
    }

    /*
    经典模式请求退出观战事件
     */
    @EventHandler
    public void onTryToQuitSpectate(ArenaTryToQuitSpectateEvent event) {
        BaseArena baseArena = event.getArena();
        if (!(baseArena instanceof ClassicArena)) {
            return;
        }
        ClassicArena arena = (ClassicArena) baseArena;
        Player player = event.getPlayer();
        ClassicSpectatorData spectatorData = (ClassicSpectatorData) arena.getSpector(player.getName());
        if (arena.getArenaData().hasFunction(ArenaType.FunctionInternalType.CLASSIC_SPECTATE) && (boolean) (arena.getArenaData().getFunctionData(CLASSIC_SPECTATE)[3])) {
            for (BossBar bossBar : arena.getHealthBossBars().values()) {
                bossBar.removePlayer(player);
            }
        }
        if (DuelTimePlugin.serverVersionInt >= 8) {
            player.setGameMode(spectatorData.getOriginalGameMode());
        }
        spectateLeaveWorldSkipCheck.add(player.getName());
        UtilSync.safeTeleport(player, spectatorData.getOriginalLocation());
        spectateLeaveWorldSkipCheck.remove(player.getName());
    }

    /*
    经典模式请求强制停止竞技场比赛事件
     */
    @EventHandler
    public void onTryToStop(ArenaTryToStopEvent event) {
        BaseArena arena = event.getArena();
        if (!(arena instanceof ClassicArena)) {
            return;
        }
        ((ClassicArena) arena).confirmResult(ClassicArena.Result.STOPPED, null);
        for (BaseGamerData baseGamerData : arena.getGamerDataList()) {
            Player gamerPlayer = baseGamerData.getPlayer();
            if (gamerPlayer == null) {
                continue;
            }
            String reason = event.getReason();
            if (reason == null || reason.isEmpty()) {
                MsgBuilder.send(Msg.ARENA_TYPE_CLASSIC_END_RESULT_STOPPED, gamerPlayer, false);
            } else {
                MsgBuilder.send(Msg.ARENA_TYPE_CLASSIC_END_RESULT_STOPPED_WITH_REASON, gamerPlayer, false,
                        event.getReason());
            }
        }
        DuelTimePlugin.getInstance().getArenaManager().end(arena.getId());
    }

    public static Map<String, Location> respawnLocMap = new HashMap<>();

    /*
    玩家复活事件
    复活后传送回原点或大厅点（用于为添加自动重生这一附加功能的情况）
     */
    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (player != null && player.getName() != null && respawnLocMap.containsKey(player.getName())) {
            SchedulerUtil.runTaskLater(() -> {
                UtilSync.safeTeleport(player, respawnLocMap.get(player.getName()));
                respawnLocMap.remove(player.getName());
            }, 5);
        }
    }

    /*
    玩家下线事件
    移除潜在的指定重生点，避免影响一些指定上线地点的登录类插件的运作
     */
    @EventHandler
    public void onLeaveServerWithRespawnLoc(PlayerQuitEvent event) {
        respawnLocMap.remove(event.getPlayer().getName());
    }

    private static final List<String> spectateLeaveWorldSkipCheck = new ArrayList<>();

    /*
    观众传送事件
    一旦发生跨世界传送，直接视为取消观看
     */
    @EventHandler
    public void onSpectatorLeaveArenaWorld(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (spectateLeaveWorldSkipCheck.contains(player.getName())) return;
        ArenaManager arenaManager = DuelTimePlugin.getInstance().getArenaManager();
        BaseArena arena = arenaManager.getSpectate(player);
        if (arena == null) {
            return;
        }
        if (event.getTo().getWorld().getName().equals(arena.getArenaData().getDiagonalPointLocation1().getWorld().getName())) {
            return;
        }
        if (DuelTimePlugin.serverVersionInt >= 8) {
            player.setGameMode(((ClassicSpectatorData) arena.getSpector(player.getName())).getOriginalGameMode());
        }
        arenaManager.removeSpectator(player);
    }

    /*
    观众离开服务器事件
    一旦离开服务器，直接视为取消观看
     */
    @EventHandler
    public void onSpectatorLeaveServer(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ArenaManager arenaManager = DuelTimePlugin.getInstance().getArenaManager();
        BaseArena arena = arenaManager.getSpectate(player);
        if (arena == null) {
            return;
        }
        if (DuelTimePlugin.serverVersionInt >= 8) {
            player.setGameMode(((ClassicSpectatorData) arena.getSpector(player.getName())).getOriginalGameMode());
        }
        arenaManager.removeSpectator(player);
    }

    /*
    实体生成事件
    如果竞技场添加了禁止实体刷出的附加功能且涉及实体种类不在白名单内，则阻止
     */
    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        BaseArena baseArena = UtilGeometry.getArena(event.getLocation());
        if (!(baseArena instanceof ClassicArena)) {
            return;
        }
        ClassicArena arena = (ClassicArena) baseArena;
        if (arena.getArenaData().hasFunction(ArenaType.FunctionInternalType.CLASSIC_BAN_ENTITY_SPAWN)) {
            List<String> whitelist = (List<String>) arena.getArenaData().getFunctionData(CLASSIC_BAN_ENTITY_SPAWN)[0];
            if (!whitelist.contains(event.getEntityType().name())) {
                event.setCancelled(true);
            }
        }
    }
}
