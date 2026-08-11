package cn.valorin.dueltime.util;

import cn.valorin.dueltime.yaml.message.Msg;
import cn.valorin.dueltime.yaml.message.MsgBuilder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UtilHelpList {
    private final Msg titleMsg;
    private final boolean hasSubArg;
    private final List<SingleCommand> commands = new ArrayList<>();
    private final List<String> commandMainAliaList = new ArrayList<>();

    public UtilHelpList(Msg titleMsg, boolean hasSubArg) {
        this.titleMsg = titleMsg;
        this.hasSubArg = hasSubArg;
    }

    /**
     * 注册一条子指令
     *
     * @param commandContent    指令原内容
     * @param commandPermission 指令所需要的权限
     * @return 注册一条指令后的本对象
     */
    public UtilHelpList add(String id, String[] commandAlias, String commandContent, String commandPermission, Msg helpMsg, boolean isSeries) {
        String[] commandContentClips = ((hasSubArg ? "arg0 " : "") + commandContent).split(" ");
        SingleCommand singleCommand = new SingleCommand(id, commandAlias, commandContent, commandContentClips, helpMsg, commandPermission, isSeries);
        commands.add(singleCommand);
        commandMainAliaList.add(commandAlias[0]);
        return this;
    }


    public UtilHelpList add(String id, String[] commandAlias, String commandContent, String commandPermission, Msg helpMsg) {
        return add(id, commandAlias, commandContent, commandPermission, helpMsg, false);
    }

    /**
     * 向玩家发送完整的帮助
     *
     * @param sender 接收的玩家或后台
     */
    public void send(CommandSender sender, String label, String firstArg) {
        sender.sendMessage("§a§lDuel§2§l§oTime §f§l>> §r" + MsgBuilder.get(titleMsg, sender));
        for (SingleCommand command : commands) {
            if (!command.judgePermission(sender)) {
                continue;
            }
            //一条完整的指令内容组成：颜色符号（根据是否需要权限区分）+ 加粗符号（根据是否为系列指令） + 斜杠 + 玩家输入主指令原词（因为玩家可能输入缩写）+ 玩家输入的子指令原词（可能不存在，如help） + 替换参数占位符后的子指令内容
            String commandContent =
                    (command.permission != null ? "§2/" : "§a/") +
                            (command.isSeries ? "§l" : "") +
                            label +
                            (hasSubArg ? " " + firstArg : "") +
                            Para.deals(command.content, sender);
            String commandDescription = MsgBuilder.get(command.descriptionMsg, sender);
            sender.sendMessage(commandContent + " §f- §r" + commandDescription);
        }
    }

    /**
     * 向玩家发送某条帮助的纠错提示
     *
     * @param sender 接收的玩家或后台
     */
    public void sendCorrect(CommandSender sender, int wrongArgIndex, SingleCommand command, String label, String[] args) {
        //根据输入的子指令筛选出SingleCommand后，开始构建纠错指令，原则：有错则改，无错则尊重原输入
        StringBuilder builder = new StringBuilder("§6" + label);
        String[] argClips = command.argClips;
        for (int i = 0; i < argClips.length; i++) {
            boolean isWrongArg;
            String strAppended;
            if (i + 1 > args.length) {
                            /*
                            如果当前指令碎片属于未输入部分，则需从原指令碎片获取
                            接着判断是否为可选参数，若是，则不能标记为错误参数
                             */
                strAppended = argClips[i];
                isWrongArg = !(argClips[i].contains("[") && argClips[i].contains("]"));
            } else {
                /*
                如果当前指令碎片属于已输入部分，接着判断是否为标记的错误参数
                若是，则从原指令碎片中获取
                若否，则从输入的指令碎片中获取
                 */
                if (i == wrongArgIndex) {
                    strAppended = argClips[i];
                    isWrongArg = true;
                } else {
                    strAppended = args[i];
                    isWrongArg = false;
                }
            }
            //如果发现参数占位符，根据语言环境替换掉参数Msg，如将 <%player%> 替换为 <玩家名>
            if (strAppended.contains("%")) {
                strAppended = Para.deal(strAppended, sender);
            }
            //移除缩写提示，缩写提示都是带括号的，例如arena(a)
            Pattern regex = Pattern.compile("\\((.*?)\\)");
            Matcher matcher = regex.matcher(strAppended);
            while (matcher.find()) {
                String tip = matcher.group();
                strAppended = strAppended.replace("(" + tip + ")", "");
            }
            //设置颜色。未填、错填的参数都会被标记为&c红色，其他为&6橙色
            strAppended = (isWrongArg) ? " §c§n" + strAppended + "§r" : " §6" + strAppended;
            builder.append(strAppended);
        }
        //生成最终的纠错内容
        String stringCorrected = "§6/" + builder;
        MsgBuilder.sendClickable(Msg.COMMAND_CORRECT, sender, false, stringCorrected);
    }

    public void sendCorrect(CommandSender sender, int wrongArgIndex, String commandEnter, String label, String[] args) {
        SingleCommand command = getSubCommandByEnter(commandEnter);
        if (command != null) {
            sendCorrect(sender, wrongArgIndex, command, label, args);
        }
    }

    /**
     * 根据输入错误的项，经和备选项比对选出相似度最高者，生成建议指令并向玩家发送
     */
    public static void sendSuggest(CommandSender sender, int wrongArgIndex, Collection<String> candidates, String label, String[] args) {
        if (args == null || wrongArgIndex < 0 || wrongArgIndex >= args.length) {
            return;
        }
        String argEntered = args[wrongArgIndex];
        String mostSimilar = UtilSimilarityComparer.getMostSimilar(argEntered, candidates);
        if (mostSimilar == null) return;//相似度达不到默认阈值则不生成建议指令
        StringBuilder builder = new StringBuilder("§2/" + label);
        for (int i = 0; i < args.length; i++) {
            if (i == wrongArgIndex) {
                builder.append(" §a§n").append(mostSimilar).append("§r");
                continue;
            }
            builder.append(" §2").append(args[i]);
        }
        MsgBuilder.sendClickable(Msg.COMMAND_SUGGEST, sender, false, builder.toString());
    }

    public void sendSuggest(CommandSender sender, String label, String[] args) {
        sendSuggest(sender, 1, commandMainAliaList, label, args);
    }

    public Msg getTitleMsg() {
        return titleMsg;
    }

    public SingleCommand getSubCommandByEnter(String commandEnter) {
        for (SingleCommand command : commands) {
            for (String commandAlias : command.alias) {
                if (commandAlias.equalsIgnoreCase(commandEnter)) {
                    return command;
                }
            }
        }
        return null;
    }

    public SingleCommand getSubCommandById(String id) {
        for (SingleCommand command : commands) {
            if (command.id.equals(id)) {
                return command;
            }
        }
        return null;
    }

    public enum Para {
        PLAYER("player", Msg.COMMAND_PARAMETER_PLAYER),
        VALUE("value", Msg.COMMAND_PARAMETER_VALUE),
        ARENA_ID("arena_id", Msg.COMMAND_PARAMETER_ARENA_ID),
        SOURCE_ARENA_ID("source_arena_id", Msg.COMMAND_PARAMETER_SOURCE_ARENA_ID),
        TARGET_ARENA_ID("target_arena_id", Msg.COMMAND_PARAMETER_TARGET_ARENA_ID),
        ARENA_TYPE("arena_type", Msg.COMMAND_PARAMETER_ARENA_TYPE),
        ARENA_FUNCTION_ID("arena_function_id", Msg.COMMAND_PARAMETER_ARENA_FUNCTION_ID),
        LANGUAGE_FILE_NAME("language_file_name", Msg.COMMAND_PARAMETER_LANGUAGE_FILE_NAME),
        POINT("point", Msg.COMMAND_PARAMETER_POINT),
        PAGE("page", Msg.COMMAND_PARAMETER_PAGE),
        ROW("row", Msg.COMMAND_PARAMETER_ROW),
        COLUMN("column", Msg.COMMAND_PARAMETER_COLUMN),
        DESCRIPTION("description", Msg.COMMAND_PARAMETER_DESCRIPTION),
        VALUE_OR_CONTENT("value_or_content", Msg.COMMAND_PARAMETER_VALUE_OR_CONTENT),
        COMMAND_EXECUTOR("command_executor", Msg.COMMAND_PARAMETER_COMMAND_EXECUTOR),
        RANKING_TYPE("ranking_type", Msg.COMMAND_PARAMETER_RANKING_TYPE),
        RANKING_PAGE("ranking_page", Msg.COMMAND_PARAMETER_RANKING_PAGE),
        ;

        private final String placeHolderName;
        private final Msg msg;

        Para(String placeHolderName, Msg msg) {
            this.placeHolderName = placeHolderName;
            this.msg = msg;
        }

        //处理指令内容的单个碎片
        public static String deal(String arg, CommandSender sender) {
            for (Para para : values()) {
                if (arg
                        .replace("%", "")
                        .replace("<", "")
                        .replace(">", "")
                        .replace("[", "")
                        .replace("]", "").equals(para.placeHolderName)) {
                    return arg.replace("%" + para.placeHolderName + "%", MsgBuilder.get(para.msg, sender));
                }
            }
            return null;
        }

        //处理整条指令内容
        public static String deals(String content, CommandSender sender) {
            StringBuilder contentDealtBuilder = new StringBuilder();
            for (String contentClip : content.split(" ")) {
                if (!contentClip.contains("%")) {
                    contentDealtBuilder.append(" ").append(contentClip);
                    continue;
                }
                for (Para para : values()) {
                    if (contentClip
                            .replace("%", "")
                            .replace("<", "")
                            .replace(">", "")
                            .replace("[", "")
                            .replace("]", "").equals(para.placeHolderName)) {
                        contentClip = contentClip.replace("%" + para.placeHolderName + "%", MsgBuilder.get(para.msg, sender));
                        contentDealtBuilder.append(" ").append(contentClip);
                    }
                }
            }
            return contentDealtBuilder.toString();
        }
    }

    public static class SingleCommand {
        private final String id;
        private final String[] alias;
        private final String content;
        private final String[] argClips;
        private final Msg descriptionMsg;
        private final String permission;
        private final boolean isSeries;

        private SingleCommand(String id, String[] alias, String content, String[] argClips, Msg descriptionMsg, String permission, boolean isSeries) {
            this.id = id;
            this.alias = alias;
            this.content = content;
            this.argClips = argClips;
            this.descriptionMsg = descriptionMsg;
            this.permission = permission;
            this.isSeries = isSeries;
        }

        public String getId() {
            return id;
        }

        public boolean judgePermission(CommandSender sender) {
            return permission == null || !(sender instanceof Player) || sender.hasPermission(permission);
        }
    }
}
