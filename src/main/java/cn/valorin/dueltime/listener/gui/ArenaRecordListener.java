package cn.valorin.dueltime.listener.gui;

import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.arena.base.BaseRecordData;
import cn.valorin.dueltime.cache.RecordCache;
import cn.valorin.dueltime.cache.PlayerDataCache;
import cn.valorin.dueltime.data.pojo.PlayerData;
import cn.valorin.dueltime.gui.ArenaRecordInventory;
import cn.valorin.dueltime.gui.MultiPageInventory;
import cn.valorin.dueltime.viaversion.ViaVersionItem;
import cn.valorin.dueltime.yaml.configuration.CfgManager;
import cn.valorin.dueltime.yaml.message.Msg;
import cn.valorin.dueltime.yaml.message.MsgBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ArenaRecordListener implements Listener {
    @EventHandler
    public void onViewArenaRecord(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        String playerName = player.getName();
        ArenaRecordInventory recordInventory = DuelTimePlugin.getInstance().getCustomInventoryManager().getArenaRecord();
        RecordCache cache = DuelTimePlugin.getInstance().getCacheManager().getArenaRecordCache();
        int totalIndex = recordInventory.checkBeforeClickFunctionItem(event, cache.get(playerName).size());
        if (totalIndex < MultiPageInventory.INDEX_THRESHOLD) {
            //不是点击的本插件面板里的功能内容
            return;
        }
        BaseRecordData recordData = cache.get(playerName).get(totalIndex);
        CfgManager cfgManager = DuelTimePlugin.getInstance().getCfgManager();
        if (event.getClick().equals(ClickType.LEFT) && cfgManager.isRecordShowEnabled()) {
            if (!recordInventory.isShowAvailable(playerName)) {
                MsgBuilder.send(Msg.GUI_TYPE_RECORD_SHOW_FREQUENTLY, player, "" + recordInventory.getCooldownLeft(playerName));
                return;
            }
            recordInventory.updateShowCooldown(playerName);
            for (TextComponent textComponent : MsgBuilder.getClickable(Msg.GUI_TYPE_RECORD_SHOW_CONTENT, player,false,
                    playerName,
                    String.join("||", recordData.getItemStackContent()))) {
                Bukkit.spigot().broadcast(textComponent);
            }
        }
        if (event.getClick().equals(ClickType.RIGHT) && cfgManager.isRecordPrintEnabled()) {
            ItemStack itemInHand = ViaVersionItem.getItemInMainHand(player);
            if (itemInHand == null || !itemInHand.getType().equals(Material.PAPER)) {
                MsgBuilder.send(Msg.GUI_TYPE_RECORD_PRINT_FAIL_NO_PAPER_IN_HAND, player);
                return;
            }
            if (itemInHand.hasItemMeta()) {
                MsgBuilder.send(Msg.GUI_TYPE_RECORD_PRINT_FAIL_PAPER_HAS_META, player);
                return;
            }
            PlayerDataCache playerDataCache = DuelTimePlugin.getInstance().getCacheManager().getPlayerDataCache();
            PlayerData playerData = playerDataCache.get(playerName);
            double pointNeed = cfgManager.getRecordPrintCost();
            double pointNow = playerDataCache.get(playerName).getPoint();
            if (pointNow < pointNeed) {
                MsgBuilder.send(Msg.GUI_TYPE_RECORD_PRINT_INSUFFICIENT_POINT, player,
                        "" + pointNeed, "" + pointNow);
                return;
            }
            //执行积分消耗
            playerData.setPoint(pointNow - pointNeed);
            playerDataCache.set(playerName, playerData);
            //实现记录打印
            ItemMeta itemMetaInHand = itemInHand.getItemMeta();
            itemMetaInHand.setDisplayName(recordData.getItemStackTitle());
            itemMetaInHand.setLore(recordData.getItemStackContent());
            itemInHand.setItemMeta(itemMetaInHand);
            ViaVersionItem.setItemInMainHand(player, itemInHand);
            MsgBuilder.send(Msg.GUI_TYPE_RECORD_PRINT_SUCCESSFULLY, player);
        }
    }
}
