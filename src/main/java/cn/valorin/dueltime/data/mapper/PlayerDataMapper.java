package cn.valorin.dueltime.data.mapper;

import cn.valorin.dueltime.data.MyBatisManager;
import cn.valorin.dueltime.data.pojo.PlayerData;
import cn.valorin.dueltime.ranking.RankingData;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface PlayerDataMapper {
    @MapKey("id")
    Map<String, PlayerData> getMap();

    PlayerData get(String id);

    void insertOrUpdateSQLite(PlayerData playerData);

    void insertOrUpdateMySQL(PlayerData playerData);

    void createTableIfNotExists();

    List<RankingData> selectWinsRanking();

    List<RankingData> selectWinRateRanking();

    List<RankingData> selectTotalGameNumberRanking();

    List<RankingData> selectClassicGameNumberRanking();

    List<RankingData> selectTotalGameTimeRanking();

    List<RankingData> selectClassicGameTimeRanking();

    List<RankingData> selectExpRanking();
}
