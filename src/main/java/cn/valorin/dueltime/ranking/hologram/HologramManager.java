package cn.valorin.dueltime.ranking.hologram;

import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.cache.LocationCache;
import cn.valorin.dueltime.ranking.Ranking;
import cn.valorin.dueltime.ranking.RankingData;
import cn.valorin.dueltime.util.UtilFormat;
import cn.valorin.dueltime.yaml.message.Msg;
import cn.valorin.dueltime.yaml.message.MsgBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class HologramManager {
    private boolean hologramPluginExist = false;
    private HologramPluginType hologramPluginTypeUsed;
    private boolean isEnabled = false;
    private final Map<String, HologramInstance> hologramInstanceMap = new HashMap<>();

    public void enable() {
        //检查是否有全息图插件
        checkHologramPlugin();
        if (!hologramPluginExist) {
            return;
        }
        //加载所有全息图
        for (Ranking ranking : DuelTimePlugin.getInstance().getRankingManager().getRankings().values()) {
            create(ranking);
        }
        //确认初始化完毕
        isEnabled = true;
    }

    public void disable() {
        hologramInstanceMap.values().forEach(HologramInstance::destroy);
        hologramInstanceMap.clear();
    }

    public Map<String, HologramInstance> getHologramInstanceMap() {
        return hologramInstanceMap;
    }

    public void create(Ranking ranking) {
        LocationCache cache = DuelTimePlugin.getInstance().getCacheManager().getLocationCache();
        String id = ranking.getId();
        Location location = cache.get(id);
        if (location == null) {
            return;
        }
        hologramInstanceMap.put(id, new HologramInstance(location, id, getContent(ranking), ranking.getHologramItemType()));
    }

    public void destroy(String rankingId) {
        hologramInstanceMap.get(rankingId).destroy();
        hologramInstanceMap.remove(rankingId);
    }

    public void refresh(Ranking ranking) {
        hologramInstanceMap.get(ranking.getId()).refresh(getContent(ranking), ranking.getHologramItemType());
    }

    private List<String> getContent(Ranking ranking) {
        List<String> content = new ArrayList<>(MsgBuilder.gets(Msg.RANKING_HOLOGRAM_HEADING, null,
                ranking.getName(), ranking.getDescription()));
        int size = DuelTimePlugin.getInstance().getCfgManager().getConfig().getInt("Ranking.hologram.size");
        List<RankingData> dataList = ranking.getContent();
        for (int i = 0; i < Math.min(size, dataList.size()); i++) {
            RankingData data = ranking.getContent().get(i);
            content.add(MsgBuilder.get(Msg.RANKING_HOLOGRAM_BODY, null, false,
                    "" + (i + 1),
                    data.getPlayerName(),
                    UtilFormat.toString(data.getData()),
                    UtilFormat.toString(data.getExtraStr())));
        }
        content.addAll(MsgBuilder.gets(Msg.RANKING_HOLOGRAM_ENDING, null,
                ranking.getName(), ranking.getDescription()));
        return content;
    }

    public void move(String rankingId, Location location) {
        hologramInstanceMap.get(rankingId).move(location);
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    //检查全息插件
    private void checkHologramPlugin() {
        FileConfiguration config = DuelTimePlugin.getInstance().getCfgManager().getConfig();
        boolean hologramEnabled = config.getBoolean("Ranking.hologram.enabled");
        if (!hologramEnabled) return;
        String pluginNameInConfig = config.getString("Ranking.hologram.plugin");
        List<HologramPluginType> hologramPluginTypeInstalledList = new ArrayList<>();
        outer:
        for (HologramPluginType hologramPluginType : HologramPluginType.values()) {
            String[] pluginName = hologramPluginType.getPluginNames();
            for (String realName : pluginName) {
                if (Bukkit.getPluginManager().getPlugin(realName) != null) {
                    hologramPluginTypeInstalledList.add(hologramPluginType);
                    if (realName.equals(pluginNameInConfig)) {
                        hologramPluginTypeUsed = hologramPluginType;
                        hologramPluginExist = true;
                        break outer;
                    }
                }
            }
        }
        //如果遍历过后没有找到配置文件中所填写的全息插件，且装载有其他受支持的全息插件，则使用第一个检测到的
        if (!hologramPluginExist && !hologramPluginTypeInstalledList.isEmpty()) {
            hologramPluginTypeUsed = hologramPluginTypeInstalledList.get(0);
            hologramPluginExist = true;
        }
    }
}
