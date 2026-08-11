package cn.valorin.dueltime.cache;

import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.data.MyBatisManager;
import cn.valorin.dueltime.data.mapper.PlayerDataMapper;
import cn.valorin.dueltime.data.pojo.PlayerData;
import cn.valorin.dueltime.event.cache.CacheInitializedEvent;
import cn.valorin.dueltime.event.ranking.TryToRefreshRankingEvent;
import cn.valorin.dueltime.level.LevelManager;
import cn.valorin.dueltime.level.Tier;
import cn.valorin.dueltime.ranking.Ranking;
import cn.valorin.dueltime.util.UtilSync;
import cn.valorin.dueltime.util.SchedulerUtil;
import cn.valorin.dueltime.viaversion.ViaVersion;
import cn.valorin.dueltime.yaml.message.Msg;
import cn.valorin.dueltime.yaml.message.MsgBuilder;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class PlayerDataCache {
    private final Map<String, PlayerData> playerDataMap = new HashMap<>();

    public void reloadRefreshRankingTimer() {
        int interval = DuelTimePlugin.getInstance().getCfgManager().getRankingAutoRefreshInterval();
        // 最小刷新间隔设为60秒，避免过于频繁的数据库操作影响性能
        if (interval < 60) interval = 60;
        refreshRankingTimer = SchedulerUtil.runTaskTimerAsync(() -> {
            // 异步执行，避免阻塞主线程
            for (Ranking ranking : DuelTimePlugin.getInstance().getRankingManager().getRankings().values()) {
                // 使用异步调度器处理每个排名的刷新
                SchedulerUtil.runTaskAsync(() -> {
                    UtilSync.publishEvent(new TryToRefreshRankingEvent(null, ranking));
                });
            }
        }, 3 * 20L, interval * 20L);
    }

    public void reload() {
        SqlSessionFactory sqlSessionFactory = DuelTimePlugin.getInstance().getMyBatisManager().getFactory(this.getClass());
        try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
            PlayerDataMapper mapper = sqlSession.getMapper(PlayerDataMapper.class);
            mapper.createTableIfNotExists();
            for (Player player : ViaVersion.getOnlinePlayers()) {
                if (player != null && player.getName() != null) {
                    String playerName = player.getName();
                    PlayerData playerDataInDatabase = mapper.get(playerName);
                    PlayerData playerData =
                            playerDataInDatabase != null ?
                                    playerDataInDatabase :
                                    new PlayerData(playerName, 0, 0, null, 0, 0, 0, 0, 0, 0);
                    playerDataMap.put(playerName, playerData);
                    DuelTimePlugin.getInstance().getLevelManager().load(playerName, playerData.getExp());
                }
            }
            Bukkit.getServer().getPluginManager().callEvent(new CacheInitializedEvent(this.getClass()));
        }
    }

    public void reload(Player player) {
        if (player == null || player.getName() == null) {
            return;
        }
        String playerName = player.getName();
        if (playerDataMap.containsKey(playerName)) {
            return;
        }
        try (SqlSession sqlSession = DuelTimePlugin.getInstance().getMyBatisManager().getFactory(this.getClass()).openSession()) {
            PlayerData playerDataInDatabase = sqlSession.getMapper(PlayerDataMapper.class).get(playerName);
            PlayerData playerData =
                    playerDataInDatabase != null ?
                            playerDataInDatabase :
                            new PlayerData(playerName, 0, 0, null, 0, 0, 0, 0, 0, 0);
            playerDataMap.put(playerName, playerData);
            DuelTimePlugin.getInstance().getLevelManager().load(playerName, playerData.getExp());
        }
    }

    public PlayerData get(String playerName) {
        PlayerData playerData = playerDataMap.get(playerName);
        return playerData != null ? playerData.clone() : null;
    }

    /**
     * 考虑到性能因素，在加载缓存时，不会将所有玩家的数据都加载进来
     * 每个玩家对应的PlayerData对象只会在玩家上线时加载
     * 但如果一定要获取某个玩家对应的PlayerData对象，那就需要调用这个方法
     *
     * @param playerName 玩家名
     * @return PlayerData对象的拷贝（获取的是拷贝体，原因是如此能方便修改与确认，且最后可以作为整体传入，便于分析数据差异）
     */
    public PlayerData getAnyway(String playerName) {
        PlayerData playerData = playerDataMap.get(playerName);
        if (playerData == null) {
            //如果缓存中找不到这个玩家，再从数据库里找
            try (SqlSession sqlSession = DuelTimePlugin.getInstance().getMyBatisManager().getFactory(this.getClass()).openSession()) {
                PlayerData playerDataInDatabase = sqlSession.getMapper(PlayerDataMapper.class).get(playerName);
                if (playerDataInDatabase == null) {
                    //如果数据库里也不存在这个玩家，则确认不存在
                    return null;
                } else {
                    //如果数据库找到了这个玩家，则获取信息
                    playerData = playerDataInDatabase;
                }
            }
        }
        return playerData.clone();
    }

    public void set(String playerName, PlayerData playerData) {
        PlayerData playerDataBefore = playerDataMap.get(playerName);
        LevelManager levelManager = DuelTimePlugin.getInstance().getLevelManager();
        Tier tierBefore = levelManager.getTier(playerName);
        int levelBefore = levelManager.getLevel(playerName);
        playerDataMap.put(playerName, playerData);
        if (playerDataBefore.getExp() != playerData.getExp()) {
            //如果经验值发生了变更，则需通知LevelManager重新计算等级和段位名
            DuelTimePlugin.getInstance().getLevelManager().load(playerName, playerData.getExp());
            //告知与广播变化
            Player player = Bukkit.getPlayerExact(playerName);
            int level = levelManager.getLevel(playerName);
            if (level > levelBefore && player != null) {
                MsgBuilder.send(Msg.LEVEL_LEVEL_UP_MESSAGE, player,
                        "" + level);
            }
            Tier tier = levelManager.getTier(playerName);
            if (tier.compare(tierBefore) > 0) {
                MsgBuilder.broadcast(Msg.LEVEL_TIER_UP_BROADCAST, false,
                        playerName, tier.getTitle());
                if (player != null) {
                    MsgBuilder.send(Msg.LEVEL_TIER_UP_MESSAGE, player,
                            tier.getTitle());
                }
            } else if (tier.compare(tierBefore) < 0 && player != null) {
                MsgBuilder.send(Msg.LEVEL_TIER_DOWN_MESSAGE, player,
                        tier.getTitle());
            }
        }
        MyBatisManager myBatisManager = DuelTimePlugin.getInstance().getMyBatisManager();
        try (SqlSession sqlSession = myBatisManager.getFactory(this.getClass()).openSession(true)) {
            if (myBatisManager.getType(this.getClass()).equals(MyBatisManager.DatabaseType.MYSQL)) {
                sqlSession.getMapper(PlayerDataMapper.class).insertOrUpdateMySQL(playerData);
            } else {
                sqlSession.getMapper(PlayerDataMapper.class).insertOrUpdateSQLite(playerData);
            }
        }
    }

    private BukkitTask refreshRankingTimer;

    public BukkitTask getRefreshRankingTimer() {
        return refreshRankingTimer;
    }
}
