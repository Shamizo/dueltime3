package cn.valorin.dueltime.listener;

import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.listener.arena.BaseArenaListener;
import cn.valorin.dueltime.listener.arena.ClassicArenaListener;
import cn.valorin.dueltime.listener.cache.ArenaStateMonitorListener;
import cn.valorin.dueltime.listener.cache.CacheInitializedListener;
import cn.valorin.dueltime.listener.cache.JoinServerForArenaCheckListener;
import cn.valorin.dueltime.listener.cache.JoinServerForLoadingPlayerDataCacheListener;
import cn.valorin.dueltime.listener.chat.ChatListener;
import cn.valorin.dueltime.listener.gui.*;
import cn.valorin.dueltime.listener.network.CheckVersionListener;
import cn.valorin.dueltime.listener.wait.*;
import cn.valorin.dueltime.listener.progress.*;
import cn.valorin.dueltime.listener.ranking.TryToRefreshRankingListener;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class ListenerManager {
    public static void register() {
        Listener[] listeners = {
                new ShopListener(),
                new JoinServerForLoadingPlayerDataCacheListener(),
                new JoinServerForArenaCheckListener(),
                new ProgressAutoUploadListener(),
                new ProgressUploadListener(),
                new ProgressOperateListener(),
                new ProgressStartListener(),
                new ProgressFinishedListener(),
                new TryToRefreshRankingListener(),
                new ClassicArenaListener(),
                new ArenaRecordListener(),
                new ChatListener(),
                new StartListener(),
                new WaitingListener(),
                new CloseInventoryListener(),
                new BaseArenaListener(),
                new SimpleGUIListener(),
                new CacheInitializedListener(),
                new CheckVersionListener(),
                new ArenaStateMonitorListener(),
        };
        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener,
                    DuelTimePlugin.getInstance());
        }
    }
}
