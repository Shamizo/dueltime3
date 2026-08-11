package cn.valorin.dueltime.listener.gui;

import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.cache.PlayerDataCache;
import cn.valorin.dueltime.cache.ShopCache;
import cn.valorin.dueltime.data.pojo.PlayerData;
import cn.valorin.dueltime.data.pojo.ShopRewardData;
import cn.valorin.dueltime.gui.CustomInventoryManager;
import cn.valorin.dueltime.gui.MultiPageInventory;
import cn.valorin.dueltime.gui.ShopInventory;
import cn.valorin.dueltime.viaversion.ViaVersion;
import cn.valorin.dueltime.yaml.message.Msg;
import cn.valorin.dueltime.yaml.message.MsgBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class ShopListener implements Listener {
    @EventHandler
    public void onRedeemInShop(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        String playerName = player.getName();
        ShopInventory shopInventory = DuelTimePlugin.getInstance().getCustomInventoryManager().getShop();
        ShopCache shopCache = DuelTimePlugin.getInstance().getCacheManager().getShopCache();
        int totalIndex = shopInventory.checkBeforeClickFunctionItem(event, shopCache.getList().size());
        if (totalIndex < MultiPageInventory.INDEX_THRESHOLD) {
            //不是点击的本插件面板里的功能内容
            return;
        }
        ShopRewardData rewardData = shopCache.getList().get(totalIndex);
        PlayerDataCache playerDataCache = DuelTimePlugin.getInstance().getCacheManager().getPlayerDataCache();
        PlayerData playerData = playerDataCache.get(playerName);
        int levelNeed = rewardData.getLevelLimit();
        int levelNow = DuelTimePlugin.getInstance().getLevelManager().getLevel(playerName);
        if (levelNow < levelNeed) {
            MsgBuilder.send(Msg.GUI_TYPE_SHOP_REDEEM_UNSUCCESSFULLY_INSUFFICIENT_LEVEL, player,
                    "" + levelNeed, "" + levelNow);
            return;
        }
        double pointNeeded = rewardData.getPoint();
        double pointNow = playerData.getPoint();
        if (pointNow < pointNeeded) {
            MsgBuilder.send(Msg.GUI_TYPE_SHOP_REDEEM_UNSUCCESSFULLY_INSUFFICIENT_POINTS, player,
                    "" + pointNeeded, "" + pointNow);
            return;
        }
        //扣除积分
        playerData.setPoint(pointNow - pointNeeded);
        playerDataCache.set(playerName, playerData);
        //发送奖励。如果背包已满则奖励物品会以掉落物的方式在玩家脚下呈现
        Map<Integer, ItemStack> itemUnfitMap = player.getInventory().addItem(rewardData.getItemStack());
        if (itemUnfitMap.isEmpty()) {
            MsgBuilder.send(Msg.GUI_TYPE_SHOP_REDEEM_SUCCESSFULLY, player);
        } else {
            MsgBuilder.send(Msg.GUI_TYPE_SHOP_REDEEM_SUCCESSFULLY_BUT_DROP, player,
                    "" + itemUnfitMap.size());
            World world = player.getWorld();
            Location location = player.getLocation();
            for (ItemStack itemUnfit : itemUnfitMap.values()) {
                world.dropItem(location, itemUnfit);
            }
        }
        //执行兑换后指令
        List<String> commands = rewardData.getCommands();
        if (commands != null) {
            for (String commandData : commands) {
                String commandExecutor = commandData.split(":")[0];
                String commandContent = commandData.substring(commandExecutor.length()+1)
                        .replace("{player}", playerName)
                        .replace("{point}", "" + pointNeeded);
                if (commandExecutor.equals("player")) {
                    Bukkit.dispatchCommand(player, commandContent);
                }
                if (commandExecutor.equals("op")) {
                    if (player.isOp()) {
                        Bukkit.dispatchCommand(player, commandContent);
                    } else {
                        player.setOp(true);
                        Bukkit.dispatchCommand(player, commandContent);
                        player.setOp(false);
                    }
                }
                if (commandExecutor.equals("console")) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandContent);
                }
            }
        }
        //播放音效
        player.playSound(player.getLocation(), ViaVersion
                .getSound("ENTITY_PLAYER_LEVELUP", "LEVELUP"), 1.0f, 1.0f);
        //更新销量
        rewardData.updateTotalRedemptionVolume();
        int[] loc = ShopCache.getLocByIndex(totalIndex);
        shopCache.set(loc[0], loc[1], loc[2], rewardData);
        //为所有浏览者实时刷新页面
        CustomInventoryManager customInventoryManager = DuelTimePlugin.getInstance().getCustomInventoryManager();
        customInventoryManager.updatePage(customInventoryManager.getShop());
    }
}
