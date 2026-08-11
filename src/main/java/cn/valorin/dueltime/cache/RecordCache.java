package cn.valorin.dueltime.cache;

import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.arena.base.BaseRecordData;
import cn.valorin.dueltime.data.mapper.ClassicArenaRecordDataMapper;
import cn.valorin.dueltime.data.pojo.ClassicArenaRecordData;
import cn.valorin.dueltime.event.cache.CacheInitializedEvent;
import cn.valorin.dueltime.util.UtilSync;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordCache {
    private final Map<String, List<BaseRecordData>> playerRecordMap = new HashMap<>();

    public void reload() {
        SqlSessionFactory sqlSessionFactory = DuelTimePlugin.getInstance().getMyBatisManager().getFactory(this.getClass());
        try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
            ClassicArenaRecordDataMapper mapper = sqlSession.getMapper(ClassicArenaRecordDataMapper.class);
            mapper.createTableIfNotExists();
            UtilSync.publishEvent(new CacheInitializedEvent(this.getClass()));
        }
    }

    /**
     * 载入某个玩家的记录数据到缓存中
     * 一般只有两种情况会被调用
     * 1.打开记录面板时，由ArenaRecordInventory的openFor方法的最开始调用
     * 2.比赛结束后留下比赛记录时，由本类的add方法调用
     * 意味着玩家想看记录面板时才载入记录缓存，减少不必要的缓存占用
     */
    public void reload(Player player) {
        String playerName = player.getName();
        if (playerRecordMap.containsKey(playerName)) {
            return;
        }
        try (SqlSession sqlSession = DuelTimePlugin.getInstance().getMyBatisManager().getFactory(this.getClass()).openSession()) {
            ClassicArenaRecordDataMapper mapper = sqlSession.getMapper(ClassicArenaRecordDataMapper.class);
            List<BaseRecordData> records = mapper.getAll(playerName);
            playerRecordMap.put(playerName, records);
            //向记录面板GUI管理器回传数据
            DuelTimePlugin.getInstance().getCustomInventoryManager().getArenaRecord().updateContentTotalNumber(records.size(), player);
        }
    }

    public List<BaseRecordData> get(String playerName) {
        return playerRecordMap.getOrDefault(playerName, new ArrayList<>());
    }

    /**
     * 考虑到性能因素，在加载缓存时，不会将所有玩家的比赛记录都加载进来
     * 每个玩家的BaseRecordData对象集合只会在玩家上线时加载
     * 但如果一定要获取某个玩家的BaseRecordData对象集合，那就需要调用这个方法
     *
     * @param playerName 玩家名
     * @return BaseRecordData对象集合，即所有比赛记录
     */
    public List<BaseRecordData> getAnyway(String playerName) {
        List<BaseRecordData> records = playerRecordMap.get(playerName);
        if (records == null) {
            //如果缓存中找不到这个玩家，再从数据库里找
            try (SqlSession sqlSession = DuelTimePlugin.getInstance().getMyBatisManager().getFactory(this.getClass()).openSession()) {
                List<BaseRecordData> recordsInDatabase = sqlSession.getMapper(ClassicArenaRecordDataMapper.class).getAll(playerName);
                if (recordsInDatabase == null) {
                    //如果数据库里也不存在这个玩家的记录数据，则确认不存在
                    return null;
                } else {
                    //如果数据库找到了这个玩家的记录数据，则获取
                    records = recordsInDatabase;
                }
            }
        }
        return records;
    }

    public void add(Player player, ClassicArenaRecordData recordData) {
        //先判断该玩家的比赛记录是否已经载入缓存
        reload(player);
        String playerName = player.getName();
        List<BaseRecordData> records = playerRecordMap.getOrDefault(playerName, new ArrayList<>());
        records.add(recordData);
        playerRecordMap.put(playerName, records);
        DuelTimePlugin.getInstance().getCustomInventoryManager().getArenaRecord().updateContentTotalNumber(records.size(), player);
        try (SqlSession sqlSession = DuelTimePlugin.getInstance().getMyBatisManager().getFactory(this.getClass()).openSession(true)) {
            sqlSession.getMapper(ClassicArenaRecordDataMapper.class).add(recordData);
        }
    }
}
