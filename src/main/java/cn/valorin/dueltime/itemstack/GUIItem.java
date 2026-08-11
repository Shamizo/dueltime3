package cn.valorin.dueltime.itemstack;

import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.arena.ArenaManager;
import cn.valorin.dueltime.arena.base.BaseArena;
import cn.valorin.dueltime.arena.base.BaseArenaData;
import cn.valorin.dueltime.arena.base.BaseRecordData;
import cn.valorin.dueltime.arena.type.ArenaType;
import cn.valorin.dueltime.data.pojo.ShopRewardData;
import cn.valorin.dueltime.util.UtilItemBuilder;
import cn.valorin.dueltime.viaversion.ViaVersionItem;
import cn.valorin.dueltime.yaml.configuration.CfgManager;
import cn.valorin.dueltime.yaml.message.Msg;
import cn.valorin.dueltime.yaml.message.MsgBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GUIItem {
    public static ItemStack blackGlassPane = new UtilItemBuilder(ViaVersionItem.getGlassPaneType(15)).setDisplayName(" ").build();
    public static ItemStack whiteGlassPane = new UtilItemBuilder(ViaVersionItem.getGlassPaneType(0)).setDisplayName(" ").build();

    public static ItemStack getButtonLast(Player player) {
        return new UtilItemBuilder(Material.PAPER).setDisplayName(MsgBuilder.get(Msg.ITEM_GUI_SHOP_BUTTON_PREVIOUS_PAGE_NAME, player)).build();
    }

    public static ItemStack getButtonNext(Player player) {
        return new UtilItemBuilder(Material.PAPER).setDisplayName(MsgBuilder.get(Msg.ITEM_GUI_SHOP_BUTTON_NEXT_PAGE_NAME, player)).build();
    }

    public static ItemStack getShopReward(int index, Player player) {
        ShopRewardData rewardData = DuelTimePlugin.getInstance().getCacheManager().getShopCache().getList().get(index);
        ItemStack rewardItemStack = rewardData.getItemStack();
        int amount = rewardItemStack.getAmount();
        String displayName = null;
        List<String> lore = new ArrayList<>();
        ItemMeta rewardItemStackMeta = rewardItemStack.getItemMeta();
        Map<Enchantment, Integer> enchantmentMap = new HashMap<>();
        if (rewardItemStackMeta != null) {
            if (rewardItemStackMeta.hasDisplayName()) {
                displayName = rewardItemStackMeta.getDisplayName();
            }
            if (rewardItemStackMeta.hasLore()) {
                lore = rewardItemStackMeta.getLore();
            }
            if (rewardItemStackMeta.hasEnchants()) {
                enchantmentMap = rewardItemStackMeta.getEnchants();
            }
        }
        lore.addAll(
                MsgBuilder.gets(Msg.ITEM_GUI_SHOP_REWARD_INFORMATION, player,
                        "" + rewardData.getPoint(),
                        "" + rewardData.getTotalRedemptionVolume(),
                        (rewardData.getDescription() != null) ? rewardData.getDescription() : "-"));
        //获取等级
        int levelNow = DuelTimePlugin.getInstance().getLevelManager().getLevel(player.getName());
        int levelNeeded = rewardData.getLevelLimit();
        double pointNow = DuelTimePlugin.getInstance().getCacheManager().getPlayerDataCache().get(player.getName()).getPoint();
        double pointNeeded = rewardData.getPoint();
        String tip;
        if (levelNow < levelNeeded) {//优先提示等级不足，其次才是可能出现的积分不足
            tip = MsgBuilder.get(Msg.ITEM_GUI_SHOP_REWARD_REDEEM_TIP_NO_ENOUGH_LEVEL, player, "" + levelNow, "" + levelNeeded);
        } else {
            if (pointNow < pointNeeded) {
                tip = MsgBuilder.get(Msg.ITEM_GUI_SHOP_REWARD_REDEEM_TIP_NO_ENOUGH_POINT, player, "" + pointNow, "" + pointNeeded);
            } else {
                tip = MsgBuilder.get(Msg.ITEM_GUI_SHOP_REWARD_REDEEM_TIP_YES, player, "" + pointNow, "" + pointNeeded);
            }
        }
        lore.add(tip);
        if (levelNeeded != 0 && levelNow > levelNeeded) {
            lore.add(MsgBuilder.get(Msg.ITEM_GUI_SHOP_REWARD_LEVEL_LIMIT_TIP_YES, player, "" + levelNeeded));
        }
        return new UtilItemBuilder(rewardItemStack.getType())
                .setAmount(amount)
                .setDisplayName(displayName)
                .setLore(lore)
                .setEnchants(enchantmentMap)
                .build();
    }

    public static ItemStack getArenaRecord(int index, Player player) {
        BaseRecordData recordData = DuelTimePlugin.getInstance().getCacheManager().getArenaRecordCache().get(player.getName()).get(index);
        ItemStack itemStack = new ItemStack(ViaVersionItem.getMapMaterial(), 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(recordData.getItemStackTitle());
        List<String> lore = recordData.getItemStackContent();
        CfgManager cfgManager = DuelTimePlugin.getInstance().getCfgManager();
        //追加展示记录、打印记录的提示语
        boolean isArenaRecordShowEnabled = cfgManager.isRecordShowEnabled();
        boolean isArenaRecordPrintEnabled = cfgManager.isRecordPrintEnabled();
        if (isArenaRecordShowEnabled || isArenaRecordPrintEnabled) {
            lore.add("");
            if (isArenaRecordShowEnabled) {
                lore.add(MsgBuilder.get(Msg.ITEM_GUI_RECORD_SHOW_TIP, player));
            }
            if (isArenaRecordPrintEnabled) {
                lore.add(MsgBuilder.get(Msg.ITEM_GUI_RECORD_PRINT_TIP, player,
                        "" + cfgManager.getRecordPrintCost()));
            }
        }
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack getArenaInfo(String id, Player player) {
        ArenaManager arenaManager = DuelTimePlugin.getInstance().getArenaManager();
        BaseArena arena = arenaManager.get(id);
        BaseArenaData arenaData = arena.getArenaData();
        Object iconData = arena.getArenaType().getPresets().get(ArenaType.PresetType.START_ICON);
        ItemStack itemStack = new ItemStack(iconData != null ? (Material) iconData : Material.PAPER, 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(arenaData.getName());
        Msg stateMsg;
        Msg buttonMsg;
        String leftPlayerNumber = "" + arena.getGamerDataList().size();
        String rightPlayerNumber = arena.getArenaData().getMaxPlayerNumber() > 0 ? "" + arena.getArenaData().getMaxPlayerNumber() : "∞";
        switch (arena.getState()) {
            case WAITING:
                stateMsg = Msg.ITEM_GUI_START_ARENA_STATE_WAITING;
                if (arenaManager.getWaitingPlayers(id).contains(player.getName())) {
                    //如果此时竞技场处于等待状态且该玩家在这个竞技场的等待列表中，则顺便为物品添加一个附魔光效
                    buttonMsg = Msg.ITEM_GUI_START_ARENA_BUTTON_MESSAGE_WAITING_STOP;
                    itemMeta.addEnchant(Enchantment.LURE, 1, true);
                    if (ViaVersionItem.isHasItemFlagMethod()) {
                        itemMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
                        itemMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
                    }
                } else {
                    buttonMsg = Msg.ITEM_GUI_START_ARENA_BUTTON_MESSAGE_WAITING_START;
                }
                leftPlayerNumber = "" + arenaManager.getWaitingPlayers(id).size();
                rightPlayerNumber = "" + arena.getArenaData().getMinPlayerNumber();
                break;
            case IN_PROGRESS_CLOSED:
                stateMsg = Msg.ITEM_GUI_START_ARENA_STATE_IN_PROGRESS_CLOSED;
                buttonMsg = Msg.ITEM_GUI_START_ARENA_BUTTON_MESSAGE_IN_PROGRESS_CLOSED;
                break;
            case IN_PROGRESS_OPENED:
                stateMsg = Msg.ITEM_GUI_START_ARENA_STATE_IN_PROGRESS_OPENED;
                int maxPlayerNumber = arena.getArenaData().getMaxPlayerNumber();
                buttonMsg = maxPlayerNumber > 0 && arena.getGamerDataList().size() < maxPlayerNumber ? Msg.ITEM_GUI_START_ARENA_BUTTON_MESSAGE_IN_PROGRESS_OPENED : Msg.ITEM_GUI_START_ARENA_BUTTON_MESSAGE_IN_PROGRESS_OPENED_FULL;
                break;
            default: //DISABLED状态
                stateMsg = Msg.ITEM_GUI_START_ARENA_STATE_DISABLED;
                buttonMsg = Msg.ITEM_GUI_START_ARENA_BUTTON_MESSAGE_DISABLED;
        }
        itemMeta.setLore(MsgBuilder.gets(Msg.ITEM_GUI_START_ARENA_INFORMATION, player,
                MsgBuilder.get(stateMsg, player), leftPlayerNumber, rightPlayerNumber, MsgBuilder.get(buttonMsg, player)));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
