package cn.valorin.dueltime.arena.gamer;

import cn.valorin.dueltime.arena.base.BaseGamerData;
import cn.valorin.dueltime.data.pojo.ClassicArenaRecordData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ClassicGamerData extends BaseGamerData {
    private Location recentLocation;//最后一次在场地内的位置，防止因玩家在临界情况时因移动过快未能被移动事件记录，使得无法拉回场地内
    private final Location originalLocation;
    private int hitTime;
    private double totalDamage;
    private double maxDamage;
    private ClassicArenaRecordData.Result result;

    public ClassicGamerData(Player player, Location originalLocation) {
        super(player);
        this.originalLocation = originalLocation;
    }

    public Location getRecentLocation() {
        return recentLocation;
    }

    public void updateRecentLocation(Location recentLocation) {
        this.recentLocation = recentLocation;
    }

    public Location getOriginalLocation() {
        return originalLocation;
    }

    public void addDamage(double damage) {
        this.totalDamage += damage;
    }

    public void checkAndSetMaxDamage(double damage) {
        if (this.maxDamage == 0 || damage > this.maxDamage) {
            this.maxDamage = damage;
        }
    }

    public void addHitTime() {
        this.hitTime++;
    }

    public void confirmResult(ClassicArenaRecordData.Result result) {
        this.result = result;
    }

    public ClassicArenaRecordData.Result getResult() {
        return result;
    }

    public int getHitTime() {
        return hitTime;
    }

    public double getTotalDamage() {
        return totalDamage;
    }

    public double getMaxDamage() {
        return maxDamage;
    }
}
