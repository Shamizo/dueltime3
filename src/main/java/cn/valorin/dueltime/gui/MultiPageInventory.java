package cn.valorin.dueltime.gui;

import cn.valorin.dueltime.yaml.message.Msg;
import cn.valorin.dueltime.yaml.message.MsgBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class MultiPageInventory {
    private final Type type;
    private final Msg titleMsg;
    private final Map<ItemStack, int[]> decorateSlotMap;
    private final int[] contentSlots;
    private final Map<String, Integer> pageMap = new HashMap<>();
    private final int contentPageSize;
    private int contentTotalNumber;
    private int maxPage;
    private final List<String> viewers = new ArrayList<>();

    protected MultiPageInventory(Type type, Msg titleMsg, Map<ItemStack, int[]> decorateSlotMap, int[] contentSlots, int contentPageSize) {
        this.type = type;
        this.titleMsg = titleMsg;
        this.decorateSlotMap = decorateSlotMap;
        this.contentSlots = contentSlots;
        this.contentPageSize = contentPageSize;
    }

    /**
     * 接收来自缓存系统的更新通知
     * 更新内容物品总数量，并根据内容物品总数量计算当前能划分的最大页码
     *
     * @param contentTotalNumber 缓存中内容物品的总数量
     */
    public void updateContentTotalNumber(int contentTotalNumber) {
        this.contentTotalNumber = contentTotalNumber;
        //页码的最大值以兑换物总数为依据，通过向上取整的方式计算。这里的contentPageSize即单页的容量
        this.maxPage = (int) Math.ceil((double) contentTotalNumber / contentPageSize);
    }

    /**
     * 根据页码加载对应的内容物品
     *
     * @param page 页码（从1开始记，与生活常识一致）
     */
    public abstract void loadContent(Player player, Inventory inventory, int page);

    /**
     * 为某个玩家打开商城面板
     */
    public abstract void openFor(Player player);

    public Msg getTitleMsg() {
        return titleMsg;
    }

    public Map<ItemStack, int[]> getDecorateSlotMap() {
        return decorateSlotMap;
    }

    public int[] getContentSlots() {
        return contentSlots;
    }

    public Map<String, Integer> getPageMap() {
        return pageMap;
    }

    public int getContentPageSize() {
        return contentPageSize;
    }

    public int getContentTotalNumber() {
        return contentTotalNumber;
    }

    public int getMaxPage() {
        return maxPage;
    }

    public int getPlayerPage(String playerName) {
        return pageMap.getOrDefault(playerName, 1);
    }

    public void updatePlayerPage(String playerName, int page) {
        pageMap.put(playerName, page);
    }

    public void addViewer(String playerName) {
        viewers.add(playerName);
    }

    public void removeViewer(String playerName) {
        viewers.remove(playerName);
    }

    public List<String> getViewers() {
        return viewers;
    }

    public static final int INDEX_THRESHOLD = -10; //在checkBeforeClickContent方法中，用于区分返回值为负值时，具体情况是点击了非内容区功能物品([-10,-1])还是其余情况((-∞,-11])

    public int checkBeforeClickFunctionItem(InventoryClickEvent event, int realSize) {
        Player player = (Player) event.getWhoClicked();
        InventoryHolder inventoryHolder = event.getInventory().getHolder();
        if (!(inventoryHolder instanceof CustomInventoryHolder) || ((CustomInventoryHolder) inventoryHolder).getType() != type) {
            //用holder判定代替title字符串判定，有利于减少漏洞
            return -11;
        }
        //识别为本插件的特定面板后，取消点击事件
        event.setCancelled(true);
        int[] contentSlots = getContentSlots();
        int slotClicked = event.getSlot();
        int index = -1; //当前点击的槽序号对应本页内容区的第几个物品
        for (int i = 0; i < contentSlots.length; i++) {
            if (contentSlots[i] == slotClicked) {
                index = i; // 找到了，将索引值赋给index
                break; // 找到后跳出循环
            }
        }
        String playerName = player.getName();
        int nowPage = getPlayerPage(playerName);
        int maxPage = (int) Math.ceil(realSize / (double) getContentSlots().length);
        //若未找到索引，说明点击的不是内容区
        if (index == -1) {
            Inventory inventory = event.getInventory();
            //点击的是前往上一页的按钮
            if (slotClicked == 48) {
                if (nowPage == 1) {
                    MsgBuilder.send(Msg.GUI_ALREADY_THE_FIRST_PAGE, player);
                    return -12;
                }
                loadContent(player, inventory, nowPage - 1);
                updatePlayerPage(playerName, nowPage - 1);
                return -13;
            }
            //点击的是前往下一页的按钮
            if (slotClicked == 50) {
                if (nowPage >= maxPage) {
                    MsgBuilder.send(Msg.GUI_ALREADY_THE_LAST_PAGE, player);
                    return -14;
                }
                loadContent(player, inventory, nowPage + 1);
                updatePlayerPage(playerName, nowPage + 1);
                return -15;
            }
            //如果点击的既不是内容区的物品，又不是按钮，则return
            return -16;
        } else if (index + 1 > realSize) {
            /*
            如果点击的槽位对应的总序号超出值域，则return
            这种情况一般发生在点击最后一页内容区的空白槽中
             */
            return -17;
        }
        return index + (pageMap.get(player.getName())-1) * contentPageSize;
    }

    public enum Type {
        START, SHOP, ARENA_RECORD
    }
}
