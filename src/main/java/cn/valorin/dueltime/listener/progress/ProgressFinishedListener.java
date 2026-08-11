package cn.valorin.dueltime.listener.progress;

import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.arena.ArenaManager;
import cn.valorin.dueltime.arena.ClassicArena;
import cn.valorin.dueltime.arena.base.BaseArenaData;
import cn.valorin.dueltime.arena.type.ArenaType;
import cn.valorin.dueltime.data.pojo.ClassicArenaData;
import cn.valorin.dueltime.event.progress.ProgressFinishedEvent;
import cn.valorin.dueltime.progress.Progress;
import cn.valorin.dueltime.progress.ProgressType;
import cn.valorin.dueltime.progress.Step;
import cn.valorin.dueltime.util.UtilFormat;
import cn.valorin.dueltime.util.UtilGeometry;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static cn.valorin.dueltime.progress.ProgressType.InternalType.*;
import static cn.valorin.dueltime.arena.type.ArenaType.FunctionInternalType.*;

public class ProgressFinishedListener implements Listener {
    private final Map<String, String> progressTypeToFunctionType = new HashMap<String, String>() {{
        put(ADD_FUNCTION_CLASSIC_TIME_LIMIT.getId(), CLASSIC_TIME_LIMIT.getId());
        put(ADD_FUNCTION_CLASSIC_COUNTDOWN.getId(), CLASSIC_COUNTDOWN.getId());
        put(ADD_FUNCTION_CLASSIC_INVENTORY_CHECK_KEYWORD.getId(), CLASSIC_INVENTORY_CHECK_KEYWORD.getId());
        put(ADD_FUNCTION_CLASSIC_INVENTORY_CHECK_TYPE.getId(), CLASSIC_INVENTORY_CHECK_TYPE.getId());
        put(ADD_FUNCTION_CLASSIC_PRE_GAME_COMMAND.getId(), CLASSIC_PRE_GAME_COMMAND.getId());
        put(ADD_FUNCTION_CLASSIC_SPECTATE.getId(), CLASSIC_SPECTATE.getId());
        put(ADD_FUNCTION_CLASSIC_BAN_ENTITY_SPAWN.getId(), CLASSIC_BAN_ENTITY_SPAWN.getId());
    }};

    @EventHandler
    public void onProgressFinishedToApplyData(ProgressFinishedEvent event) {
        Progress progress = event.getProgress();
        String progressId = progress.getId();
        Step[] steps = progress.getSteps();
        if (progressId.equals(CREATE_CLASSIC_ARENA.getId())) {
            //创建经典类型竞技场过程
            UtilGeometry.buildCubicLine(progress.getPlayer(),(Location) steps[0].getData(), (Location) steps[1].getData(), 0.3, 60, 179, 113);
            DuelTimePlugin.getInstance().getArenaManager().add(
                    new ClassicArena(
                            new ClassicArenaData(
                                    (String) steps[4].getData(),
                                    (String) steps[5].getData(),
                                    (Location) steps[0].getData(),
                                    (Location) steps[1].getData(),
                                    null,
                                    (Location) steps[2].getData(),
                                    (Location) steps[3].getData()
                            )
                    )
            );
        }
        if (progressTypeToFunctionType.containsKey(progressId)) {
            //为经典类型竞技场添加附加功能过程
            String arenaId = (String) progress.getData();
            ArenaManager arenaManager = DuelTimePlugin.getInstance().getArenaManager();
            BaseArenaData arenaData = arenaManager.get(arenaId).getArenaData();
            HashMap<String, Object[]> functions = arenaData.getFunctions();
            if (functions == null) {
                functions = new HashMap<>();
            }
            functions.put(progressTypeToFunctionType.get(progressId), Arrays.stream(steps).map(Step::getData).toArray());
            arenaData.setFunctions(functions);
            arenaManager.update(arenaData);
        }
    }
}
