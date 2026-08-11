package cn.valorin.dueltime.event.progress;

import cn.valorin.dueltime.progress.Progress;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ProgressStartEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Progress progress;

    public ProgressStartEvent(Player player, Progress progress) {
        this.player = player;
        this.progress = progress;
    }

    public Player getPlayer() {
        return player;
    }

    public Progress getProgress() {
        return progress;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
