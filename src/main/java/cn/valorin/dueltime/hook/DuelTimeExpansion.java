package cn.valorin.dueltime.hook;

import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.cache.CacheManager;
import cn.valorin.dueltime.cache.PlayerDataCache;
import cn.valorin.dueltime.data.pojo.PlayerData;
import cn.valorin.dueltime.level.Tier;
import cn.valorin.dueltime.ranking.RankingManager;
import cn.valorin.dueltime.util.UtilFormat;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class DuelTimeExpansion extends PlaceholderExpansion {
    private final DuelTimePlugin plugin;

    public DuelTimeExpansion(DuelTimePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getAuthor() {
        return "Valorin";
    }

    @Override
    public String getIdentifier() {
        return "dueltime";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) return "";
        CacheManager cacheManager = DuelTimePlugin.getInstance().getCacheManager();
        PlayerData playerData = cacheManager.getPlayerDataCache().get(player.getName());
        if (playerData != null) {
            if (identifier.equals("classic_win_number")) {
                return "" + playerData.getArenaClassicWins();
            }
            if (identifier.equals("classic_loss_number")) {
                return "" + playerData.getArenaClassicLoses();
            }
            if (identifier.equals("classic_draw_number")) {
                return "" + playerData.getArenaClassicDraws();
            }
            if (identifier.equals("classic_win_rate")) {
                return UtilFormat.round((playerData.getArenaClassicWins() / (double) (playerData.getArenaClassicLoses() != 0 ? playerData.getArenaClassicLoses() : 1)) * 100, 1) + "%";
            }
            if (identifier.equals("classic_game_number")) {
                return "" + (playerData.getArenaClassicWins() + playerData.getArenaClassicLoses() + playerData.getArenaClassicDraws());
            }
            if (identifier.equals("classic_game_time")) {
                return "" + playerData.getArenaClassicTime();
            }
            if (identifier.equals("total_game_number")) {
                return "" + playerData.getTotalGameNumber();
            }
            if (identifier.equals("total_game_time")) {
                return "" + playerData.getTotalGameTime();
            }
            if (identifier.equals("exp")) {
                return "" + playerData.getExp();
            }
            if (identifier.equals("exp_to_next_level")) {
                return "" + DuelTimePlugin.getInstance().getLevelManager().calculateRemainingExpForLevelUp(playerData.getExp());
            }
            if (identifier.equals("level")) {
                return "" + DuelTimePlugin.getInstance().getLevelManager().getLevel(player.getName());
            }
            if (identifier.equals("tier")) {
                Tier tier = DuelTimePlugin.getInstance().getLevelManager().getTier(player.getName());
                return tier != null ? tier.getTitle() : "";
            }
            if (identifier.equals("point")) {
                return "" + playerData.getPoint();
            }
        }
        RankingManager rankingManager = DuelTimePlugin.getInstance().getRankingManager();
        try {
            if (identifier.equals("rank_classic_win_number")) {
                return rankingManager.getRanking(RankingManager.InternalType.CLASSIC_WIN_NUMBER.getId()).getRankString(player);
            }
            if (identifier.equals("rank_classic_win_rate")) {
                return rankingManager.getRanking(RankingManager.InternalType.CLASSIC_WIN_RATE.getId()).getRankString(player);
            }
            if (identifier.equals("rank_classic_game_time")) {
                return rankingManager.getRanking(RankingManager.InternalType.CLASSIC_GAME_TIME.getId()).getRankString(player);
            }
            if (identifier.equals("rank_classic_game_number")) {
                return rankingManager.getRanking(RankingManager.InternalType.CLASSIC_GAME_NUMBER.getId()).getRankString(player);
            }
            if (identifier.equals("rank_total_game_number")) {
                return rankingManager.getRanking(RankingManager.InternalType.TOTAL_GAME_NUMBER.getId()).getRankString(player);
            }
            if (identifier.equals("rank_total_game_time")) {
                return rankingManager.getRanking(RankingManager.InternalType.TOTAL_GAME_TIME.getId()).getRankString(player);
            }
            if (identifier.equals("rank_exp")) {
                return rankingManager.getRanking(RankingManager.InternalType.EXP.getId()).getRankString(player);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
}
