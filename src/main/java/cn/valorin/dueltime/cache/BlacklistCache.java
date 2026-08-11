package cn.valorin.dueltime.cache;

import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.data.mapper.BlacklistMapper;
import cn.valorin.dueltime.event.cache.CacheInitializedEvent;
import cn.valorin.dueltime.util.UtilSync;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.ArrayList;
import java.util.List;

public class BlacklistCache {
    private List<String> blacklist = new ArrayList<>();

    public void reload() {
        SqlSessionFactory sqlSessionFactory = DuelTimePlugin.getInstance().getMyBatisManager().getFactory(this.getClass());
        try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
            BlacklistMapper mapper = sqlSession.getMapper(BlacklistMapper.class);
            mapper.createTableIfNotExists();
            blacklist = mapper.get();
            UtilSync.publishEvent(new CacheInitializedEvent(this.getClass()));
        }
    }

    public List<String> get() {
        return blacklist;
    }

    public void add(String playerName) {
        blacklist.add(playerName);
        try (SqlSession sqlSession = DuelTimePlugin.getInstance().getMyBatisManager().getFactory(this.getClass()).openSession(true)) {
            sqlSession.getMapper(BlacklistMapper.class).add(playerName);
        }
    }


    public void remove(String playerName) {
        blacklist.remove(playerName);
        try (SqlSession sqlSession = DuelTimePlugin.getInstance().getMyBatisManager().getFactory(this.getClass()).openSession(true)) {
            sqlSession.getMapper(BlacklistMapper.class).remove(playerName);
        }
    }

    public boolean contains(String playerName) {
        return blacklist.contains(playerName);
    }
}
