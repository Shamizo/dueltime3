package cn.valorin.dueltime.yaml.message;

import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.cache.PlayerDataCache;
import cn.valorin.dueltime.data.pojo.PlayerData;
import cn.valorin.dueltime.util.UtilFormat;
import cn.valorin.dueltime.viaversion.ViaVersion;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 提示语构建器
 * 用来获取/直接发送某个处理后的提示语
 */
public class MsgBuilder {
    protected static String prefix;

    private static String getLanguage(CommandSender sender) {
        String language = DuelTimePlugin.getInstance().getCfgManager().getDefaultLanguage();
        if (sender instanceof Player) {
            if (sender != null && sender.getName() != null) {
                String playerName = sender.getName();
                PlayerDataCache playerDataCache = DuelTimePlugin.getInstance().getCacheManager().getPlayerDataCache();
                if (playerDataCache != null) {
                    PlayerData playerData = playerDataCache.get(playerName);
                    if (playerData != null) {
                        String languageUsed = playerData.getLanguage();
                        if (languageUsed != null && MsgManager.languageYamlFileMap.containsKey(languageUsed)) {
                            language = languageUsed;
                        }
                    }
                }
            }
        }
        return language;
    }

    /**
     * 完整方法：获取单条提示语
     *
     * @param msg       提示语
     * @param sender    接收的玩家或后台
     * @param hasPrefix 是否需要前缀（加载消息最前面）
     * @param replacers 变量替换者，依次替换掉Msg中已确立的变量
     * @return 最终的单条提示语
     */
    public static String get(Msg msg, CommandSender sender, boolean hasPrefix, String... replacers) {
        //获取玩家所使用的语言文件，如果是后台则使用默认语言
        String language = getLanguage(sender);
        //获取该语言文件下相应的提示语，如果没有则使用内置的提示语
        String message = language != null ?
                MsgManager.languageYamlFileMap.get(language).getString(msg.getKey(), msg.getDefaultMessage()) :
                msg.getDefaultMessage();
        //根据需要为消息添加前缀
        if (hasPrefix) {
            message = prefix + message;
        }
        //替换颜色符号
        message = message.replace("&", "§");
        //执行变量替换
        String[] variables = msg.getVariable();
        if (replacers != null) {
            for (int i = 0; i < variables.length && i < replacers.length; i++) {
                if (replacers[i] != null) {
                    message = message.replace("{" + variables[i] + "}", replacers[i]);
                }
            }
        }
        //返回最终的提示语
        return message;
    }

    /**
     * 快捷方法：获取单条提示语（无前缀、可选变量）
     */
    public static String get(Msg msg, CommandSender sender, String... replacers) {
        return get(msg, sender, false, replacers);
    }

    /**
     * 完整方法：发送单条提示语
     */
    public static void send(Msg msg, CommandSender sender, boolean hasPrefix, String... replacers) {
        CommandSender target = sender != null ? sender : Bukkit.getConsoleSender();
        target.sendMessage(get(msg, sender, hasPrefix, replacers));
    }

    /**
     * 快捷方法：发送单条提示语（有前缀、可选变量）
     */
    public static void send(Msg msg, CommandSender sender, String... replacers) {
        CommandSender target = sender != null ? sender : Bukkit.getConsoleSender();
        target.sendMessage(get(msg, sender, true, replacers));
    }

