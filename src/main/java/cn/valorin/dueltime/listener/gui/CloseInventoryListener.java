package cn.valorin.dueltime.listener.gui;

import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.gui.CustomInventoryHolder;
import cn.valorin.dueltime.gui.CustomInventoryManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class CloseInventoryListener implements Listener {
    @EventHandler
    public void removeViewerForClosingInventory(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof CustomInventoryHolder) {
            CustomInventoryManager manager = DuelTimePlugin.getInstance().getCustomInventoryManager();
            String playerName = event.getPlayer().getName();
            manager.getShop().removeViewer(playerName);
            manager.getStart().removeViewer(playerName);
        }
    }
}
