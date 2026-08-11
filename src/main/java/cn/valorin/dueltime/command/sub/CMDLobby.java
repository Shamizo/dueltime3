package cn.valorin.dueltime.command.sub;

import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.cache.LocationCache;
import cn.valorin.dueltime.command.SubCommand;
import cn.valorin.dueltime.util.UtilHelpList;
import cn.valorin.dueltime.util.UtilSync;
import cn.valorin.dueltime.yaml.message.Msg;
import cn.valorin.dueltime.yaml.message.MsgBuilder;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CMDLobby extends SubCommand {
    private final UtilHelpList helpList;

    public CMDLobby() {
        super("lobby", "l");
        helpList = new UtilHelpList(Msg.COMMAND_TITLE_LOBBY, true)
                .add("help", new String[]{"help", "h"}, "help(h)", CommandPermission.ADMIN, Msg.COMMAND_SUB_LOBBY_HELP_DESCRIPTION)
                .add("set", new String[]{"set", "s"}, "set(s)", CommandPermission.ADMIN, Msg.COMMAND_SUB_LOBBY_SET_DESCRIPTION)
                .add("delete", new String[]{"delete", "d"}, "delete(d)", CommandPermission.ADMIN, Msg.COMMAND_SUB_LOBBY_DELETE_DESCRIPTION);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        LocationCache cache = DuelTimePlugin.getInstance().getCacheManager().getLocationCache();
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                MsgBuilder.send(Msg.ERROR_NOT_PLAYER_EXECUTOR, sender);
                return true;
            }
            Location lobbyLocation = cache.get(LocationCache.InternalType.LOBBY.getId());
            if (lobbyLocation == null) {
                MsgBuilder.send(Msg.COMMAND_SUB_LOBBY_NONE, sender);
                return true;
            }
            UtilSync.safeTeleport((Player) sender, lobbyLocation);
            return true;
        }
        if (!sender.hasPermission(CommandPermission.ADMIN)) {
            MsgBuilder.send(Msg.ERROR_NO_PERMISSION, sender);
            return true;
        }
        String commandEntered = args[1];
        UtilHelpList.SingleCommand singleCommand = helpList.getSubCommandByEnter(commandEntered);
        if (singleCommand == null) {
            helpList.send(sender, label,args[0]);
            return true;
        }
        String singleCommandId = singleCommand.getId();
        if (singleCommandId.equals("help")) {
            helpList.send(sender, label,args[0]);
            return true;
        }
        if (!(sender instanceof Player)) {
            MsgBuilder.send(Msg.ERROR_NOT_PLAYER_EXECUTOR, sender);
            return true;
        }
        Player player = (Player) sender;
        Location lobbyLocation = cache.get(LocationCache.InternalType.LOBBY.getId());
        if (singleCommandId.equals("set")) {
            if (lobbyLocation != null) {
                cache.set(LocationCache.InternalType.LOBBY.getId(), player.getLocation());
                MsgBuilder.send(Msg.COMMAND_SUB_LOBBY_UPDATE_SUCCESSFULLY, sender);
            } else {
                cache.add(LocationCache.InternalType.LOBBY.getId(), player.getLocation());
                MsgBuilder.send(Msg.COMMAND_SUB_LOBBY_SET_SUCCESSFULLY, sender);
            }
            return true;
        }
        if (singleCommandId.equals("delete")) {
            if (lobbyLocation == null) {
                MsgBuilder.send(Msg.COMMAND_SUB_LOBBY_DELETE_FAIL_NONE, sender);
                return true;
            }
            cache.remove(LocationCache.InternalType.LOBBY.getId());
            MsgBuilder.send(Msg.COMMAND_SUB_LOBBY_DELETE_SUCCESSFULLY, sender);
            return true;
        }
        helpList.sendSuggest(sender, label, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 2) {
            // 第二级tab补全：显示lobby子命令
            List<String> subCommands = Arrays.asList("help", "set", "delete");
            return subCommands.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return null;
    }
}
