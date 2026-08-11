package cn.valorin.dueltime.command;

import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.command.sub.CommandPermission;
import cn.valorin.dueltime.yaml.message.Msg;
import cn.valorin.dueltime.yaml.message.MsgBuilder;
import net.md_5.bungee.api.chat.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CMDMain {
    public static boolean onCommand(CommandSender sender, String label) {
        sender.sendMessage("");
        sender.sendMessage("§a§lDuel§2§l§oTime§2§l3 §fBy Valorin(秋韵)");
        sender.sendMessage("§7§m──§f§l§m───────────────────────§7§m──");
        sender.sendMessage("");
        sender.sendMessage("  §b/"+label+"§b help§3(h) §7§l- "+ MsgBuilder.get(Msg.COMMAND_MAIN_HELP,sender));
        if (sender.hasPermission(CommandPermission.ADMIN))
            sender.sendMessage("  §b/"+label+"§b adminhelp§3(ah) §7§l- "+ MsgBuilder.get(Msg.COMMAND_MAIN_ADMIN_HELP,sender));
        sender.sendMessage("");
        sender.sendMessage("§7§m──§f§l§m───────────────────────§7§m──");
        sendSite(sender);
        sender.sendMessage("");
        return true;
    }

    private static void sendSite(CommandSender sender) {
        TextComponent doc = new TextComponent();
        doc.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.yuque.com/qiuyun-hsodc/flfcwb?#"));
        doc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("点击打开本插件的使用文档链接\nClick to open the doc link for this plugin").create()));
        String text1 = "§6[使用文档 | Document]";
        doc.setText(text1);
        if (sender instanceof Player) {
            ((Player) sender).spigot().sendMessage(doc);//因为发现1.7及以下的CommandSender是没有spigot()的，所以要转换类型后再发送
        } else {
            sender.sendMessage(text1);
        }
    }
}
