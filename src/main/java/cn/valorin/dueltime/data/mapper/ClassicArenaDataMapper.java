package cn.valorin.dueltime.data.mapper;

import cn.valorin.dueltime.data.pojo.ClassicArenaData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ClassicArenaDataMapper {
    List<ClassicArenaData> getAll();

    void add(ClassicArenaData arenaData);

    void update(ClassicArenaData arenaData);

    void delete(@Param("id") String id);

    void createTableIfNotExists();
}
