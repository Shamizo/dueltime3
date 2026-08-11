package cn.valorin.dueltime.arena.base;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class BaseRecordData {
    private final String playerName;
    private final String arenaId;
    private final String date;

    public BaseRecordData(String playerName, String arenaId,String date) {
        this.playerName = playerName;
        this.arenaId = arenaId;
        this.date = date;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(playerName);
    }

    public String getArenaId() {
        return arenaId;
    }

    public String getDate() {
        return date;
    }

    //获取在记录面板中用于展示比赛记录详情的物品的名称
    public abstract String getItemStackTitle();

    //获取在记录面板中用于展示比赛记录详情的物品的lore
    public abstract List<String> getItemStackContent();
}
