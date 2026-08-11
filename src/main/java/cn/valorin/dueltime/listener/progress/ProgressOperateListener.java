package cn.valorin.dueltime.listener.progress;

import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.progress.Progress;
import cn.valorin.dueltime.progress.ProgressManager;
import cn.valorin.dueltime.progress.Step;
import cn.valorin.dueltime.util.UtilFormat;
import cn.valorin.dueltime.viaversion.ViaVersion;
import cn.valorin.dueltime.yaml.message.Msg;
import cn.valorin.dueltime.yaml.message.MsgBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;

import java.util.ArrayList;
import java.util.List;

public class ProgressOperateListener implements Listener {
    /**
     * 暂停、退出、撤回步骤
     */
    @EventHandler
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        ProgressManager progressManager = DuelTimePlugin.getInstance().getProgressManager();
        Progress progress = progressManager.getProgress(player.getName());
        if (progress == null) {
            return;
        }
        String message = event.getMessage();
        if (message.equalsIgnoreCase("-r") || message.equalsIgnoreCase("-reverse")) {
            event.setCancelled(true);
            if (progress.getFinishedStep() == 0) {
                MsgBuilder.send(Msg.PROGRESS_OPERATION_REVERSE_THE_FIRST_STEP, player);
                return;
            }
            progress.reverse();
            MsgBuilder.sendTitle(Msg.PROGRESS_OPERATION_REVERSE_TITLE, Msg.PROGRESS_OPERATION_REVERSE_SUBTITLE, 0, 30, 5, player, ViaVersion.TitleType.SUBTITLE);
            return;
        }
        if (message.equalsIgnoreCase("-p") || message.equalsIgnoreCase("-pause")) {
            event.setCancelled(true);
            if (progress.isPaused()) {
                MsgBuilder.send(Msg.PROGRESS_OPERATION_PAUSE_HAS_BEEN_PAUSED, player);
                return;
            }
            progress.setPaused(true);
            MsgBuilder.sendTitle(Msg.PROGRESS_OPERATION_PAUSE_TITLE, Msg.PROGRESS_OPERATION_PAUSE_SUBTITLE, 0, 30, 5, player, ViaVersion.TitleType.SUBTITLE);
            return;
        }
        if (message.equalsIgnoreCase("-c") || message.equalsIgnoreCase("-continue")) {
            event.setCancelled(true);
            if (!progress.isPaused()) {
                MsgBuilder.send(Msg.PROGRESS_OPERATION_CONTINUE_HAS_BEEN_PAUSED, player);
                return;
            }
            progress.setPaused(false);
            MsgBuilder.sendTitle(Msg.PROGRESS_OPERATION_CONTINUE_TITLE, Msg.PROGRESS_OPERATION_CONTINUE_SUBTITLE, 0, 30, 5, player, ViaVersion.TitleType.SUBTITLE);
            return;
        }
        if (message.equalsIgnoreCase("-e") || message.equalsIgnoreCase("-exit")) {
            event.setCancelled(true);
            progressManager.exit(player.getName());
            MsgBuilder.send(Msg.PROGRESS_OPERATION_EXIT_SUCCESSFULLY, player, progress.getName());
        }
        if (message.equalsIgnoreCase("-l") || message.equalsIgnoreCase("-list")) {
            event.setCancelled(true);
            if (!progress.getNowStep().getDataType().equals(List.class)) {
                MsgBuilder.send(Msg.PROGRESS_OPERATION_LIST_INCORRECT_DATA_TYPE, player, progress.getName());
                return;
            }
            List<String> listEntered = ProgressAutoUploadListener.listEnteredCache.getOrDefault(player.getName(),new ArrayList<>());
            if (listEntered.isEmpty() && !progress.getNowStep().hasAutoUploadTags(Step.AutoUploadTag.LIST_CONDITION_NULLABLE)) {
                MsgBuilder.send(Msg.PROGRESS_OPERATION_LIST_EMPTY_DATA, player, progress.getName());
                return;
            }
            progress.next(listEntered);
            ProgressAutoUploadListener.listEnteredCache.remove(player.getName());
        }
    }
}
