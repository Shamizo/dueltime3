package cn.valorin.dueltime.listener.progress;

import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.event.progress.ProgressStartEvent;
import cn.valorin.dueltime.progress.Progress;
import cn.valorin.dueltime.util.UtilGeometry;
import cn.valorin.dueltime.util.SchedulerUtil;
import cn.valorin.dueltime.yaml.message.Msg;
import cn.valorin.dueltime.yaml.message.MsgBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static cn.valorin.dueltime.progress.ProgressType.InternalType.ADD_FUNCTION_CLASSIC_SPECTATE;
import static cn.valorin.dueltime.progress.ProgressType.InternalType.CREATE_CLASSIC_ARENA;

public class ProgressStartListener implements Listener {
    @EventHandler
    public void onProgressStart(ProgressStartEvent event) {
        Progress progress = event.getProgress();
        Player player = progress.getPlayer();
        if (progress.isBossBarUsed()) {
            progress.initBossBar(BarColor.GREEN, BarStyle.SOLID);
        } else {
            MsgBuilder.sends(Msg.PROGRESS_BOSSBAR_FREE_TIP, player, false,
                    progress.getName(), "" + 0, "" + progress.getSteps().length,
                    "0 1",
                    progress.getSteps()[0].getTip());
        }
        if (progress.getId().equals(CREATE_CLASSIC_ARENA.getId()) || progress.getId().equals(ADD_FUNCTION_CLASSIC_SPECTATE.getId())) {
            progress.setTimer(SchedulerUtil.runTaskTimerAsync(() -> {
                //第二步执行完毕开始（即A、B点都选好后，开始绘线）
                if (progress.getFinishedStep() >= 2) {
                    Location locationDiagonalA = (Location) progress.getSteps()[0].getData();
                    Location locationDiagonalB = (Location) progress.getSteps()[1].getData();
                    int[] colors = progress.isPaused() ? new int[]{255, 255, 0} : new int[]{72, 209, 204};
                    UtilGeometry.buildCubicLine(player, locationDiagonalA, locationDiagonalB, 0.3, colors[0], colors[1], colors[2]);
                }
            }, 20, 20));
        }
    }
}
