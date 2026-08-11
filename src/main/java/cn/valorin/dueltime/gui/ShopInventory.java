package cn.valorin.dueltime.gui;

import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.itemstack.GUIItem;
import cn.valorin.dueltime.yaml.message.Msg;
import cn.valorin.dueltime.yaml.message.MsgBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ShopInventory extends MultiPageInventory {

    public ShopInventory() {
        super(Type.SHOP, Msg.GUI_TYPE_SHOP_TITLE,
                new HashMap<ItemStack, int[]>() {{
                    put(GUIItem.blackGlassPane, new int[]{1, 2, 3, 4, 5, 6, 7, 9, 17, 18, 26, 27, 35, 36, 44, 46, 47, 49, 51, 52});
                    put(GUIItem.whiteGlassPane, new int[]{0, 8, 45, 53});
                }},
                new int[]{11, 12, 13, 14, 15, 20, 21, 22, 23, 24, 29, 30, 31, 32, 33, 38, 39, 40, 41, 42},
                20);
        //初始化时，拉取商品总数
        updateContentTotalNumber(DuelTimePlugin.getInstance().getCacheManager().getShopCache().getList().size());
    }

    public void loadContent(Player player, Inventory inventory, int page) {
        //检查当前页码是否超过最大值
        if (page > getMaxPage()) {
            /*
            如果页码超过最大值，则将页码设为最大值，即最后一页
            这种情况的一般触发条件：
            玩家浏览最后一页后关闭了这个GUI，随后管理员删除了一些内容物，使页码总数发生变化，最后一页的页码数自然也发生变化
            当玩家再次打开这个GUI时，会根据pageMap中储存的浏览页数而打开原先的最后一页，那么这时候页码就会超过现在的最大页码
             */
            page = getMaxPage();
        }
        //获取当前页码的物品数量
        int contentNumberInThisPage = Math.min(20, getContentTotalNumber() - (page - 1) * 20);
        //将当前页码的物品依次安置
        for (int i = 0; i < contentNumberInThisPage; i++) {
            //根据迭代序号获取slot序号
            int contentSlot = getContentSlots()[i];
            //获取要安置的物品
            ItemStack itemStack = GUIItem.getShopReward((page - 1) * 20 + i, player);
            inventory.setItem(contentSlot, itemStack);
        }
        //如果当前浏览的是最后一页，考虑到最后一页可能不会填满20个内容槽，所以要将无需安置物品的内容槽清空
        if (page == getMaxPage()) {
            for (int i = contentNumberInThisPage; i < 20; i++) {
                int contentSlot = getContentSlots()[i];
                inventory.setItem(contentSlot, null);
            }
        }
    }

    public void openFor(Player player) {
        if (getMaxPage() == 0) {
            MsgBuilder.send(Msg.GUI_TYPE_SHOP_EMPTY, player);
            return;
        }
        //创建Inventory容器
        Inventory inventory = Bukkit.createInventory(new CustomInventoryHolder(Type.SHOP), 54, MsgBuilder.get(getTitleMsg(), player));
        //布设装饰用的物品
        for (Map.Entry<ItemStack, int[]> kv : getDecorateSlotMap().entrySet()) {
            ItemStack decorateItemStack = kv.getKey();
            int[] slots = kv.getValue();
            for (int slot : slots) {
                inventory.setItem(slot, decorateItemStack);
            }
        }
        //安置翻页按钮
        inventory.setItem(48, GUIItem.getButtonLast(player));
        inventory.setItem(50, GUIItem.getButtonNext(player));
        //根据玩家在缓存中的浏览页数，加载内容物
        String playerName = player.getName();
        int nowPage;
        if (getPageMap().containsKey(playerName)) {
            nowPage = getPageMap().get(playerName);
        } else {
            //如果用来储存玩家当前浏览页码的pageMap缓存中没有该玩家，则添加缓存
            nowPage = 1;
            getPageMap().put(playerName, 1);
        }
        loadContent(player, inventory, nowPage);
        player.openInventory(inventory);
        addViewer(playerName);
    }
}