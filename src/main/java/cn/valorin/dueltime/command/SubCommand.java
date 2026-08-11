package cn.valorin.dueltime.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class SubCommand {
    private final String[] aliases;
    
    public SubCommand(String... aliases) {
        this.aliases = aliases;
    }

    public abstract boolean onCommand(CommandSender sender, Command cmd, String label, String[] args);

    public String[] getAliases() {
        return aliases;
    }
    
    /**
     * Tab补全方法
     * @param sender 命令发送者
     * @param command 命令
     * @param label 命令标签
     * @param args 参数
     * @return 补全列表
     */
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return null;
    }
}
