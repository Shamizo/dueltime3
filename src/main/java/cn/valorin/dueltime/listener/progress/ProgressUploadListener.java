package cn.valorin.dueltime.listener.progress;

import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.progress.Progress;
import cn.valorin.dueltime.progress.ProgressType;
import cn.valorin.dueltime.yaml.message.Msg;
import cn.valorin.dueltime.yaml.message.MsgBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

public class ProgressUploadListener implements Listener {

    /**
     * 上传字符串、字符串列表、数字
     */
    @EventHandler
    public void onSendMessage(PlayerChatEvent event) {
        Player player = event.getPlayer();
        Progress progress = DuelTimePlugin.getInstance().getProgressManager().getProgress(player.getName());
        if (progress == null ||
                !progress.getId().equals(ProgressType.InternalType.ADD_FUNCTION_CLASSIC_INVENTORY_CHECK_KEYWORD.getId()) ||
                progress.getFinishedStep() != 0) {
            return;
        }
        if (progress.isPaused()) {
            return;
        }
        String enter = event.getMessage();
        if (!enter.equalsIgnoreCase("name") &&
                !enter.equalsIgnoreCase("lore") &&
                !enter.equalsIgnoreCase("all")) {
            MsgBuilder.send(Msg.PROGRESS_TYPE_SET_FUNCTION_INVENTORY_CHECK_KEYWORD_STEP_1_INCORRECT_RANGE,player,
                    enter);
        }
        progress.next(enter.toLowerCase());
    }
}