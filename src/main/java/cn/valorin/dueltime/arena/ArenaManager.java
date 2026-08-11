package cn.valorin.dueltime.arena;

import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.arena.base.BaseArena;
import cn.valorin.dueltime.arena.base.BaseArenaData;
import cn.valorin.dueltime.arena.base.BaseGamerData;
import cn.valorin.dueltime.arena.type.ArenaType;
import cn.valorin.dueltime.data.mapper.ClassicArenaDataMapper;
import cn.valorin.dueltime.data.pojo.ClassicArenaData;
import cn.valorin.dueltime.event.arena.*;
import cn.valorin.dueltime.gui.CustomInventoryManager;
import cn.valorin.dueltime.yaml.message.Msg;
import cn.valorin.dueltime.yaml.message.MsgBuilder;
import cn.valorin.dueltime.util.SchedulerUtil;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * 活跃态竞技场管理器，负责Arena竞技场实例的缓存工作
 * 但出于一些考虑，这个类没有并入cache缓存包中
 * 首先，BaseArena是一个活跃态的竞技场对象，并非直接存入数据库的对象（指BaseArenaData)
 * 其次，竞技场数据没有跨服共享的意义，与其他模块的数据共同点少
 */
public class ArenaManager {
    private final Map<String, BaseArena> arenaMap = new HashMap<>();
    private final Map<String, String> gamerArenaMap = new HashMap<>();
    private final Map<String, String> spectatorArenaMap = new HashMap<>();
    private final Map<String, String> waitingPlayerToArenaMap = new HashMap<>();
    private final Map<String, List<String>> waitingArenaToPlayersMap = new HashMap<>();

    public ArenaManager() {
        reload();
    }

