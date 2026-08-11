package cn.valorin.dueltime.command.sub;

import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.arena.ArenaManager;
import cn.valorin.dueltime.arena.base.BaseArena;
import cn.valorin.dueltime.command.SubCommand;
import cn.valorin.dueltime.util.UtilHelpList;
import cn.valorin.dueltime.yaml.message.Msg;
import cn.valorin.dueltime.yaml.message.MsgBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

public class CMDSpectate extends SubCommand {

    public CMDSpectate() {
        super("spectate", "sp", "watch", "w");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MsgBuilder.send(Msg.ERROR_NOT_PLAYER_EXECUTOR, sender);
            return true;
        }
        Player player = (Player) sender;
        ArenaManager arenaManager = DuelTimePlugin.getInstance().getArenaManager();
        if (args.length < 2) {
            BaseArena arenaSpectated = arenaManager.getSpectate(player);
            if (arenaSpectated != null) {
                arenaManager.removeSpectator(player);
                MsgBuilder.send(Msg.ARENA_TYPE_CLASSIC_FUNCTION_STOP_SPECTATE_SUCCESSFULLY, player,
                        arenaSpectated.getName());
                return true;
            }
            CMDHelp.helpList.sendCorrect(sender, 1, CMDHelp.helpList.getSubCommandById("spectate"), label, args);
            return true;
        }
        String arenaIdEntered = args[1];
        BaseArena arena = DuelTimePlugin.getInstance().getArenaManager().get(arenaIdEntered);
        if (arena == null) {
            MsgBuilder.send(Msg.COMMAND_SUB_SPECTATE_FAIL_ARENA_NOT_EXISTS, player,
                    arenaIdEntered);
            UtilHelpList.sendSuggest(sender, 1,
                    arenaManager.getList().stream().map(BaseArena::getId).collect(Collectors.toList()), label, args);
            return true;
        }
        if (arena.getState() == BaseArena.State.DISABLED) {
            MsgBuilder.send(Msg.COMMAND_SUB_SPECTATE_FAIL_ARENA_NO_GAMES, player,
                    arenaIdEntered);
            return true;
        }
        if (arena.getState() == BaseArena.State.WAITING) {
            MsgBuilder.send(Msg.COMMAND_SUB_SPECTATE_FAIL_ARENA_NO_GAMES, player,
                    arenaIdEntered);
            return true;
        }
        arenaManager.spectate(player, arenaIdEntered);
        return true;
    }
}
