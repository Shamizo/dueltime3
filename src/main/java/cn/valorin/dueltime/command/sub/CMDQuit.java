package cn.valorin.dueltime.command.sub;

import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.arena.base.BaseArena;
import cn.valorin.dueltime.command.SubCommand;
import cn.valorin.dueltime.event.arena.ArenaTryToQuitEvent;
import cn.valorin.dueltime.yaml.message.Msg;
import cn.valorin.dueltime.yaml.message.MsgBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CMDQuit extends SubCommand {

    public CMDQuit() {
        super("quit", "q","leave","le");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MsgBuilder.send(Msg.ERROR_NOT_PLAYER_EXECUTOR, sender);
            return true;
        }
        Player player = (Player) sender;
        BaseArena arena = DuelTimePlugin.getInstance().getArenaManager().getOf(player);
        if (arena == null) {
            MsgBuilder.send(Msg.COMMAND_SUB_QUIT_NOT_IN_ARENA, player);
            return true;
        }
        //发布事件，方便其他插件编写退出比赛的逻辑
        Bukkit.getServer().getPluginManager().callEvent(new ArenaTryToQuitEvent(player, arena));
        return true;
    }
}
