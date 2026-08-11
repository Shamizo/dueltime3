package cn.valorin.dueltime.command.sub;

import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.command.SubCommand;
import cn.valorin.dueltime.yaml.message.Msg;
import cn.valorin.dueltime.yaml.message.MsgBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CMDRecord extends SubCommand{
    public CMDRecord() {
        super("record", "r");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                MsgBuilder.send(Msg.ERROR_NOT_PLAYER_EXECUTOR, sender);
                return true;
            }
            DuelTimePlugin.getInstance().getCustomInventoryManager().getArenaRecord().openFor((Player) sender);
            return true;
        }
        return true;
    }
}