    /**
     * 根据各个类型的场地数据(ArenaData)载入所有竞技场
     */
    public void reload() {
        SqlSessionFactory sqlSessionFactory = DuelTimePlugin.getInstance().getMyBatisManager().getFactory(this.getClass());
        try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
            //载入经典类型竞技场
            ClassicArenaDataMapper classicArenaDataMapper = sqlSession.getMapper(ClassicArenaDataMapper.class);
            classicArenaDataMapper.createTableIfNotExists();
            for (ClassicArenaData arenaData : classicArenaDataMapper.getAll()) {
                ClassicArena classicArena;
                try {
                    classicArena = new ClassicArena(arenaData);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    continue;
                }
                arenaMap.put(arenaData.getId(), classicArena);
            }
            //载入其他类型竞技场...
        }
    }

    public BaseArena get(String id) {
        return arenaMap.get(id);
    }


    public BaseArena getOf(Player player) {
        if (player == null || player.getName() == null) {
            return null;
        }
        return arenaMap.get(gamerArenaMap.get(player.getName()));
    }

    public BaseArena getSpectate(Player player) {
        if (player == null || player.getName() == null) {
            return null;
        }
        return arenaMap.get(spectatorArenaMap.get(player.getName()));
    }

    //已弃用，现改用缓存来处理玩家-竞技场的对应关系
    @Deprecated
    public BaseArena getOfWithoutCache(Player player) {
        if (player == null || player.getName() == null) {
            return null;
        }
        String playerName = player.getName();
        for (BaseArena arena : arenaMap.values()) {
            List<BaseGamerData> gamerDataList = arena.getGamerDataList();
            if (gamerDataList == null || gamerDataList.isEmpty()) {
                continue;
            }
            for (BaseGamerData gamerData : arena.getGamerDataList()) {
                if (gamerData.getPlayer().getName().equals(playerName)) {
                    return arena;
                }
            }
        }
        return null;
    }

    //经由本管理器来调用比赛开始的方法，这么做会事先发布比赛尝试开始的事件，同时顺带清理等待者列表、载入玩家-竞技场映射（这些工作可能会被不走这个方法的开发者疏漏），最后发布比赛开始事件（若未被取消）
    public void start(String id, Object data, Player... players) {
        BaseArena arena = get(id);
        ArenaTryToStartEvent event = new ArenaTryToStartEvent(arena, players);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            for (Player player : players) {
                if (player != null && player.getName() != null && waitingPlayerToArenaMap.containsKey(player.getName())) {
                    removeWaitingPlayer(player);
                }
            }
            return;
        }
        waitingArenaToPlayersMap.remove(id);
        for (Player player : players) {
            if (player != null && player.getName() != null) {
                addGamerToMap(player, id);
                // 使用Folia兼容的方式关闭玩家的库存
                SchedulerUtil.runTaskForPlayer(player, () -> player.closeInventory());
                waitingPlayerToArenaMap.remove(player.getName());
            }
        }
        arena.start(data, players);
        // 使用Folia兼容的方式发布事件和更新界面
        SchedulerUtil.runTask(() -> {
            Bukkit.getServer().getPluginManager().callEvent(new ArenaStartEvent(arena));
            updateStartInventory();
        });
    }

    //经由本管理器来调用比赛结束的方法，这么做会事先发布比赛结束的事件，同时顺带清除相关的玩家-竞技场映射（这些工作可能会被不走这个方法的开发者疏漏）
    public void end(String id) {
        BaseArena arena = get(id);
        ArenaTryToEndEvent event = new ArenaTryToEndEvent(arena);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            for (BaseGamerData gamerData : arena.getGamerDataList()) {
                removeGamerFromMap(gamerData.getPlayerName());
            }
            arena.end();
            updateStartInventory();
            Bukkit.getServer().getPluginManager().callEvent(new ArenaEndEvent(arena));
        }
    }

    public void stop(String id, String reason) {
        BaseArena arena = get(id);
        Bukkit.getServer().getPluginManager().callEvent(new ArenaTryToStopEvent(arena, reason));
        for (BaseGamerData gamerData : arena.getGamerDataList()) {
            removeGamerFromMap(gamerData.getPlayerName());
        }
        updateStartInventory();
        Bukkit.getServer().getPluginManager().callEvent(new ArenaStopEvent(arena, reason));
    }

    //经由本管理器来整合并调用玩家中途加入的方法，这么做会事先发布玩家加入的的事件，同时顺带载入玩家-竞技场映射（这些工作可能会被不走这个方法的开发者疏漏）
    public void join(Player player, String id, ArenaTryToJoinEvent.Way way) {
        BaseArena arena = get(id);
        ArenaTryToJoinEvent event = new ArenaTryToJoinEvent(player, arena, way);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            addGamerToMap(player, id);
        }
        updateStartInventory();
    }

    public void addGamerToMap(Player player, String id) {
        if (player != null && player.getName() != null) {
            gamerArenaMap.put(player.getName(), id);
        }
    }

    public void removeGamerFromMap(String playerName) {
        if (playerName != null) {
            gamerArenaMap.remove(playerName);
        }
    }

    //经由本管理器来整合并调用玩家观战的方法，这么做会事先发布玩家观战的的事件，同时顺带载入玩家-竞技场映射（这些工作可能会被不走这个方法的开发者疏漏）
    public void spectate(Player player, String id) {
        BaseArena arena = get(id);
        ArenaTryToSpectateEvent event = new ArenaTryToSpectateEvent(player, arena);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            if (player != null && player.getName() != null) {
                spectatorArenaMap.put(player.getName(), id);
            }
        }
    }

    public void removeSpectator(Player player) {
        if (player == null || player.getName() == null) {
            return;
        }
        BaseArena arena = getSpectate(player);
        ArenaTryToQuitSpectateEvent event = new ArenaTryToQuitSpectateEvent(player, arena);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            arena.removeSpectatorData(player);
            spectatorArenaMap.remove(player.getName());
        }
    }

    public void addWaitingPlayer(Player player, String id) {
        if (player == null || player.getName() == null) {
            return;
        }
        String playerName = player.getName();
        boolean isSwitch = !waitingPlayerToArenaMap.getOrDefault(playerName, id).equals(id);
        BaseArena arena = get(id);
        ArenaTryToWaitEvent event = new ArenaTryToWaitEvent(player, arena);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        if (isSwitch) {
            String oldId = waitingPlayerToArenaMap.get(playerName);
            List<String> watingPlayerList = waitingArenaToPlayersMap.getOrDefault(oldId, new ArrayList<>());
            watingPlayerList.remove(player.getName());
            waitingArenaToPlayersMap.put(oldId, watingPlayerList);
        }
        waitingPlayerToArenaMap.put(playerName, id);
        List<String> watingPlayerList = waitingArenaToPlayersMap.getOrDefault(id, new ArrayList<>());
        watingPlayerList.add(playerName);
        waitingArenaToPlayersMap.put(id, watingPlayerList);
        if (watingPlayerList.size() >= arena.getArenaData().getMinPlayerNumber()) {
            // 使用异步调度器来启动比赛，避免阻塞主线程
            SchedulerUtil.runTask(() -> {
                player.closeInventory();
                start(arena.getId(), null, watingPlayerList.stream().map(Bukkit::getPlayerExact).filter(Objects::nonNull).toArray(Player[]::new));
            });
            return;
        }
        Bukkit.getServer().getPluginManager().callEvent(new ArenaWaitEvent(player, arena));
        MsgBuilder.send(isSwitch ? Msg.ARENA_WAIT_SWITCH : Msg.ARENA_WAIT_START, player, arena.getName());
        updateStartInventory();
    }

    public void removeWaitingPlayer(Player player) {
        if (player == null || player.getName() == null) {
            return;
        }
        String playerName = player.getName();
        if (waitingPlayerToArenaMap.containsKey(playerName)) {
            String arenaId = waitingPlayerToArenaMap.get(playerName);
            if (waitingArenaToPlayersMap.containsKey(arenaId)) {
                waitingArenaToPlayersMap.get(arenaId).remove(playerName);
            }
        }
        waitingPlayerToArenaMap.remove(playerName);
        updateStartInventory();
    }

    public BaseArena getWaitingFor(Player player) {
        if (player == null || player.getName() == null) {
            return null;
        }
        return arenaMap.get(waitingPlayerToArenaMap.get(player.getName()));
    }

    public List<String> getWaitingPlayers(String id) {
        return waitingArenaToPlayersMap.getOrDefault(id, new ArrayList<>());
    }

    public Map<String, BaseArena> getMap() {
        return arenaMap;
    }

    public List<BaseArena> getList() {
        return new ArrayList<>(arenaMap.values());
    }

    public int size() {
        return arenaMap.size();
    }

    public void add(BaseArena arena) {
        BaseArenaData arenaData = arena.getArenaData();
        arenaMap.put(arenaData.getId(), arena);
        try (SqlSession sqlSession = DuelTimePlugin.getInstance().getMyBatisManager().getFactory(this.getClass()).openSession(true)) {
            if (arenaData.getTypeId().equals(ArenaType.InternalType.CLASSIC.getId())) {
                sqlSession.getMapper(ClassicArenaDataMapper.class).add((ClassicArenaData) arenaData);
            }
        }
    }

    public void update(BaseArenaData arenaData) {
        BaseArena arena = arenaMap.get(arenaData.getId());
        arena.setArenaData(arenaData);
        arenaMap.put(arenaData.getId(), arena);
        try (SqlSession sqlSession = DuelTimePlugin.getInstance().getMyBatisManager().getFactory(this.getClass()).openSession(true)) {
            if (arenaData.getTypeId().equals(ArenaType.InternalType.CLASSIC.getId())) {
                sqlSession.getMapper(ClassicArenaDataMapper.class).update((ClassicArenaData) arenaData);
            }
        }
    }

    public void delete(String id) {
        arenaMap.remove(id);
        try (SqlSession sqlSession = DuelTimePlugin.getInstance().getMyBatisManager().getFactory(this.getClass()).openSession(true)) {
            sqlSession.getMapper(ClassicArenaDataMapper.class).delete(id);
        }
    }

    private void updateStartInventory() {
        CustomInventoryManager customInventoryManager = DuelTimePlugin.getInstance().getCustomInventoryManager();
        customInventoryManager.updatePage(customInventoryManager.getStart());
    }
}
