package cn.valorin.dueltime.listener.cache;

import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.cache.LocationCache;
import cn.valorin.dueltime.event.cache.CacheInitializedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CacheInitializedListener implements Listener {
    @EventHandler
    public void onLocationCacheInit(CacheInitializedEvent e) {
        if (e.getClazz().equals(LocationCache.class)) {
            DuelTimePlugin.getInstance().getHologramManager().enable();
        }
    }
}
