package cn.valorin.dueltime;

import cn.valorin.dueltime.arena.ArenaManager;
import cn.valorin.dueltime.arena.base.BaseArena;
import cn.valorin.dueltime.arena.type.ArenaTypeManager;
import cn.valorin.dueltime.cache.CacheManager;
import cn.valorin.dueltime.command.CommandHandler;
import cn.valorin.dueltime.data.MyBatisManager;
import cn.valorin.dueltime.gui.CustomInventoryManager;
import cn.valorin.dueltime.hook.DuelTimeExpansion;
import cn.valorin.dueltime.level.LevelManager;
import cn.valorin.dueltime.listener.ListenerManager;
import cn.valorin.dueltime.network.VersionChecker;
import cn.valorin.dueltime.progress.ProgressManager;
import cn.valorin.dueltime.ranking.RankingManager;
import cn.valorin.dueltime.ranking.hologram.HologramManager;
import cn.valorin.dueltime.request.RequestReceiverManager;
import cn.valorin.dueltime.stats.Metrics;
import cn.valorin.dueltime.viaversion.ViaVersion;
import cn.valorin.dueltime.yaml.configuration.CfgManager;
import cn.valorin.dueltime.yaml.message.MsgManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class DuelTimePlugin extends JavaPlugin {

    private static DuelTimePlugin instance;
    public static String serverVersion;
    public static int serverVersionInt;
    private CfgManager cfgManager;
    private MsgManager msgManager;
    private MyBatisManager myBatisManager;
    private CacheManager cacheManager;
    private ArenaTypeManager arenaTypeManager;
    private ArenaManager arenaManager;
    private CommandHandler commandHandler;
    private ProgressManager progressManager;
    private CustomInventoryManager customInventoryManager;
    private RequestReceiverManager requestReceiverManager;
    private LevelManager levelManager;
    private RankingManager rankingManager;
    private HologramManager hologramManager;
    private Metrics metrics;
    private VersionChecker versionChecker;

    @Override
    public void onEnable() {
        instance = this;
        cfgManager = new CfgManager();
        levelManager = new LevelManager();
        msgManager = new MsgManager();
        myBatisManager = new MyBatisManager();
        rankingManager = new RankingManager();
        hologramManager = new HologramManager();
        cacheManager = new CacheManager();
        cacheManager.load();
        arenaTypeManager = new ArenaTypeManager();
        arenaManager = new ArenaManager();
        commandHandler = new CommandHandler();
        progressManager = new ProgressManager();
        customInventoryManager = new CustomInventoryManager();
        requestReceiverManager = new RequestReceiverManager();
        // 先初始化所有管理器（确保 arenaManager 等已就绪），再注册事件监听器
        // 避免在 Luminol 等区域化多线程环境下，监听器在管理器初始化完成前接收到事件导致 NPE
        ListenerManager.register();
        //确认服务器版本，方便一些版本差异的讨论
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        serverVersion = packageName.substring(packageName.lastIndexOf('.') + 1);
        if (packageName.split("\\.").length >= 4) {
            //packageName形如org.bukkit.craftbukkit.v1_20_R1
            serverVersionInt = Integer.parseInt(serverVersion.contains("_") ? serverVersion.split("_")[1] : serverVersion.split("-")[0]);
            if (serverVersionInt == 8 || serverVersionInt == 9) {
                ViaVersion.getClassesForTitleAndAction();
            }
        } else {
            //packageName形如org.bukkit.craftbukkit
            try {
                serverVersionInt = Integer.parseInt(Bukkit.getVersion().split("\\.")[1]);
            } catch (NumberFormatException e) {
                //Bukkit.getVersion()形如1.21-66-99ae7bb
                serverVersionInt = Integer.parseInt(Bukkit.getVersion().split("-")[0].split("\\.")[1]);
            }
        }
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new DuelTimeExpansion(this).register();
        }
        metrics = new Metrics(this, 22633);
        // 版本检查功能已禁用
        versionChecker = new VersionChecker();
    }

    @Override
    public void onDisable() {
        // 防止在启动失败时出现空指针异常
        if (arenaManager != null) {
            for (BaseArena arena : arenaManager.getList()) {
                if (arena.getState().equals(BaseArena.State.IN_PROGRESS_OPENED) || arena.getState().equals(BaseArena.State.IN_PROGRESS_CLOSED)) {
                    arenaManager.stop(arena.getId(), null);
                }
            }
        }
        
        if (hologramManager != null) {
            hologramManager.disable();
        }
        
        if (cacheManager != null && cacheManager.getPlayerDataCache() != null
                && cacheManager.getPlayerDataCache().getRefreshRankingTimer() != null) {
            cacheManager.getPlayerDataCache().getRefreshRankingTimer().cancel();
        }
        
        if (myBatisManager != null) {
            myBatisManager.closeConnection();
        }
        
        if (progressManager != null) {
            progressManager.exitAll();
        }
    }

    public static DuelTimePlugin getInstance() {
        return instance;
    }

    public CfgManager getCfgManager() {
        return cfgManager;
    }

    public MsgManager getMsgManager() {
        return msgManager;
    }

    public MyBatisManager getMyBatisManager() {
        return myBatisManager;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public ArenaTypeManager getArenaTypeManager() {
        return arenaTypeManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    public ProgressManager getProgressManager() {
        return progressManager;
    }

    public CustomInventoryManager getCustomInventoryManager() {
        return customInventoryManager;
    }

    public RequestReceiverManager getRequestReceiverManager() {
        return requestReceiverManager;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public RankingManager getRankingManager() {
        return rankingManager;
    }

    public HologramManager getHologramManager() {
        return hologramManager;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public VersionChecker getVersionChecker() {
        return versionChecker;
    }
}