    /**
     * 完整方法：获取多条提示语
     *
     * @param msg       提示语
     * @param sender    接收的玩家或后台
     * @param hasPrefix 是否需要前缀（加在第一行）
     * @param replacers 变量替换者，依次替换掉Msg中已确立的变量
     * @return 最终的提示语集合
     */
    public static List<String> gets(Msg msg, CommandSender sender, boolean hasPrefix, String... replacers) {
        //获取玩家所使用的语言文件，如果是后台则使用默认语言
        String language = getLanguage(sender);
        //获取该语言文件下相应的提示语
        List<String> messages;
        if (language != null) {
            List<String> messagesInOutFile = MsgManager.languageYamlFileMap.get(language).getStringList(msg.getKey());
            if (!messagesInOutFile.isEmpty()) {
                messages = messagesInOutFile;
            } else {
                messages = Arrays.asList(msg.getDefaultMessages());
            }
        } else {
            messages = Arrays.asList(msg.getDefaultMessages());
        }
        if (hasPrefix) {
            List<String> prefixSingletonList = Collections.singletonList(prefix);
            prefixSingletonList.addAll(messages);
            messages = prefixSingletonList;
        }
        List<String> messagesReplaced = new ArrayList<>();
        //替换颜色符号与变量
        for (String message : messages) {
            //执行变量替换
            String[] variables = msg.getVariable();
            for (int i = 0; i < variables.length; i++) {
                message = replace(message, variables[i], replacers[i]);
            }
            //执行颜色符号替换
            message = message.replace("&", "§");
            messagesReplaced.add(message);
        }
        //返回最终的提示语
        return messagesReplaced;
    }

    /**
     * 快捷方法：获取多条提示语（无前缀、可选变量）
     */
    public static List<String> gets(Msg msg, CommandSender sender, String... replacers) {
        return gets(msg, sender, false, replacers);
    }

    /**
     * 完整方法：发送多条提示语
     */
    public static void sends(Msg msg, CommandSender sender, boolean hasPrefix, String... replacers) {
        for (String message : gets(msg, sender, hasPrefix, replacers)) {
            sender.sendMessage(message);
        }
    }

    /**
     * 快捷方法：发送多条提示语（无前缀、可选变量）
     */
    public static void sends(Msg msg, CommandSender sender, String... replacers) {
        for (String message : gets(msg, sender, false, replacers)) {
            sender.sendMessage(message);
        }
    }

