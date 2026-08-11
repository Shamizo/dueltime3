package cn.valorin.dueltime.ranking.hologram;

import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.util.SchedulerUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class HologramInstance {
    private eu.decentsoftware.holograms.api.holograms.Hologram hologram;

    public HologramInstance(Location location, String rankingId, List<String> content, Material material) {
        //这个插件的全息图id不允许包括冒号
        this.hologram = eu.decentsoftware.holograms.api.DHAPI.createHologram(rankingId.replace("dueltime:", ""), location, null);
        if (material != null) {
            eu.decentsoftware.holograms.api.DHAPI.addHologramLine(hologram, material);
        }
        content.forEach(line -> eu.decentsoftware.holograms.api.DHAPI.addHologramLine(hologram, line));
    }

    public void destroy() {
        if (hologram != null) {
            hologram.delete();
        }
    }

    public void refresh(List<String> content, Material material) {
        SchedulerUtil.runTask(() -> {
            eu.decentsoftware.holograms.api.DHAPI.setHologramLines(hologram, new ArrayList<>());
            if (material != null) {
                eu.decentsoftware.holograms.api.DHAPI.addHologramLine(hologram, material);
            }
            content.forEach(line -> eu.decentsoftware.holograms.api.DHAPI.addHologramLine(hologram, line));
        });
    }


    public void move(Location location) {
        if (hologram != null) {
            eu.decentsoftware.holograms.api.DHAPI.moveHologram(hologram, location);
        }
    }
}
