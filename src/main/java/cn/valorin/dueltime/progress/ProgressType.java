package cn.valorin.dueltime.progress;

import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.arena.type.ArenaType;
import cn.valorin.dueltime.yaml.message.Msg;
import org.bukkit.entity.Player;

public class ProgressType {
    public static Progress createArena(Player player, ArenaType arenaType) {
        boolean isBossBarUsed = DuelTimePlugin.serverVersionInt >= 9;
        return new Progress(
                DuelTimePlugin.getInstance(),
                arenaType.getCreateProgressId(),
                arenaType.getCreateProgressName(player),
                player,
                null,
                isBossBarUsed,
                arenaType.getCreateTemplateSteps()
        );
    }

    public static Progress addFunction(Player player, String arenaId, String arenaTypeId, String functionTypeId, String progressId) {
        ArenaType arenaType = DuelTimePlugin.getInstance().getArenaTypeManager().get(arenaTypeId);
        ArenaType.Function function = arenaType.getFunctionDef().get(functionTypeId);
        Step[] templateSteps = function.getTemplateSteps();
        Step[] steps = new Step[templateSteps.length];
        for (int i = 0; i < templateSteps.length; i++) {
            Step templateStep = templateSteps[i];
            steps[i] = new Step(templateStep.getTip(), templateStep.getFinishTitle(), templateStep.getFinishSubTitle(), player, templateStep.getDataType(), templateStep.isAutoUpload(), templateStep.getAutoUploadTags());
        }
        boolean isBossBarUsed = DuelTimePlugin.serverVersionInt >= 9;
        return new Progress(DuelTimePlugin.getInstance(), progressId, Msg.PROGRESS_TYPE_UPLOAD_DATA_FOR_ARENA_FUNCTION, player, arenaId, isBossBarUsed, steps);
    }

    public enum InternalType {
        CREATE_CLASSIC_ARENA("dueltime:create_classic_arena"),
        ADD_FUNCTION_CLASSIC_TIME_LIMIT("dueltime:add_function_classic_time_limit"),
        ADD_FUNCTION_CLASSIC_COUNTDOWN("dueltime:add_function_classic_countdown"),
        ADD_FUNCTION_CLASSIC_INVENTORY_CHECK_KEYWORD("dueltime:add_function_classic_inventory_check_keyword"),
        ADD_FUNCTION_CLASSIC_INVENTORY_CHECK_TYPE("dueltime:add_function_classic_inventory_check_type"),
        ADD_FUNCTION_CLASSIC_PRE_GAME_COMMAND("dueltime:add_function_classic_pre_game_command"),
        ADD_FUNCTION_CLASSIC_SPECTATE("dueltime:add_function_classic_spectate"),
        ADD_FUNCTION_CLASSIC_BAN_ENTITY_SPAWN("dueltime:add_function_classic_ban_entity_spawn");

        private final String id;

        public String getId() {
            return id;
        }

        InternalType(String id) {
            this.id = id;
        }
    }
}