    /**
     * 完整方法：获取单条/多条含有可点击文字的提示语的TextComponent
     *
     * @param msg       提示语
     * @param sender    接收的玩家或后台
     * @param hasPrefix 是否需要前缀（若是，则根据提示语分类讨论添加）
     * @param replacers 变量替换者，依次替换掉Msg中已确立的变量
     * @return 处理后的TextComponent集合
     */
    public static List<TextComponent> getClickable(Msg msg, CommandSender sender, boolean hasPrefix, String... replacers) {
        String language = getLanguage(sender);
        //获取单行化的字符串，如果本来就是单行就直接处理，反之则加上换行符[line]后合并成单行进行处理
        String singleMessage;
        boolean isMultiple = msg.getDefaultMessages().length > 1;
        if (isMultiple) {
            List<String> messagesInConfig = language != null ?
                    MsgManager.languageYamlFileMap.get(language).getStringList(msg.getKey()) :
                    Arrays.asList(msg.getDefaultMessages());
            List<String> messagesList = !messagesInConfig.isEmpty() ? messagesInConfig : Arrays.asList(msg.getDefaultMessages());
            StringBuilder stringBuilder = new StringBuilder(messagesList.get(0));
            for (int i = 1; i < messagesList.size(); i++) {
                stringBuilder.append("[line]").append(messagesList.get(i));
            }
            singleMessage = stringBuilder.toString();
        } else {
            singleMessage = language != null ?
                    MsgManager.languageYamlFileMap.get(language).getString(msg.getKey(), msg.getDefaultMessage()) :
                    msg.getDefaultMessage();
        }

        //执行变量替换，这里的变量替换就不只替换Msg提示语内容的变量了，还要替换掉悬浮文字、运行指令内容、建议指令内容里的变量。千万注意：这里要深拷贝ClickableSettingsList，直接clone仍会影响到原对象
        String[][] internalSettingsListRaw = msg.getClickableSettingsList().clone();
        String[][] internalSettingsListReplaced = new String[internalSettingsListRaw.length][];
        for (int i = 0; i < internalSettingsListRaw.length; i++) {
            internalSettingsListReplaced[i] = Arrays.copyOf(internalSettingsListRaw[i], internalSettingsListRaw[i].length);
        }
        String[] variables = msg.getVariable();
        for (int i = 0; i < variables.length; i++) {
            singleMessage = replace(singleMessage, variables[i], replacers[i]);
            for (int i2 = 0; i2 < internalSettingsListReplaced.length; i2++) {
                for (int i3 = 0; i3 < 4; i3++) {
                    internalSettingsListReplaced[i2][i3] = replace(internalSettingsListReplaced[i2][i3], variables[i], replacers[i]);
                }
            }
        }
        //执行颜色符号替换
        singleMessage = singleMessage.replace("&", "§");
        //定义正则表达式，非贪婪式匹配所有被[clickable]包围起来的内容（即可点击文字的设置内容），供后续识别
        String pattern = "\\[clickable](.*?)\\[clickable]";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(singleMessage);
        //收集所有匹配到的内容
        List<String> clickablePlaceHolderFound = new ArrayList<>();
        while (matcher.find()) {
            String matchText = matcher.group(1);
            //先收集匹配到的内容
            clickablePlaceHolderFound.add(matchText);
            //操作原message，使匹配到的内容替换为统一的标识，方便后续以[split]为分割符”打碎“原message
            singleMessage = singleMessage.replace("[clickable]" + matchText + "[clickable]", "[split][clickable][split]");
        }
        //“打碎”原message，”碎片“为普通文本、包含换行符的文本、点击文字的设置内容文本
        String[] messageClips = singleMessage.split("\\[split]");
        //创建一个TextComponent集合，一个元素代表一行
        List<TextComponent> textComponents = new ArrayList<>();
        TextComponent nowTextComponent;
        //按需要分类讨论添加前缀
        if (hasPrefix) {
            if (isMultiple) {
                textComponents.add(new TextComponent(prefix));
                nowTextComponent = new TextComponent("");
            } else {
                nowTextComponent = new TextComponent(prefix);
            }
        } else {
            nowTextComponent = new TextComponent("");
        }
        /*
        开始将文本碎片转化为TextComponent
        整体逻辑：
        遇到[clickable]时，按照原设置内容配置可点击文字
        遇到[line]时，换行，将当前操作的TextComponent添加到集合中，表示一行编辑完毕
        未遇到上述时，直接追加TextComponent
         */
        int nowClickablePlaceHolderIndex = 0;
        for (String messageClip : messageClips) {
            TextComponent textComponent;
            if (messageClip.equals("[clickable]")) {
                /*
                检测为可点击文字的占位符，开始解析原本匹配到的设置内容
                设置内容格式：占位符名（插件内定，必须）+分隔符+直接展示的文本内容（必须）+分隔符+触发鼠标悬浮事件后展示的文本内容（非必须）
                这里的分隔符为连续的两个英文冒号":"，即"::"
                 */
                String originalSettingsContent = clickablePlaceHolderFound.get(nowClickablePlaceHolderIndex);
                String[] settings = originalSettingsContent.split("::");
                /*
                筛选出配置项设置够的设置内容
                多余的内容会被无视
                 */
                if (settings.length >= 2) {
                    String clickableTextPlaceHolderType = settings[0];
                    //根据设置内容的第一项的占位符名获取其对应的内定设置内容
                    String[] settingsMatched = null;
                    for (String[] internalSettings : internalSettingsListReplaced) {
                        if (clickableTextPlaceHolderType.equals(internalSettings[0])) {
                            settingsMatched = internalSettings;
                            break;
                        }
                    }
                    //如果有效则进一步配置这个可点击文字
                    if (settingsMatched != null) {
                        String clickableTextDisplayed = settings[1];
                        textComponent = new TextComponent(clickableTextDisplayed);
                        //检测是否设置了可选项“悬浮文字”
                        if (settings.length >= 3) {
                            String clickableTextHovered = settings[2];
                            textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(clickableTextHovered.replace("||", "\n")).create()));
                        }
                        //检测内定配置中是否设置了可选项“点击后执行的命令”
                        if (!settingsMatched[1].isEmpty()) {
                            //这里使用了正则表达式清除所有颜色符号
                            String clickableTextRunCommand = settingsMatched[1].replaceAll("§[0-9a-fA-Fk-oK-OrR]", "");
                            textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, clickableTextRunCommand));
                        }
                        //检测内定配置中是否设置了可选项“点击后建议的命令”
                        if (!settingsMatched[2].isEmpty()) {
                            //这里使用了正则表达式清除所有颜色符号
                            String clickableTextSuggestCommand = settingsMatched[2].replaceAll("§[0-9a-fA-Fk-oK-OrR]", "");
                            textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, clickableTextSuggestCommand));
                        }
                        //检测内定配置中是否设置了可选项“点击后打开的URL”
                        if (!settingsMatched[3].isEmpty()) {
                            String clickableTextOpenURL = settingsMatched[3];
                            textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, clickableTextOpenURL));
                        }
                        //完成本行文本追加
                        nowTextComponent.addExtra(textComponent);
                        //占位符序号自加，让下次找到[clickable]占位符后，可以替换为对应的内容
                        nowClickablePlaceHolderIndex++;
                        continue;
                    }
                }
            }
            /*
            如果遇到的不是占位符，或者是占位符但在解析过程中出现不符合条件的情况，视为非设置内容
            接着再判断碎片是否包含换行符
             */
            if (messageClip.contains("[line]")) {
                //检测含有一个或多个换行符，则以[line]为分隔符打碎，将第一个碎片加到本行，接着以后每一个碎片（除了最后一个）单独成行并添加到textComponents中，最后一个碎片继任nowTextComponent
                String[] lineMessageClips = messageClip.split("\\[line]");
                for (int i = 0; i < lineMessageClips.length; i++) {
                    String lineMessageClip = lineMessageClips[i];
                    if (i == 0) {
                        nowTextComponent.addExtra(new TextComponent(lineMessageClip));
                        textComponents.add(nowTextComponent);
                    } else if (i == lineMessageClips.length - 1) {
                        nowTextComponent = new TextComponent(lineMessageClip);
                    } else {
                        textComponents.add(new TextComponent(lineMessageClip));
                    }
                }
                continue;
            }
            //若也不是换行符，则视为普通文本，继续在本行执行文本追加
            nowTextComponent.addExtra(new TextComponent(messageClip));
        }
        //把最后一次编辑的行对应的TextComponent添加到集合中
        textComponents.add(nowTextComponent);
        return textComponents;
    }

    public static void broadcast(Msg msg, boolean prefix, String... replacers) {
        String message = get(msg, null, prefix, replacers);
        Bukkit.broadcastMessage(message);
    }

    /**
     * 完整方法：发送Title（根据Msg类型）
     */
    public static void sendTitle(Msg titleMsg, Msg subTitleMsg, int fadeIn, int stay, int fadeOut, Player player, ViaVersion.TitleType titleTypeAsMessage, String... replacers) {
        if (player != null) {
            ViaVersion.sendTitle(player, get(titleMsg, player, replacers), get(subTitleMsg, player, replacers), fadeIn, stay, fadeOut, titleTypeAsMessage);
        }
    }

    /**
     * 快捷方法：发送Title（根据Msg类型），默认若服务器为1.8以下版本则不发送
     */
    public static void sendTitle(Msg titleMsg, Msg subTitleMsg, int fadeIn, int stay, int fadeOut, Player player, String... replacers) {
        if (player != null) {
            sendTitle(titleMsg, subTitleMsg, fadeIn, stay, fadeOut, player, null, replacers);
        }
    }

    /**
     * 完整方法：发送Title（根据String类型）
     */
    public static void sendTitle(String title, String subTitle, int fadeIn, int stay, int fadeOut, Player player, ViaVersion.TitleType titleTypeAsMessage) {
        if (player != null) {
            ViaVersion.sendTitle(player, title, subTitle, fadeIn, stay, fadeOut, titleTypeAsMessage);
        }
    }

    /**
     * 快捷方法：发送Title（根据String类型），默认若服务器为1.8以下版本则不发送
     */
    public static void sendTitle(String title, String subTitle, int fadeIn, int stay, int fadeOut, Player player) {
        if (player != null) {
            sendTitle(title, subTitle, fadeIn, stay, fadeOut, player, null);
        }
    }

    /**
     * 完整方法：发送ActionBar（根据Msg类型）
     */
    public static void sendActionBar(Msg actionBarMsg, Player player, boolean considerLowVersion, String... replacers) {
        if (player != null) {
            ViaVersion.sendActionBar(player, get(actionBarMsg, player, replacers), considerLowVersion);
        }
    }

    /**
     * 快捷方法：发送ActionBar（根据Msg类型），默认若服务器为1.8以下版本则不发送
     */
    public static void sendActionBar(Msg actionBarMsg, Player player, String... replacers) {
        if (player != null) {
            ViaVersion.sendActionBar(player, get(actionBarMsg, player, replacers), false);
        }
    }

    /**
     * 完整方法：发送ActionBar（根据String类型）
     */
    public static void sendActionBar(String actionBar, Player player, boolean considerLowVersion) {
        if (player != null) {
            ViaVersion.sendActionBar(player, actionBar, considerLowVersion);
        }
    }

    /**
     * 快捷方法：发送ActionBar（根据String类型），默认若服务器为1.8以下版本则不发送
     */
    public static void sendActionBar(String actionBar, Player player) {
        if (player != null) {
            ViaVersion.sendActionBar(player, actionBar, false);
        }
    }

    /**
     * 完整方法：发送含有可点击文字的单条提示语（如果接收方是后台，那也能确保能正常展示）
     */
    public static void sendClickable(Msg msg, CommandSender sender, boolean hasPrefix, String... replacers) {
        TextComponent textComponent = getClickable(msg, sender, hasPrefix, replacers).get(0);
        if (sender instanceof Player) {
            ((Player) sender).spigot().sendMessage(textComponent);
        } else {
            sender.sendMessage(textComponent.toPlainText());
        }
    }

    /**
     * 完整方法：发送含有可点击文字的多条提示语
     */
    public static void sendsClickable(Msg msg, CommandSender sender, boolean hasPrefix, String... replacers) {
        List<TextComponent> textComponents = getClickable(msg, sender, hasPrefix, replacers);
        for (TextComponent textComponent : textComponents) {
            if (sender instanceof Player) {
                ((Player) sender).spigot().sendMessage(textComponent);
            } else {
                sender.sendMessage(textComponent.toPlainText());
            }
        }
    }

    /**
     * 替换占位符的内容，尤其是特殊占位符（进度条等）
     *
     * @param message  原字符串
     * @param variable 变量名（可能需要特殊处理，如进度条传入的包括变量名id和一些属性的定义）
     * @param replacer 传入的替换内容（可能需要特殊处理，如进度条传入的替换内容则是一组double型数据）
     * @return 替换后的字符串
     */
    private static String replace(String message, String variable, String replacer) {
        if (variable.startsWith("progress_bar_") && message.contains("progress_bar_")) {
            //特殊变量：进度条
            try {
                String id = message.split("_")[2];
                Matcher matcher = Pattern.compile("(\\{progress_bar_" + id + "_.*?\\})").matcher(message);
                String variableFound = null;
                while (matcher.find()) {
                    variableFound = matcher.group(1);
                }
                if (variableFound == null) return message;
                String[] splits = variableFound.split("_");
                int number = Integer.parseInt(splits[3]);
                String symbol = splits[4];
                char completedPartColorCode = splits[5].charAt(0);
                char remainingPartColorCode = splits[6].charAt(0);
                double completedValue = Double.parseDouble(replacer.split(" ")[0]);
                double remainingValue = Double.parseDouble(replacer.split(" ")[1]);
                return message.replace(variableFound, UtilFormat.getProgressBarString(number, symbol, completedPartColorCode, remainingPartColorCode, completedValue, remainingValue));
            } catch (Exception e) {
                e.printStackTrace();
                return message;
            }
        }
        //常规替换
        return message.replace("{" + variable + "}", replacer);
    }
}
