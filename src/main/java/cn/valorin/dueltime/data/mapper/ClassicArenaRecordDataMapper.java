package cn.valorin.dueltime.data.mapper;

import cn.valorin.dueltime.arena.base.BaseRecordData;
import cn.valorin.dueltime.data.pojo.ClassicArenaRecordData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ClassicArenaRecordDataMapper {
    List<BaseRecordData> getAll(@Param("playerName") String playerName);

    void add(ClassicArenaRecordData arenaRecordData);

    void createTableIfNotExists();
}
