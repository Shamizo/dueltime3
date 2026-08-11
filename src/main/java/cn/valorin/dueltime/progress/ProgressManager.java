package cn.valorin.dueltime.progress;

import cn.valorin.dueltime.event.progress.ProgressStartEvent;
import cn.valorin.dueltime.yaml.message.Msg;
import cn.valorin.dueltime.yaml.message.MsgBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ProgressManager {
    private final Map<String, Progress> progressMap = new HashMap<>();

    public Progress getProgress(String playerName) {
        return progressMap.get(playerName);
    }

    public void enter(Player player, Progress progress) {
        Progress nowProgress = progressMap.get(player.getName());
        if (nowProgress != null) {
            if (nowProgress.getId().equals(progress.getId())) {
                MsgBuilder.send(Msg.PROGRESS_REPEATEDLY_JOIN, player,
                        progress.getName());
            } else {
                MsgBuilder.send(Msg.PROGRESS_JOIN_WHILE_HANDLING_OTHER, player,
                        nowProgress.getName());
            }
            return;
        }
        progressMap.put(player.getName(), progress);
        MsgBuilder.send(Msg.PROGRESS_JOIN_SUCCESSFULLY, player,
                progress.getName());
        //发布事件
        Bukkit.getPluginManager().callEvent(new ProgressStartEvent(player, progress));
    }

    public void exit(String playerName) {
        progressMap.get(playerName).exit();
    }

    //仅供Progress内部调用
    public void cancel(String playerName) {
        progressMap.remove(playerName);
    }

    public void exitAll() {
        for (Progress progress : progressMap.values()) {
            progress.exit();
        }
    }
}

