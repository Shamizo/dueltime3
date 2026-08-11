package cn.valorin.dueltime.arena.type;

import cn.valorin.dueltime.progress.Step;
import cn.valorin.dueltime.util.UtilFormat;
import cn.valorin.dueltime.yaml.message.Msg;
import cn.valorin.dueltime.yaml.message.MsgBuilder;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 竞技场类型
 * 用于声明某个类型的竞技场的各种属性
 * 如id、类型名、附加功能定义等
 */
public class ArenaType {
    //竞技场类型的唯一标识
    private final String id;
    //竞技场类型名称
    private final Object name;
    private final String createProgressId;
    private final Object createProgressName;
    private final Step[] createTemplateSteps;
    //附加功能(Function)
    private final Map<String, Function> functionDef;
    //预设
    private final Map<PresetType, Object> presets;


    public ArenaType(Plugin plugin, String id, Object name, String createProgressId, Object createProgressName, Step[] createTemplateSteps, Map<String, Function> functionDef, Map<PresetType, Object> presets) {
        if (plugin == null) {
            throw new NullPointerException("The plugin cannot be null");
        }
        if (!id.contains(":") || !id.split(":")[0].equals(plugin.getDescription().getName().toLowerCase()) || !UtilFormat.isIDStyle(id.split(":")[1])) {
            throw new IllegalArgumentException("The format of the 2nd argument must be 'the lowercase of your plugin' + ':' + 'id',for example,'dueltime:test',and the id can only consist of English and number");
        }
        this.id = id;
        if (!(name instanceof String) && !(name instanceof Msg)) {
            throw new IllegalArgumentException("The 3rd argument must be String or Msg");
        }
        this.createProgressId = createProgressId;
        if (!(createProgressName instanceof String) && !(createProgressName instanceof Msg)) {
            throw new IllegalArgumentException("The 5th argument must be String or Msg");
        }
        this.name = name;
        this.createProgressName = createProgressName;
        this.createTemplateSteps = createTemplateSteps;
        this.functionDef = functionDef;
        this.presets = presets == null ? new HashMap<>() : presets;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return (String) name;
    }

    public String getName(CommandSender sender) {
        return (name instanceof Msg) ?
                MsgBuilder.get((Msg) name, sender) :
                (String) name;
    }

    public String getCreateProgressId() {
        return createProgressId;
    }

    public String getCreateProgressName() {
        return (String) createProgressName;
    }

    public String getCreateProgressName(CommandSender sender) {
        return (createProgressName instanceof Msg) ?
                MsgBuilder.get((Msg) createProgressName, sender) :
                (String) createProgressName;
    }

    public Step[] getCreateTemplateSteps() {
        return createTemplateSteps;
    }

    public Map<String, Function> getFunctionDef() {
        return functionDef;
    }

    public Map<PresetType, Object> getPresets() {
        return presets;
    }

    public enum InternalType {
        CLASSIC("dueltime:classic");
        private final String id;

        InternalType(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    /**
     * 附加功能定义类
     */
    public static class Function {
        //附加功能的唯一标识
        private final String id;
        //附加功能的名称
        private final Object name;
        //附加功能的描述
        private final Object description;
        //附加功能数据上传的过程ID
        private final String progressId;
        //附加功能数据上传需要的步骤模板
        private final Step[] templateSteps;

        public Function(String id, Object name, Object description, Plugin plugin, String progressId, Step... templateSteps) {
            if (!(name instanceof String) && !(name instanceof Msg)) {
                throw new IllegalArgumentException("The 2nd argument must be String or Msg");
            }
            if (!(description instanceof String) && !(description instanceof Msg)) {
                throw new IllegalArgumentException("The 3rd argument must be String or Msg");
            }
            this.id = id;
            this.name = name;
            this.description = description;
            if (progressId != null && (!progressId.contains(":") || !progressId.split(":")[0].equals(plugin.getDescription().getName().toLowerCase()) || !UtilFormat.isIDStyle(progressId.split(":")[1]))) {
                throw new IllegalArgumentException("The format of the 5th argument must be 'the lowercase of your plugin' + ':' + 'id',for example,'dueltime:test',and the id can only consist of English and numbers");
            }
            this.progressId = progressId;
            this.templateSteps = templateSteps;
        }

        public Function(String id, Object name, Object description) {
            this(id, name, description, null, null);
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return (String) name;
        }

        public String getName(CommandSender sender) {
            return MsgBuilder.get((Msg) name, sender);
        }

        public String getDescription() {
            return (String) description;
        }

        public String getDescription(CommandSender sender) {
            return MsgBuilder.get((Msg) description, sender);
        }

        public String getProgressId() {
            return progressId;
        }

        public Step[] getTemplateSteps() {
            return templateSteps;
        }
    }

    public enum PresetType {
        START_ICON(Material.class),//开始界面中的图标种类，数据类型为Material
        PROTECTION_BREAK(List.class),//进行比赛时，阻止玩家破坏方块，数据类型为List<Material>，为可破坏种类的白名单
        PROTECTION_PLACE(List.class),//进行比赛时，阻止玩家放置方块，数据类型为List<Material>，为可放置种类的白名单
        PROTECTION_INTERACT(List.class),//进行比赛时，阻止玩家交互，数据类型为List<Material>，为可交互种类的白名单
        PROTECTION_POUR_LIQUID(List.class),//进行比赛时，阻止玩家倾倒液体，数据类型为List<Material>，为可倾倒种类的白名单
        PROTECTION_GET_LIQUID(List.class),//进行比赛时，阻止玩家捞取液体，数据类型为List<Material>，为可捞取种类的白名单
        PROTECTION_ENTITY_BREAK_DOOR(null),//进行比赛时，阻止实体破坏门。无数据
        PROTECTION_ENTITY_SPAWN(List.class),//进行比赛时，阻止实体生成，数据类型为List<EntityType>，为可生成种类的白名单
        PROTECTION_BLOCK_IGNITED(List.class),//进行比赛时，阻止方块被点燃，数据类型为List<Material>，为可点燃种类的白名单
        PROTECTION_BLOCK_BURNING(List.class);//进行比赛时，阻止方块持续燃烧，数据类型为List<Material>，为可持续燃烧种类的白名单

        private final Class<?> dataType;

        PresetType(Class<?> dataType) {
            this.dataType = dataType;
        }

        public Class<?> getDataType() {
            return dataType;
        }
    }

    public enum FunctionInternalType {
        CLASSIC_TIME_LIMIT("dueltime:time_limit"),
        CLASSIC_COUNTDOWN("dueltime:countdown"),
        CLASSIC_INVENTORY_CHECK_KEYWORD("dueltime:inventory_check_keyword"),
        CLASSIC_INVENTORY_CHECK_TYPE("dueltime:inventory_check_type"),
        CLASSIC_PRE_GAME_COMMAND("dueltime:pre_game_command"),
        CLASSIC_SPECTATE("dueltime:spectate"),
        CLASSIC_BAN_ENTITY_SPAWN("dueltime:ban_entity_spawn");

        private final String id;

        FunctionInternalType(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }
}
