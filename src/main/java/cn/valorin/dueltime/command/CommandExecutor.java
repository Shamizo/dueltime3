package cn.valorin.dueltime.command;


import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.util.UtilSimilarityComparer;
import cn.valorin.dueltime.yaml.message.Msg;
import cn.valorin.dueltime.yaml.message.MsgBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class CommandExecutor implements TabExecutor {
    private final List<String> subCommandMainAliaList;

    public CommandExecutor(Set<SubCommand> commands) {
        subCommandMainAliaList = commands.stream().map(command -> command.getAliases()[0]).collect(Collectors.toList());
    }

    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {
        if (args.length == 0) {
            return CMDMain.onCommand(sender, label);
        } else {
            SubCommand subCommand = DuelTimePlugin.getInstance().getCommandHandler()
                    .getSubCommand(args[0]);
            if (subCommand == null) {
                MsgBuilder.send(Msg.ERROR_SUB_COMMAND_NOT_EXISTS, sender,
                        args[0]);
                String mostSimilarSubCommand = UtilSimilarityComparer.getMostSimilar(args[0], subCommandMainAliaList);
                if (mostSimilarSubCommand != null) {
                    String commandSuggested = "§2/" + label + " §a§n" + mostSimilarSubCommand + "§r";
                    MsgBuilder.sendClickable(Msg.COMMAND_SUGGEST, sender, false, commandSuggested);
                }
                return true;
            }
            return subCommand.onCommand(sender, command, label, args);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            // 第一级tab补全：显示所有子命令
            return subCommandMainAliaList.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else {
            // 获取对应的子命令
            SubCommand subCommand = DuelTimePlugin.getInstance().getCommandHandler().getSubCommand(args[0]);
            if (subCommand != null) {
                // 调用子命令的tab补全方法
                return subCommand.onTabComplete(sender, command, label, args);
            }
        }
        return null;
    }
}
