package cn.valorin.dueltime.gui;

import cn.valorin.dueltime.DuelTimePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CustomInventoryManager {
    private final ShopInventory shopInventory;
    private final ArenaRecordInventory arenaRecordInventory;
    private final StartInventory startInventory;

    public CustomInventoryManager() {
        shopInventory = new ShopInventory();
        arenaRecordInventory = new ArenaRecordInventory();
        startInventory = new StartInventory();
    }

    public ShopInventory getShop() {
        return shopInventory;
    }

    public ArenaRecordInventory getArenaRecord() {
        return arenaRecordInventory;
    }

    public StartInventory getStart() {
        return startInventory;
    }

    public void updatePage(MultiPageInventory multiPageInventory) {
        for (String viewerName : multiPageInventory.getViewers()) {
            Player viewer = Bukkit.getPlayerExact(viewerName);
            if (viewer != null) {
                multiPageInventory.loadContent(viewer, viewer.getOpenInventory().getTopInventory(), multiPageInventory.getPlayerPage(viewer.getName()));
            }
        }
    }
}
