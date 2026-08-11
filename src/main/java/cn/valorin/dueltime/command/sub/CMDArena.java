package cn.valorin.dueltime.command.sub;

import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.arena.ArenaManager;
import cn.valorin.dueltime.arena.base.BaseArena;
import cn.valorin.dueltime.arena.base.BaseArenaData;
import cn.valorin.dueltime.arena.type.ArenaType;
import cn.valorin.dueltime.arena.type.ArenaTypeManager;
import cn.valorin.dueltime.command.SubCommand;
import cn.valorin.dueltime.progress.ProgressType;
import cn.valorin.dueltime.util.UtilFormat;
import cn.valorin.dueltime.util.UtilHelpList;
import cn.valorin.dueltime.yaml.message.Msg;
import cn.valorin.dueltime.yaml.message.MsgBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class CMDArena extends SubCommand {
    private final UtilHelpList helpList;

    public CMDArena() {
        super("arena", "a");
        helpList = new UtilHelpList(Msg.COMMAND_TITLE_ARENA, true)
                .add("list", new String[]{"list", "l"}, "list(l)", null, Msg.COMMAND_SUB_ARENA_LIST_DESCRIPTION)
                .add("type", new String[]{"type", "t"}, "type(t)", null, Msg.COMMAND_SUB_ARENA_TYPE_DESCRIPTION)
                .add("create", new String[]{"create", "c"}, "create(c) <%arena_type%>", CommandPermission.ADMIN, Msg.COMMAND_SUB_ARENA_CREATE_DESCRIPTION)
                .add("view", new String[]{"view", "v"}, "view(v) <%arena_id%>", CommandPermission.ADMIN, Msg.COMMAND_SUB_ARENA_VIEW_DESCRIPTION)
                .add("delete", new String[]{"delete", "d"}, "delete(d) <%arena_id%>", CommandPermission.ADMIN, Msg.COMMAND_SUB_ARENA_DELETE_DESCRIPTION)
                .add("function_add", new String[]{"function", "f", "functions"}, "function(f) add(a) <%arena_id%> <%arena_function_id%>", CommandPermission.ADMIN, Msg.COMMAND_SUB_ARENA_FUNCTION_ADD_DESCRIPTION)
                .add("function_remove", new String[]{"function", "f", "functions"}, "function(f) remove(r) <%arena_id%> <%arena_function_id%>", CommandPermission.ADMIN, Msg.COMMAND_SUB_ARENA_FUNCTION_REMOVE_DESCRIPTION)
                .add("function_reset", new String[]{"function", "f", "functions"}, "function(f) reset(rs) <%arena_id%> <%arena_function_id%>", CommandPermission.ADMIN, Msg.COMMAND_SUB_ARENA_FUNCTION_RESET_DESCRIPTION)
                .add("function_clear", new String[]{"function", "f", "functions"}, "function(f) clear(c) <%arena_id%>", CommandPermission.ADMIN, Msg.COMMAND_SUB_ARENA_FUNCTION_CLEAR_DESCRIPTION)
                .add("function_list", new String[]{"function", "f", "functions"}, "function(f) list(l) <%arena_id%>", CommandPermission.ADMIN, Msg.COMMAND_SUB_ARENA_FUNCTION_LIST_DESCRIPTION)
                .add("function_type", new String[]{"function", "f", "functions"}, "function(f) type(t) <%arena_type%>", CommandPermission.ADMIN, Msg.COMMAND_SUB_ARENA_FUNCTION_TYPE_DESCRIPTION)
                .add("function_copy", new String[]{"function", "f", "functions"}, "function(f) copy(cp) <%source_arena_id%> <%target_arena_id%> <%arena_function_id%>", CommandPermission.ADMIN, Msg.COMMAND_SUB_ARENA_FUNCTION_COPY_DESCRIPTION);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            helpList.send(sender, label, args[0]);
            return true;
        }
        String commandEntered = args[1];
        UtilHelpList.SingleCommand singleCommand = helpList.getSubCommandByEnter(commandEntered);
        if (singleCommand == null) {
            helpList.send(sender, label, args[0]);
            helpList.sendSuggest(sender, label, args);
            return true;
        }
        String singleCommandId = singleCommand.getId();
        ArenaManager arenaManager = DuelTimePlugin.getInstance().getArenaManager();
        if (singleCommandId.equals("list")) {
            Collection<BaseArena> arenaList = arenaManager.getMap().values();
            if (arenaList.isEmpty()) {
                MsgBuilder.send(Msg.COMMAND_SUB_ARENA_LIST_EMPTY, sender);
                return true;
            }
            MsgBuilder.send(Msg.COMMAND_SUB_ARENA_LIST_HEADING, sender, false);
            int index = 0;
            for (BaseArena arena : arenaList) {
                index++;
                BaseArenaData baseArenaData = arena.getArenaData();
                MsgBuilder.send(Msg.COMMAND_SUB_ARENA_LIST_BODY, sender, false,
                        "" + index, baseArenaData.getName(), DuelTimePlugin.getInstance().getArenaTypeManager().get(baseArenaData.getTypeId()).getName(sender), baseArenaData.getId());
            }
            MsgBuilder.send(Msg.COMMAND_SUB_ARENA_LIST_ENDING, sender, false,
                    "" + index);
            return true;
        }
        if (singleCommandId.equals("type")) {
            List<ArenaType> arenaTypeList = DuelTimePlugin.getInstance().getArenaTypeManager().getList();
            MsgBuilder.send(Msg.COMMAND_SUB_ARENA_TYPE_HEADING, sender, false);
            int index = 0;
            for (ArenaType arenaType : arenaTypeList) {
                index++;
                MsgBuilder.send(Msg.COMMAND_SUB_ARENA_TYPE_BODY, sender, false,
                        "" + index, arenaType.getName(sender), arenaType.getId());
            }
            MsgBuilder.send(Msg.COMMAND_SUB_ARENA_TYPE_ENDING, sender, false,
                    "" + index);
            return true;
        }
        if (!sender.hasPermission(CommandPermission.ADMIN)) {
            MsgBuilder.send(Msg.ERROR_NO_PERMISSION, sender);
            return true;
        }
        if (singleCommandId.equals("create")) {
            if (!(sender instanceof Player)) {
                MsgBuilder.send(Msg.ERROR_NOT_PLAYER_EXECUTOR, sender);
                return true;
            }
            if (args.length < 3) {
                helpList.sendCorrect(sender, -1, singleCommand, label, args);
                return true;
            }
            String typeIdEntered = args[2];
            ArenaType arenaType = DuelTimePlugin.getInstance().getArenaTypeManager().get(typeIdEntered);
            if (arenaType == null) {
                helpList.sendCorrect(sender, 2, singleCommand, label, args);
                MsgBuilder.sendClickable(Msg.COMMAND_SUB_ARENA_CREATE_FAIL_INVALID_TYPE, sender, true,
                        typeIdEntered);
                UtilHelpList.sendSuggest(sender, 2,
                        DuelTimePlugin.getInstance().getArenaTypeManager().getList().stream().map(ArenaType::getId).collect(Collectors.toList()),
                        label, args);
                return true;
            }
            Player player = (Player) sender;
            DuelTimePlugin.getInstance().getProgressManager().enter(player, ProgressType.createArena(player, arenaType));
            return true;
        }
        if (singleCommandId.equals("view")) {
            if (args.length < 3) {
                helpList.sendCorrect(sender, -1, singleCommand, label, args);
                return true;
            }
            String idEntered = args[2];
            BaseArena arena = arenaManager.get(idEntered);
            if (arena == null) {
                MsgBuilder.sendClickable(Msg.COMMAND_SUB_ARENA_INVALID_ID, sender, true,
                        idEntered);
                UtilHelpList.sendSuggest(sender, 2,
                        arenaManager.getList().stream().map(BaseArena::getId).collect(Collectors.toList()),
                        label, args);
                return true;
            }
            BaseArenaData arenaData = arena.getArenaData();
            MsgBuilder.sendsClickable(Msg.COMMAND_SUB_ARENA_VIEW, sender, true,
                    arenaData.getName(),
                    arenaData.getId(),
                    DuelTimePlugin.getInstance().getArenaTypeManager().get(arenaData.getTypeId()).getName(sender),
                    (arenaData.getFunctions() != null) ? "" + arenaData.getFunctions().size() : "0");
            return true;
        }
        if (singleCommandId.equals("delete")) {
            if (args.length < 3) {
                helpList.sendCorrect(sender, -1, singleCommand, label, args);
                return true;
            }
            String idEntered = args[2];
            BaseArena arena = arenaManager.get(idEntered);
            if (arena == null) {
                MsgBuilder.sendClickable(Msg.COMMAND_SUB_ARENA_INVALID_ID, sender, true,
                        idEntered);
                UtilHelpList.sendSuggest(sender, 2,
                        arenaManager.getList().stream().map(BaseArena::getId).collect(Collectors.toList()),
                        label, args);
                return true;
            }
            arenaManager.delete(idEntered);
            MsgBuilder.send(Msg.COMMAND_SUB_ARENA_DELETE_SUCCESSFULLY, sender,
                    idEntered);
            return true;
        }
        if (singleCommandId.startsWith("function")) {
            //如果参数不足，则默认想输入add
            if (args.length < 3) {
                helpList.sendCorrect(sender, 2, helpList.getSubCommandById("function_add"), label, args);
                return true;
            }
            ArenaTypeManager arenaTypeManager = DuelTimePlugin.getInstance().getArenaTypeManager();
            boolean isAdd = args[2].equalsIgnoreCase("add") || args[2].equalsIgnoreCase("a");
            boolean isRemove = args[2].equalsIgnoreCase("remove") || args[2].equalsIgnoreCase("r");
            boolean isReset = args[2].equalsIgnoreCase("reset") || args[2].equalsIgnoreCase("rs");
            boolean isClear = args[2].equalsIgnoreCase("clear") || args[2].equalsIgnoreCase("c");
            boolean isList = args[2].equalsIgnoreCase("list") || args[2].equalsIgnoreCase("l");
            boolean isType = args[2].equalsIgnoreCase("type") || args[2].equalsIgnoreCase("t");
            boolean isCopy = args[2].equalsIgnoreCase("copy") || args[2].equalsIgnoreCase("cp");
            if (isType || isCopy) {
                if (isType) {
                    //function系列的type指令
                    singleCommand = helpList.getSubCommandById("function_type");
                    if (args.length < 4) {
                        helpList.sendCorrect(sender, -1, singleCommand, label, args);
                        return true;
                    }
                    String typeIdEntered = args[3];
                    ArenaType arenaType = arenaTypeManager.get(typeIdEntered);
                    if (arenaType == null) {
                        helpList.sendCorrect(sender, 3, singleCommand, label, args);
                        MsgBuilder.sendClickable(Msg.COMMAND_SUB_ARENA_TYPE_INVALID_ID, sender, true,
                                typeIdEntered);
                        return true;
                    }
                    if (arenaType.getFunctionDef().isEmpty()) {
                        MsgBuilder.send(Msg.COMMAND_SUB_ARENA_FUNCTION_TYPE_EMPTY, sender,
                                typeIdEntered);
                        return true;
                    }
                    MsgBuilder.send(Msg.COMMAND_SUB_ARENA_FUNCTION_TYPE_HEADING, sender, false,
                            arenaType.getName(sender));
                    int index = 0;
                    for (ArenaType.Function function : arenaType.getFunctionDef().values()) {
                        index++;
                        MsgBuilder.sends(Msg.COMMAND_SUB_ARENA_FUNCTION_TYPE_BODY, sender, false,
                                "" + index, function.getName(sender), function.getId(), function.getDescription(sender));
                    }
                    MsgBuilder.send(Msg.COMMAND_SUB_ARENA_FUNCTION_TYPE_ENDING, sender, false,
                            "" + index);
                } else {
                    //function系列的copy指令
                    singleCommand = helpList.getSubCommandById("function_copy");
                    if (args.length < 6) {
                        helpList.sendCorrect(sender, -1, singleCommand, label, args);
                        return true;
                    }
                    String sourceArenaIdEntered = args[3];
                    BaseArena sourceArena = arenaManager.get(sourceArenaIdEntered);
                    if (sourceArena == null) {
                        MsgBuilder.sendClickable(Msg.COMMAND_SUB_ARENA_FUNCTION_COPY_INVALID_SOURCE_ARENA_ID, sender, true,
                                sourceArenaIdEntered);
                        UtilHelpList.sendSuggest(sender, 3,
                                arenaManager.getList().stream().map(BaseArena::getId).collect(Collectors.toList()),
                                label, args);
                        return true;
                    }
                    String targetArenaIdEntered = args[4];
                    BaseArena targetArena = arenaManager.get(targetArenaIdEntered);
                    if (targetArena == null) {
                        MsgBuilder.sendClickable(Msg.COMMAND_SUB_ARENA_FUNCTION_COPY_INVALID_TARGET_ARENA_ID, sender, true,
                                sourceArenaIdEntered);
                        UtilHelpList.sendSuggest(sender, 4,
                                arenaManager.getList().stream().map(BaseArena::getId).collect(Collectors.toList()),
                                label, args);
                        return true;
                    }
                    if (!targetArena.getArenaTypeId().equals(sourceArena.getArenaTypeId())) {
                        MsgBuilder.send(Msg.COMMAND_SUB_ARENA_FUNCTION_COPY_DIFFERENT_TYPE, sender,
                                sourceArena.getName(), sourceArena.getArenaType().getName(sender),
                                targetArena.getName(), targetArena.getArenaType().getName(sender));
                        return true;
                    }
                    String functionIdEntered = args[5];
                    ArenaType arenaType = sourceArena.getArenaType();
                    if (!arenaType.getFunctionDef().containsKey(functionIdEntered)) {
                        MsgBuilder.sendClickable(Msg.COMMAND_SUB_ARENA_FUNCTION_INVALID_ID, sender, true,
                                arenaType.getName(sender), functionIdEntered, arenaType.getId());
                        UtilHelpList.sendSuggest(sender, 5,
                                sourceArena.getArenaType().getFunctionDef().keySet(),
                                label, args);
                        return true;
                    }
                    if (!sourceArena.getArenaData().hasFunction(functionIdEntered)) {
                        MsgBuilder.sendClickable(Msg.COMMAND_SUB_ARENA_FUNCTION_COPY_EMPTY_FUNCTION, sender, true,
                                sourceArena.getName(), functionIdEntered, arenaType.getName(sender), arenaType.getId());
                        return true;
                    }
                    //上述已经确认满足所有复制条件了，则再根据目标竞技场原先是否有这个功能，来选择性提示复制后的功能是添加过去还是覆盖上去
                    boolean hasBefore = targetArena.getArenaData().hasFunction(functionIdEntered);
                    targetArena.getArenaData().addFunction(functionIdEntered, sourceArena.getArenaData().getFunctionData(functionIdEntered));
                    arenaManager.update(targetArena.getArenaData());
                    MsgBuilder.send(hasBefore ? Msg.COMMAND_SUB_ARENA_FUNCTION_COPY_SUCCESSFULLY_OVERWRITE : Msg.COMMAND_SUB_ARENA_FUNCTION_COPY_SUCCESSFULLY, sender,
                            sourceArena.getName(), targetArena.getName(), functionIdEntered);
                }
                return true;
            } else {
                if (isAdd || isRemove || isReset || isClear || isList) {
                    //确认输入的是function系列的add remove reset clear list的那一个子指令
                    if (isAdd) singleCommand = helpList.getSubCommandById("function_add");
                    if (isRemove) singleCommand = helpList.getSubCommandById("function_remove");
                    if (isReset) singleCommand = helpList.getSubCommandById("function_reset");
                    if (isClear) singleCommand = helpList.getSubCommandById("function_clear");
                    if (isList) singleCommand = helpList.getSubCommandById("function_list");
                    if (args.length < ((isAdd || isRemove || isReset) ? 5 : 4)) {
                        helpList.sendCorrect(sender, -1, singleCommand, label, args);
                        return true;
                    }
                    String arenaIdEntered = args[3];
                    BaseArena arena = arenaManager.get(arenaIdEntered);
                    if (arena == null) {
                        MsgBuilder.sendClickable(Msg.COMMAND_SUB_ARENA_INVALID_ID, sender, true,
                                arenaIdEntered);
                        UtilHelpList.sendSuggest(sender, 3,
                                arenaManager.getList().stream().map(BaseArena::getId).collect(Collectors.toList()),
                                label, args);
                        return true;
                    }
                    BaseArenaData arenaData = arena.getArenaData();
                    ArenaType arenaType = arenaTypeManager.get(arena.getArenaTypeId());
                    if (isAdd || isRemove || isReset) {
                        String functionIdEntered = args[4];
                        if (!functionIdEntered.contains(":")) functionIdEntered = "dueltime:" + functionIdEntered;
                        ArenaType.Function functionsDefined = arenaType.getFunctionDef().get(functionIdEntered);
                        if (functionsDefined == null) {
                            MsgBuilder.sendClickable(Msg.COMMAND_SUB_ARENA_FUNCTION_INVALID_ID, sender, true,
                                    arenaType.getName(sender), functionIdEntered, arenaType.getId());
                            UtilHelpList.sendSuggest(sender, 4,
                                    arenaType.getFunctionDef().keySet(),
                                    label, args);
                            return true;
                        }
                        if (isAdd) {
                            if (arenaData.hasFunction(functionIdEntered)) {
                                helpList.sendCorrect(sender, 4, singleCommand, label, args);
                                MsgBuilder.send(Msg.COMMAND_SUB_ARENA_FUNCTION_ADD_REPEATEDLY, sender,
                                        arenaData.getName(), functionIdEntered);
                                return true;
                            }
                            if (functionsDefined.getProgressId() == null) {
                                //没有声明上传数据的步骤ID，则说明无需数据，则直接更新Arena对象并提示附加功能添加成功
                                arenaData.addFunction(functionIdEntered, null);
                                arenaManager.update(arenaData);
                                MsgBuilder.send(Msg.COMMAND_SUB_ARENA_FUNCTION_ADD_SUCCESSFULLY, sender,
                                        arenaData.getName(), arenaType.getFunctionDef().get(functionIdEntered).getName(sender));
                            } else {
                                //需要上传数据，则先让玩家进入上传数据的过程(Progress)
                                if (!(sender instanceof Player)) {
                                    MsgBuilder.send(Msg.ERROR_NOT_PLAYER_EXECUTOR, sender);
                                    return true;
                                }
                                MsgBuilder.send(Msg.COMMAND_SUB_ARENA_FUNCTION_ADD_ENTER_PROGRESS, sender,
                                        arenaData.getName(), functionsDefined.getName(sender));
                                Player player = (Player) sender;
                                DuelTimePlugin.getInstance().getProgressManager().enter(player,
                                        ProgressType.addFunction(player,
                                                arenaIdEntered,
                                                arenaType.getId(),
                                                functionIdEntered,
                                                functionsDefined.getProgressId()));

                            }
                        } else if (isRemove) {
                            if (!arenaData.hasFunction(functionIdEntered)) {
                                helpList.sendCorrect(sender, 4, singleCommand, label, args);
                                MsgBuilder.send(Msg.COMMAND_SUB_ARENA_FUNCTION_REMOVE_FAIL_NOT_EXISTS, sender,
                                        arenaData.getName(),
                                        functionIdEntered);
                                return true;
                            }
                            arenaData.removeFunction(functionIdEntered);
                            arenaManager.update(arenaData);
                            MsgBuilder.send(Msg.COMMAND_SUB_ARENA_FUNCTION_REMOVE_SUCCESSFULLY, sender,
                                    arenaData.getName(),
                                    functionIdEntered);
                        } else {
                            if (!arenaData.hasFunction(functionIdEntered)) {
                                helpList.sendCorrect(sender, 4, singleCommand, label, args);
                                MsgBuilder.send(Msg.COMMAND_SUB_ARENA_FUNCTION_RESET_FAIL_NOT_EXISTS, sender,
                                        arenaData.getName(), functionIdEntered);
                                return true;
                            }
                            if (functionsDefined.getTemplateSteps() == null) {
                                MsgBuilder.send(Msg.COMMAND_SUB_ARENA_FUNCTION_RESET_FAIL_NO_DATA_NEEDED, sender);
                                return true;
                            }
                            MsgBuilder.send(Msg.COMMAND_SUB_ARENA_FUNCTION_RESET_ENTER_PROGRESS, sender,
                                    arenaData.getName(), functionsDefined.getName(sender));
                            Player player = (Player) sender;
                            DuelTimePlugin.getInstance().getProgressManager().enter(player,
                                    ProgressType.addFunction(player,
                                            arenaIdEntered,
                                            arenaType.getId(),
                                            functionIdEntered,
                                            functionsDefined.getProgressId()));
                        }
                    } else {
                        if (isClear) {
                            arenaData.setFunctions(null);
                            arenaManager.update(arenaData);
                            MsgBuilder.send(Msg.COMMAND_SUB_ARENA_FUNCTION_CLEAR_SUCCESSFULLY, sender);
                        } else {
                            HashMap<String, Object[]> functions = arenaData.getFunctions();
                            if (functions == null) {
                                MsgBuilder.send(Msg.COMMAND_SUB_ARENA_FUNCTION_LIST_EMPTY, sender,
                                        arenaData.getName());
                                return true;
                            }
                            MsgBuilder.send(Msg.COMMAND_SUB_ARENA_FUNCTION_LIST_HEADING, sender,
                                    arenaData.getName());
                            int index = 0;
                            for (Map.Entry<String, Object[]> kv : functions.entrySet()) {
                                index++;
                                String functionId = kv.getKey();
                                Object[] data = kv.getValue();
                                ArenaType.Function function = arenaType.getFunctionDef().get(functionId);
                                MsgBuilder.sends(Msg.COMMAND_SUB_ARENA_FUNCTION_LIST_BODY, sender,
                                        "" + index,
                                        function.getName(sender),
                                        functionId,
                                        function.getDescription(sender),
                                        data == null ? MsgBuilder.get(Msg.STRING_NONE, sender) : UtilFormat.toString(data));
                            }
                            MsgBuilder.send(Msg.COMMAND_SUB_ARENA_FUNCTION_LIST_ENDING, sender,
                                    "" + functions.size());
                        }
                    }
                    return true;
                }
            }
            UtilHelpList.sendSuggest(sender, 2, Arrays.asList("add", "remove", "reset", "clear", "list", "type", "copy"), label, args);
            return true;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 2) {
            // 第二级tab补全：显示arena子命令
            List<String> subCommands = Arrays.asList("list", "type", "create", "view", "delete", "function");
            return subCommands.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 3 && args[1].equalsIgnoreCase("function")) {
            // function子命令的第三级tab补全
            List<String> functionSubCommands = Arrays.asList("add", "remove", "reset", "clear", "list", "type", "copy");
            return functionSubCommands.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length >= 3) {
            // 竞技场ID的tab补全
            ArenaManager arenaManager = DuelTimePlugin.getInstance().getArenaManager();
            List<String> arenaIds = arenaManager.getList().stream()
                    .map(BaseArena::getId)
                    .collect(Collectors.toList());
            
            if (args.length == 3 && (args[1].equalsIgnoreCase("view") || args[1].equalsIgnoreCase("delete"))) {
                return arenaIds.stream()
                        .filter(id -> id.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args.length == 4 && args[1].equalsIgnoreCase("function")) {
                if (args[2].equalsIgnoreCase("add") || args[2].equalsIgnoreCase("remove") || 
                    args[2].equalsIgnoreCase("reset") || args[2].equalsIgnoreCase("clear") || 
                    args[2].equalsIgnoreCase("list")) {
                    return arenaIds.stream()
                            .filter(id -> id.toLowerCase().startsWith(args[3].toLowerCase()))
                            .collect(Collectors.toList());
                }
            } else if (args.length == 5 && args[1].equalsIgnoreCase("function")) {
                if (args[2].equalsIgnoreCase("add") || args[2].equalsIgnoreCase("remove") || 
                    args[2].equalsIgnoreCase("reset")) {
                    // 功能ID的tab补全
                    BaseArena arena = arenaManager.get(args[3]);
                    if (arena != null) {
                        ArenaType arenaType = arena.getArenaType();
                        if (arenaType != null) {
                            return arenaType.getFunctionDef().keySet().stream()
                                    .filter(id -> id.toLowerCase().startsWith(args[4].toLowerCase()))
                                    .collect(Collectors.toList());
                        }
                    }
                }
            }
        }
        return null;
    }
}
