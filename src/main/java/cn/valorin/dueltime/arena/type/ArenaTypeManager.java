package cn.valorin.dueltime.arena.type;

import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.progress.ProgressType;
import cn.valorin.dueltime.progress.Step;
import cn.valorin.dueltime.viaversion.ViaVersionItem;
import cn.valorin.dueltime.yaml.message.Msg;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.valorin.dueltime.arena.type.ArenaType.FunctionInternalType.*;
import static cn.valorin.dueltime.progress.Step.AutoUploadTag.*;

public class ArenaTypeManager {
    private final List<ArenaType> arenaTypeList = new ArrayList<>();

    public ArenaTypeManager() {
        reload();
    }

    /**
     * （重新）载入所有种类定义
     */
    public void reload() {
        Step[] steps = new Step[]{
                //点击方块确定空间对角点1
                new Step(
                        Msg.PROGRESS_TYPE_CREATE_CLASSIC_ARENA_STEP_1_TIP,
                        Msg.PROGRESS_TYPE_CREATE_CLASSIC_ARENA_STEP_1_TITLE,
                        Msg.PROGRESS_TYPE_CREATE_CLASSIC_ARENA_STEP_1_SUBTITLE,
                        null,
                        Location.class,
                        true,
                        LOCATION_CONDITION_CLICK_BLOCK),
                //点击方块确定空间对角点2
                new Step(
                        Msg.PROGRESS_TYPE_CREATE_CLASSIC_ARENA_STEP_2_TIP,
                        Msg.PROGRESS_TYPE_CREATE_CLASSIC_ARENA_STEP_2_TITLE,
                        Msg.PROGRESS_TYPE_CREATE_CLASSIC_ARENA_STEP_2_SUBTITLE,
                        null,
                        Location.class,
                        true,
                        LOCATION_CONDITION_CLICK_BLOCK,
                        LOCATION_CONDITION_THE_SAME_WORLD,
                        LOCATION_CONDITION_DIFFERENT_BLOCK,
                        LOCATION_CONDITION_CANNOT_OVERLAP_WITH_OTHER_ARENA),
                //点击空气确定传送点1
                new Step(
                        Msg.PROGRESS_TYPE_CREATE_CLASSIC_ARENA_STEP_3_TIP,
                        Msg.PROGRESS_TYPE_CREATE_CLASSIC_ARENA_STEP_3_TITLE,
                        Msg.PROGRESS_TYPE_CREATE_CLASSIC_ARENA_STEP_3_SUBTITLE,
                        null,
                        Location.class,
                        true,
                        LOCATION_CONDITION_CLICK_AIR),
                //点击空气确定传送点2
                new Step(
                        Msg.PROGRESS_TYPE_CREATE_CLASSIC_ARENA_STEP_4_TIP,
                        Msg.PROGRESS_TYPE_CREATE_CLASSIC_ARENA_STEP_4_TITLE,
                        Msg.PROGRESS_TYPE_CREATE_CLASSIC_ARENA_STEP_4_SUBTITLE,
                        null,
                        Location.class,
                        true,
                        LOCATION_CONDITION_CLICK_AIR,
                        LOCATION_CONDITION_THE_SAME_WORLD),
                //输入竞技场ID
                new Step(
                        Msg.PROGRESS_TYPE_CREATE_CLASSIC_ARENA_STEP_5_TIP,
                        Msg.PROGRESS_TYPE_CREATE_CLASSIC_ARENA_STEP_5_TITLE,
                        Msg.PROGRESS_TYPE_CREATE_CLASSIC_ARENA_STEP_5_SUBTITLE,
                        null,
                        String.class,
                        true,
                        STRING_CONDITION_ID_STYLE),
                //输入竞技场展示名
                new Step(
                        Msg.PROGRESS_TYPE_CREATE_CLASSIC_ARENA_STEP_6_TIP,
                        Msg.PROGRESS_TYPE_CREATE_CLASSIC_ARENA_STEP_6_TITLE,
                        Msg.PROGRESS_TYPE_CREATE_CLASSIC_ARENA_STEP_6_SUBTITLE,
                        null,
                        String.class,
                        true,
                        STRING_FUNCTION_REPLACE_BLANK, STRING_FUNCTION_REPLACE_COLOR_SYMBOL)
        };
        Map<String, ArenaType.Function> functionDef =
                new HashMap<String, ArenaType.Function>() {{
                    //时间限制
                    put(CLASSIC_TIME_LIMIT.getId(), new ArenaType.Function(CLASSIC_TIME_LIMIT.getId(), Msg.ARENA_TYPE_CLASSIC_FUNCTION_TIME_LIMIT_NAME, Msg.ARENA_TYPE_CLASSIC_FUNCTION_TIME_LIMIT_DESCRIPTION,
                            DuelTimePlugin.getInstance(),
                            ProgressType.InternalType.ADD_FUNCTION_CLASSIC_TIME_LIMIT.getId(),
                            //输入秒数
                            new Step(
                                    Msg.PROGRESS_TYPE_SET_FUNCTION_TIME_LIMIT_STEP_1_TIP,
                                    Msg.PROGRESS_TYPE_SET_FUNCTION_TIME_LIMIT_STEP_1_TITLE,
                                    Msg.PROGRESS_TYPE_SET_FUNCTION_TIME_LIMIT_STEP_1_SUBTITLE,
                                    null,
                                    Integer.class,
                                    true,
                                    INTEGER_CONDITION_POSITIVE_VALUE)));
                    //赛前倒计时
                    put(CLASSIC_COUNTDOWN.getId(), new ArenaType.Function(CLASSIC_COUNTDOWN.getId(), Msg.ARENA_TYPE_CLASSIC_FUNCTION_COUNTDOWN_NAME, Msg.ARENA_TYPE_CLASSIC_FUNCTION_COUNTDOWN_DESCRIPTION,
                            DuelTimePlugin.getInstance(),
                            ProgressType.InternalType.ADD_FUNCTION_CLASSIC_COUNTDOWN.getId(),
                            //输入秒数
                            new Step(
                                    Msg.PROGRESS_TYPE_SET_FUNCTION_COUNTDOWN_STEP_1_TIP,
                                    Msg.PROGRESS_TYPE_SET_FUNCTION_COUNTDOWN_STEP_1_TITLE,
                                    Msg.PROGRESS_TYPE_SET_FUNCTION_COUNTDOWN_STEP_1_SUBTITLE,
                                    null,
                                    Integer.class,
                                    true,
                                    INTEGER_CONDITION_POSITIVE_VALUE),
                            //输入T/F表示倒计时期间是否可以移动
                            new Step(
                                    Msg.PROGRESS_TYPE_SET_FUNCTION_COUNTDOWN_STEP_2_TIP,
                                    Msg.PROGRESS_TYPE_SET_FUNCTION_COUNTDOWN_STEP_2_TITLE,
                                    Msg.PROGRESS_TYPE_SET_FUNCTION_COUNTDOWN_STEP_2_SUBTITLE,
                                    null,
                                    Boolean.class,
                                    true)));
                    //赛前背包检测（依关键词)
                    put(CLASSIC_INVENTORY_CHECK_KEYWORD.getId(), new ArenaType.Function(CLASSIC_INVENTORY_CHECK_KEYWORD.getId(), Msg.ARENA_TYPE_CLASSIC_FUNCTION_INVENTORY_CHECK_KEYWORD_NAME, Msg.ARENA_TYPE_CLASSIC_FUNCTION_INVENTORY_CHECK_KEYWORD_DESCRIPTION,
                            DuelTimePlugin.getInstance(),
                            ProgressType.InternalType.ADD_FUNCTION_CLASSIC_INVENTORY_CHECK_KEYWORD.getId(),
                            //输入检查的范围，name代表展示名，lore代表描述，all代表都检查
                            new Step(
                                    Msg.PROGRESS_TYPE_SET_FUNCTION_INVENTORY_CHECK_KEYWORD_STEP_1_TIP,
                                    Msg.PROGRESS_TYPE_SET_FUNCTION_INVENTORY_CHECK_KEYWORD_STEP_1_TITLE,
                                    Msg.PROGRESS_TYPE_SET_FUNCTION_INVENTORY_CHECK_KEYWORD_STEP_1_SUBTITLE,
                                    null,
                                    String.class,
                                    false),
                            //输入列表
                            new Step(
                                    Msg.PROGRESS_TYPE_SET_FUNCTION_INVENTORY_CHECK_KEYWORD_STEP_2_TIP,
                                    Msg.PROGRESS_TYPE_SET_FUNCTION_INVENTORY_CHECK_KEYWORD_STEP_2_TITLE,
                                    Msg.PROGRESS_TYPE_SET_FUNCTION_INVENTORY_CHECK_KEYWORD_STEP_2_SUBTITLE,
                                    null,
                                    List.class,
                                    true)));
                    //赛前背包检测（依物品种类)
                    put(CLASSIC_INVENTORY_CHECK_TYPE.getId(), new ArenaType.Function(CLASSIC_INVENTORY_CHECK_TYPE.getId(), Msg.ARENA_TYPE_CLASSIC_FUNCTION_INVENTORY_CHECK_TYPE_NAME, Msg.ARENA_TYPE_CLASSIC_FUNCTION_INVENTORY_CHECK_TYPE_DESCRIPTION,
                            DuelTimePlugin.getInstance(),
                            ProgressType.InternalType.ADD_FUNCTION_CLASSIC_INVENTORY_CHECK_TYPE.getId(),
                            //输入列表，元素为字符串-整数对
                            new Step(
                                    Msg.PROGRESS_TYPE_SET_FUNCTION_INVENTORY_CHECK_TYPE_STEP_1_TIP,
                                    Msg.PROGRESS_TYPE_SET_FUNCTION_INVENTORY_CHECK_TYPE_STEP_1_TITLE,
                                    Msg.PROGRESS_TYPE_SET_FUNCTION_INVENTORY_CHECK_TYPE_STEP_1_SUBTITLE,
                                    null,
                                    List.class,
                                    true,
                                    LIST_CONDITION_STRING_INTEGER_PAIR_LOOSE,
                                    STRING_FUNCTION_TO_UPPERCASE)));
                    //赛前入场后指令
                    put(CLASSIC_PRE_GAME_COMMAND.getId(), new ArenaType.Function(CLASSIC_PRE_GAME_COMMAND.getId(), Msg.ARENA_TYPE_CLASSIC_FUNCTION_PRE_GAME_COMMAND_NAME, Msg.ARENA_TYPE_CLASSIC_FUNCTION_PRE_GAME_COMMAND_DESCRIPTION,
                            DuelTimePlugin.getInstance(),
                            ProgressType.InternalType.ADD_FUNCTION_CLASSIC_PRE_GAME_COMMAND.getId(),
                            //输入列表，元素为字符串对，前者为身份名，后者为指令
                            new Step(
                                    Msg.PROGRESS_TYPE_SET_FUNCTION_PRE_GAME_COMMAND_STEP_1_TIP,
                                    Msg.PROGRESS_TYPE_SET_FUNCTION_PRE_GAME_COMMAND_STEP_1_TITLE,
                                    Msg.PROGRESS_TYPE_SET_FUNCTION_PRE_GAME_COMMAND_STEP_1_SUBTITLE,
                                    null,
                                    List.class,
                                    true,
                                    LIST_CONDITION_IDENTITY_COMMAND_PAIR)));
                    //观战
                    put(CLASSIC_SPECTATE.getId(), new ArenaType.Function(CLASSIC_SPECTATE.getId(), Msg.ARENA_TYPE_CLASSIC_FUNCTION_SPECTATE_NAME, Msg.ARENA_TYPE_CLASSIC_FUNCTION_SPECTATE_DESCRIPTION,
                            DuelTimePlugin.getInstance(),
                            ProgressType.InternalType.ADD_FUNCTION_CLASSIC_SPECTATE.getId(),
                            //传入观战席区域A点
                            new Step(
                                    Msg.PROGRESS_TYPE_SET_FUNCTION_SPECTATOR_STEP_1_TIP,
                                    Msg.PROGRESS_TYPE_SET_FUNCTION_SPECTATOR_STEP_1_TITLE,
                                    Msg.PROGRESS_TYPE_SET_FUNCTION_SPECTATOR_STEP_1_SUBTITLE,
                                    null,
                                    Location.class,
                                    true,
                                    LOCATION_CONDITION_CLICK_BLOCK),
                            //传入观战席区域B点
                            new Step(
                                    Msg.PROGRESS_TYPE_SET_FUNCTION_SPECTATOR_STEP_2_TIP,
                                    Msg.PROGRESS_TYPE_SET_FUNCTION_SPECTATOR_STEP_2_TITLE,
                                    Msg.PROGRESS_TYPE_SET_FUNCTION_SPECTATOR_STEP_2_SUBTITLE,
                                    null,
                                    Location.class,
                                    true,
                                    LOCATION_CONDITION_CLICK_BLOCK,
                                    LOCATION_CONDITION_DIFFERENT_BLOCK,
                                    LOCATION_CONDITION_THE_SAME_WORLD),
                            //传入观众传送点
                            new Step(
                                    Msg.PROGRESS_TYPE_SET_FUNCTION_SPECTATOR_STEP_3_TIP,
                                    Msg.PROGRESS_TYPE_SET_FUNCTION_SPECTATOR_STEP_3_TITLE,
                                    Msg.PROGRESS_TYPE_SET_FUNCTION_SPECTATOR_STEP_3_SUBTITLE,
                                    null,
                                    Location.class,
                                    true,
                                    LOCATION_CONDITION_CLICK_AIR),
                            //传入是否向观众实时展示对战双方血量
                            new Step(
                                    Msg.PROGRESS_TYPE_SET_FUNCTION_SPECTATOR_STEP_4_TIP,
                                    Msg.PROGRESS_TYPE_SET_FUNCTION_SPECTATOR_STEP_4_TITLE,
                                    Msg.PROGRESS_TYPE_SET_FUNCTION_SPECTATOR_STEP_4_SUBTITLE,
                                    null,
                                    Boolean.class,
                                    true)
                    ));
                    put(CLASSIC_BAN_ENTITY_SPAWN.getId(), new ArenaType.Function(CLASSIC_BAN_ENTITY_SPAWN.getId(), Msg.ARENA_TYPE_CLASSIC_FUNCTION_BAN_ENTITY_SPAWN_NAME, Msg.ARENA_TYPE_CLASSIC_FUNCTION_BAN_ENTITY_SPAWN_DESCRIPTION,
                            DuelTimePlugin.getInstance(),
                            ProgressType.InternalType.ADD_FUNCTION_CLASSIC_BAN_ENTITY_SPAWN.getId(),
                            new Step(Msg.PROGRESS_TYPE_SET_FUNCTION_BAN_ENTITY_SPAWN_STEP_1_TIP,
                                    Msg.PROGRESS_TYPE_SET_FUNCTION_BAN_ENTITY_SPAWN_STEP_1_TITLE,
                                    Msg.PROGRESS_TYPE_SET_FUNCTION_BAN_ENTITY_SPAWN_STEP_1_SUBTITLE,
                                    null,
                                    List.class,
                                    true,
                                    LIST_CONDITION_NULLABLE)));
                }};
        Map<ArenaType.PresetType, Object> presets =
                new HashMap<ArenaType.PresetType, Object>() {{
                    put(ArenaType.PresetType.START_ICON, ViaVersionItem.getMapMaterial());
                    put(ArenaType.PresetType.PROTECTION_BREAK, null);
                    put(ArenaType.PresetType.PROTECTION_PLACE, null);
                    put(ArenaType.PresetType.PROTECTION_INTERACT, null);
                    put(ArenaType.PresetType.PROTECTION_POUR_LIQUID, null);
                    put(ArenaType.PresetType.PROTECTION_GET_LIQUID, null);
                    put(ArenaType.PresetType.PROTECTION_ENTITY_BREAK_DOOR, null);
                    //put(ArenaType.PresetType.PROTECTION_ENTITY_SPAWN, null);改成自定义
                    put(ArenaType.PresetType.PROTECTION_BLOCK_IGNITED, null);
                    put(ArenaType.PresetType.PROTECTION_BLOCK_BURNING, null);
                }};
        arenaTypeList.add(
                new ArenaType(
                        //本插件实例
                        DuelTimePlugin.getInstance(),
                        //经典竞技场ID
                        ArenaType.InternalType.CLASSIC.getId(),
                        //经典竞技场展示名
                        Msg.ARENA_TYPE_CLASSIC_NAME,
                        //创建经典竞技场的过程ID
                        ProgressType.InternalType.CREATE_CLASSIC_ARENA.getId(),
                        //创建经典竞技场的过程名称
                        Msg.PROGRESS_TYPE_CREATE_CLASSIC_ARENA_NAME,
                        //创建经典竞技场过程的模板步骤
                        steps,
                        //经典竞技场所定义的附加功能
                        functionDef,
                        //经典竞技场所采用的预设
                        presets
                ));
    }

    public ArenaType get(String id) {
        if (!id.contains(":")) id = "dueltime:" + id;
        for (ArenaType type : arenaTypeList) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }

    public List<ArenaType> getList() {
        return arenaTypeList;
    }
}
