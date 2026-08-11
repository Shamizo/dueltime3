package cn.valorin.dueltime.level;

import cn.valorin.dueltime.DuelTimePlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

/**
 * 管理配置文件中对升级公式的定义、对等级展示名的定义、玩家等级与对应展示名的缓存
 */
public class LevelManager {
    private final Map<String, Integer> levelCacheMap = new HashMap<>();
    private final List<Tier> tiers = new ArrayList<>();
    private String defaultTitle;
    private final String DEFAULT_ID = "default";
    private final Map<String, Tier> tierCacheMap = new HashMap<>();

    public LevelManager() {
        reloadSettings();
    }

    public int getLevel(String playerName, double exp) {
        if (!tierCacheMap.containsKey(playerName)) {
            load(playerName, exp);
        }
        return levelCacheMap.get(playerName);
    }

    public int getLevel(String playerName) {
        return levelCacheMap.get(playerName);
    }

    /**
     * 根据某个玩家的经验计算出对应的等级、段位并载入/更新到本类的缓存，一般用于玩家进服、PlayerDataCache更新且触发经验变动时调用
     *
     * @param playerName 玩家名
     * @param exp        玩家的经验
     */
    public void load(String playerName, double exp) {
        int originalLevel = levelCacheMap.getOrDefault(playerName, -1);
        int level = calculateLevel(exp);
        levelCacheMap.put(playerName, level);
        if (originalLevel == -1 || originalLevel != level) {
            //如果玩家原先不在缓存中、或者在缓存中且检测到了等级变化，则计算段位名
            Tier tier = calculateTier(level);
            tierCacheMap.put(playerName, tier);
        }
    }

    public int calculateLevel(double exp) {
        int currentLevel = 0;
        double totalExpNeeded = 0;
        for (int i = 0; i < tiers.size(); i++) {
            Tier currentTier = tiers.get(i);
            double expForThisTier = currentTier.getExpForLevelUp() * (i == tiers.size() - 1 ?
                    Double.MAX_VALUE :
                    tiers.get(i + 1).getLevel() - currentTier.getLevel());
            if (exp < totalExpNeeded + expForThisTier) {
                currentLevel += (int) ((exp - totalExpNeeded) / currentTier.getExpForLevelUp());
                break;
            }
            totalExpNeeded += expForThisTier;
            currentLevel = tiers.get(i + 1).getLevel();
        }
        return currentLevel;
    }

    public Tier calculateTier(int level) {
        Tier tier = tiers.get(0);
        for (Tier nowTier : tiers) {
            if (level < nowTier.getLevel()) {
                break;
            }
            tier = nowTier;
        }
        return tier;
    }

    public double calculateRemainingExpForLevelUp(double exp) {
        double totalExpNeeded = 0;
        for (int i = 0; i < tiers.size(); i++) {
            Tier currentTier = tiers.get(i);
            double expForThisTier = currentTier.getExpForLevelUp() * (i == tiers.size() - 1 ?
                    Double.MAX_VALUE :
                    tiers.get(i + 1).getLevel() - currentTier.getLevel());
            if (exp < totalExpNeeded + expForThisTier) {
                double expForLevelUp = currentTier.getExpForLevelUp();
                exp -= totalExpNeeded;
                exp %= expForLevelUp;
                return expForLevelUp - exp;
            }
            totalExpNeeded += expForThisTier;
        }
        return 0;
    }

    public double calculateLevelUpProgress(double exp) {
        double totalExpNeeded = 0;
        for (int i = 0; i < tiers.size(); i++) {
            Tier currentTier = tiers.get(i);
            double expForThisTier = currentTier.getExpForLevelUp() * (i == tiers.size() - 1 ?
                    Double.MAX_VALUE :
                    tiers.get(i + 1).getLevel() - currentTier.getLevel());
            if (exp < totalExpNeeded + expForThisTier) {
                double expForLevelUp = currentTier.getExpForLevelUp();
                exp -= totalExpNeeded;
                exp %= expForLevelUp;
                return exp / expForLevelUp;
            }
            totalExpNeeded += expForThisTier;
        }
        return 1;
    }

    /**
     * 从缓存中提取段位
     *
     * @param exp        可能传入的经验值，用于将不在缓存中的玩家载入缓存
     * @param playerName 玩家名
     * @return 玩家对应的段位
     */
    public Tier getTier(String playerName, double exp) {
        if (!tierCacheMap.containsKey(playerName)) {
            load(playerName, exp);
        }
        return tierCacheMap.get(playerName);
    }


    /**
     * 从缓存中直接提取段位，但调用时要保证玩家在本类的缓存中
     */
    public Tier getTier(String playerName) {
        return tierCacheMap.get(playerName);
    }

    public List<Tier> getTiers() {
        return tiers;
    }

    public Tier getDefaultTier() {
        return tiers.get(0);
    }

    /**
     * 从config.yml中载入对单挑等级展示名的设置
     */
    public void reloadSettings() {
        FileConfiguration config = DuelTimePlugin.getInstance().getCfgManager().getConfig();//直接获取instance里的getConfig()在低版本可能会乱码
        defaultTitle = config.getString("Level.tier.default.title").replace("&", "§");
        int defaultExpForLevelUp = config.getInt("Level.tier.default.exp-for-level-up");
        tiers.add(new Tier(DEFAULT_ID, 0, defaultTitle, defaultExpForLevelUp));
        String path = "Level.tier.custom";
        List<Integer> levelDefinedList = new ArrayList<>();
        for (String id : config.getConfigurationSection(path).getKeys(false)) {
            if (id.equals(DEFAULT_ID)) {
                DuelTimePlugin.getInstance().getLogger().warning("等级id非法");
                //对应的等级已有展示名定义，抛出异常
                break;
            }
            int level = config.getInt(path + "." + id + ".level");
            if (level < 1) {
                DuelTimePlugin.getInstance().getLogger().warning("等级值非法：" + level);
                //等级为非正数，抛出异常
                break;
            }
            if (levelDefinedList.contains(level)) {
                DuelTimePlugin.getInstance().getLogger().warning("这个等级已定义：" + level);
                //对应的等级已有展示名定义，抛出异常
                break;
            }
            double expForLevelUp = config.getDouble(path + "." + id + ".exp-for-level-up");
            if (expForLevelUp <= 0) {
                DuelTimePlugin.getInstance().getLogger().warning("经验值非正");
                //升级所需为非正数，抛出异常
                break;
            }
            String title = config.getString(path + "." + id + ".title").replace("&", "§");
            Tier tier = new Tier(id, level, title, expForLevelUp);
            int index = 0;
            while (index < tiers.size() && level > tiers.get(index).getLevel()) {
                /*
                index即为当前所比较对象对应的索引值
                由于customNameList按level属性保持升序，现在则按索引值顺序执行比较，遇到大于自己的就停下并在退出循环后插入
                */
                index++;
            }
            levelDefinedList.add(level);
            tiers.add(index, tier);
        }
    }
}
