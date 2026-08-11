package cn.valorin.dueltime.listener.gui;

import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.arena.ArenaManager;
import cn.valorin.dueltime.arena.base.BaseArena;
import cn.valorin.dueltime.command.sub.CommandPermission;
import cn.valorin.dueltime.event.arena.ArenaTryToJoinEvent;
import cn.valorin.dueltime.gui.CustomInventoryManager;
import cn.valorin.dueltime.gui.MultiPageInventory;
import cn.valorin.dueltime.gui.StartInventory;
import cn.valorin.dueltime.viaversion.ViaVersion;
import cn.valorin.dueltime.yaml.message.Msg;
import cn.valorin.dueltime.yaml.message.MsgBuilder;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.HashMap;
import java.util.Map;

import static cn.valorin.dueltime.arena.base.BaseArena.State.*;

public class StartListener implements Listener {
    @EventHandler
    public void onChooseArena(InventoryClickEvent event) {
        CustomInventoryManager customInventoryManager = DuelTimePlugin.getInstance().getCustomInventoryManager();
        StartInventory startInventory = customInventoryManager.getStart();
        ArenaManager arenaManager = DuelTimePlugin.getInstance().getArenaManager();
        int totalIndex = startInventory.checkBeforeClickFunctionItem(event, arenaManager.size());
        if (totalIndex < MultiPageInventory.INDEX_THRESHOLD) {
            //不是点击的本插件面板里的功能内容
            return;
        }
        BaseArena arenaClicked = arenaManager.getList().get(totalIndex);
        Player player = (Player) event.getWhoClicked();
        if (arenaManager.getOf(player) != null) {
            MsgBuilder.send(Msg.GUI_TYPE_START_USE_WHILE_IN_GAME, player, arenaClicked.getName());
            return;
        }
        BaseArena.State state = arenaClicked.getState();
        if (state == WAITING) {
            BaseArena arenaWaited = arenaManager.getWaitingFor(player);
            if (arenaWaited != null && arenaWaited.getId().equals(arenaClicked.getId())) {
                //如果点击的是当前所等待的竞技场，则取消等待
                arenaManager.removeWaitingPlayer(player);
                MsgBuilder.send(Msg.ARENA_WAIT_STOP, player, arenaClicked.getName());
            } else {
                //如果要开始等待，或者想切换等待。为了防止频繁操作，这里添加了一个短暂的时间间隔约束
                if (!player.hasPermission(CommandPermission.ADMIN)) {
                    if (waitingOperationCooldown.getOrDefault(player.getName(), 0L) > System.currentTimeMillis()) {
                        MsgBuilder.send(Msg.GUI_TYPE_START_WAITING_OPERATION_COOLDOWN, player);
                        return;
                    }
                    waitingOperationCooldown.put(player.getName(), System.currentTimeMillis() + 1000);
                }
                arenaManager.addWaitingPlayer(player, arenaClicked.getId());
                Sound sound = ViaVersion.getSound("BLOCK_ANVIL_PLACE", "ANVIL_PLACE");
                if (sound != null) {
                    player.playSound(player.getLocation(), sound, 1, 0);
                }
            }
        } else if (state == IN_PROGRESS_CLOSED) {
            MsgBuilder.send(Msg.GUI_TYPE_START_STATE_IN_PROGRESS_CLOSED, player, arenaClicked.getName());
        } else if (state == IN_PROGRESS_OPENED) {
            if (arenaClicked.isFull()) {
                MsgBuilder.send(Msg.GUI_TYPE_START_STATE_IN_PROGRESS_OPENED_FULL, player, arenaClicked.getName());
            } else {
                MsgBuilder.send(Msg.GUI_TYPE_START_STATE_IN_PROGRESS_OPENED, player, arenaClicked.getName());
                arenaManager.join(player, arenaClicked.getId(), ArenaTryToJoinEvent.Way.GUI);
            }
        } else {
            MsgBuilder.send(Msg.GUI_TYPE_START_STATE_DISABLED, player, arenaClicked.getName());
        }
    }

    Map<String, Long> waitingOperationCooldown = new HashMap<>();
}
