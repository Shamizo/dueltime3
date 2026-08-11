package cn.valorin.dueltime.command.sub;

import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.command.SubCommand;
import cn.valorin.dueltime.yaml.message.Msg;
import cn.valorin.dueltime.yaml.message.MsgBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CMDStart extends SubCommand {

    public CMDStart() {
        super("start", "st");
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command cmd, String label, String[] args) {
        if (!(commandSender instanceof Player)) {
            MsgBuilder.send(Msg.ERROR_NOT_PLAYER_EXECUTOR, commandSender);
            return true;
        }
        Player player = (Player) commandSender;
        if (DuelTimePlugin.getInstance().getCacheManager().getBlacklistCache().contains(player.getName())) {
            MsgBuilder.send(Msg.COMMAND_SUB_START_FAIL_SELF_IN_BLACK_LIST, player);
            return true;
        }
        DuelTimePlugin.getInstance().getCustomInventoryManager().getStart().openFor(player);
        return true;
    }
}
