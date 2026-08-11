package cn.valorin.dueltime.listener.arena;

import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.arena.ArenaManager;
import cn.valorin.dueltime.arena.base.BaseArena;
import cn.valorin.dueltime.arena.type.ArenaType;
import cn.valorin.dueltime.command.sub.CommandPermission;
import cn.valorin.dueltime.event.arena.ArenaEndEvent;
import cn.valorin.dueltime.stats.Metrics;
import cn.valorin.dueltime.util.UtilGeometry;
import cn.valorin.dueltime.util.UtilSync;
import cn.valorin.dueltime.yaml.message.Msg;
import cn.valorin.dueltime.yaml.message.MsgBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.*;

import java.util.HashMap;
import java.util.List;

public class BaseArenaListener implements Listener {
    public static HashMap<String, Long> tempMovePermit = new HashMap<>();

    /*
    阻止玩家进入空闲或停用中的的比赛场地
    阻止任何非选手且非观众的玩家在其中移动
     */
    @EventHandler
    public void moveIn(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission(CommandPermission.ADMIN)) return;
        Location to = event.getTo();
        BaseArena arena = UtilGeometry.getArena(to);
        if (arena != null) {
            if (arena.getState() == BaseArena.State.WAITING || arena.getState() == BaseArena.State.DISABLED) {
                //等待状态或停用状态下，除非有临时移动权限，一律不可入内
                if (tempMovePermit.getOrDefault(player.getName(), 0L) > System.currentTimeMillis()) {
                    return;
                }
                UtilSync.safeTeleport(player, event.getFrom());
                MsgBuilder.sendActionBar(MsgBuilder.get(Msg.ARENA_PROTECTION_WALK_IN_WHILE_AVAILABLE, player, arena.getName()), player, true);
            } else {
                if (!arena.hasPlayer(player) && !arena.hasSpectator(player)) {
                    //比赛进行时，不允许非选手且非观众的玩家入内
                    UtilSync.safeTeleport(player, event.getFrom());
                    MsgBuilder.sendActionBar(MsgBuilder.get(Msg.ARENA_PROTECTION_WALK_IN_WHILE_IN_PROGRESS, player, arena.getName()), player, true);
                }
            }
        }
    }

    /*
    阻止玩家传送到空闲或停用中的比赛场地
    阻止任何非选手且非观众的玩家传送过去
     */
    @EventHandler
    public void teleportTo(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission(CommandPermission.ADMIN)) return;
        Location to = event.getTo();
        BaseArena arena = UtilGeometry.getArena(to);
        if (arena != null) {
            if (arena.getState() == BaseArena.State.WAITING || arena.getState() == BaseArena.State.DISABLED) {
                //等待状态或停用状态下，一律不许传送过去
                event.setCancelled(true);
                MsgBuilder.sendActionBar(MsgBuilder.get(Msg.ARENA_PROTECTION_TELEPORT_TO_WHILE_AVAILABLE, player, arena.getName()), player, true);
            } else {
                if (!arena.hasPlayer(player) && !arena.hasSpectator(player)) {
                    //比赛进行时，不允许非选手且非观众的玩家传送过去
                    event.setCancelled(true);
                    MsgBuilder.sendActionBar(MsgBuilder.get(Msg.ARENA_PROTECTION_TELEPORT_TO_WHILE_IN_PROGRESS, player, arena.getName()), player, true);
                }
            }
        }
    }

    /*
    阻止比赛过程中，选手受到来自其他选手以外所有玩家的直接攻击（如近战伤害等）
     */
    @EventHandler
    public void attackGamerDirectly(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player && event.getEntity() instanceof Player)) {
            return;
        }
        Player target = (Player) event.getEntity();
        ArenaManager arenaManager = DuelTimePlugin.getInstance().getArenaManager();
        BaseArena arena = arenaManager.getOf(target);
        if (arena == null) {
            return;
        }
        Player attacker = (Player) event.getDamager();
        BaseArena attackerArena = arenaManager.getOf(attacker);
        if (attackerArena == null) {
            //说明为观众或者无关人员，阻止攻击，并予以提示
            event.setCancelled(true);
            MsgBuilder.sendActionBar(MsgBuilder.get(Msg.ARENA_PROTECTION_ATTACK_GAMER, attacker, arena.getName()), attacker, true);
        } else {
            if (!attackerArena.getId().equals(arena.getId())) {
                //说明攻击方是其他竞技场的，虽然这种情况不太平凡，但还是考虑一下，不予提示
                event.setCancelled(true);
            }
        }
    }

    /*
    阻止比赛过程中，选手受到来自其他选手以外所有玩家的间接攻击（如射击等）
     */
    @EventHandler
    public void attackGamerInDirectly(EntityDamageByEntityEvent event) {
        //考虑到部分种类的枪械模组的兼容性问题，这个暂时不写
    }

    /*
    阻止玩家在空闲的比赛场地破坏
    阻止非选手的玩家在比赛中的场地破坏
    阻止玩家破坏场地外的任何方块
    阻止玩家在比赛过程中破坏比赛场地内白名单以外的方块
     */
    @EventHandler
    public void breakBlock(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission(CommandPermission.ADMIN)) {
            return;
        }
        checkBehaviourWithPlayer(player, event.getBlock().getLocation(), event.getBlock().getType(), event, Msg.ARENA_PROTECTION_BREAK, ArenaType.PresetType.PROTECTION_BREAK);
    }

    /*
    阻止玩家在空闲的比赛场地内部和上空放置方块
    阻止非选手的玩家在比赛中的场地的内部和上空放置方块
    阻止玩家在场地外放置任何方块
    阻止玩家在比赛过程中在比赛场地内部放置白名单以外的方块
     */
    @EventHandler
    public void placeBlock(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission(CommandPermission.ADMIN)) {
            return;
        }
        BaseArena playerArena = DuelTimePlugin.getInstance().getArenaManager().getOf(player);
        BaseArena blockArena = UtilGeometry.getArena(event.getBlock().getLocation());
        if (playerArena == null) {
            boolean isArenaBelow = false;
            if (blockArena == null) {
                blockArena = UtilGeometry.getArenaBelow(event.getBlock().getLocation());
                if (blockArena == null) {
                    return;
                }
                isArenaBelow = true;
            }
            event.setCancelled(true);
            MsgBuilder.sendActionBar(MsgBuilder.get(isArenaBelow ? Msg.ARENA_PROTECTION_PLACE_OVER : Msg.ARENA_PROTECTION_PLACE, player, blockArena.getName()), player, true);
        } else {
            if (blockArena == null || !blockArena.getId().equals(playerArena.getId())) {
                event.setCancelled(true);
            } else {
                Object data = playerArena.getArenaType().getPresets().get(ArenaType.PresetType.PROTECTION_PLACE);
                if (data != null && ((List<Material>) data).contains(event.getBlock().getType())) {
                    return;
                }
                event.setCancelled(true);
            }
        }
    }

    /*
    交互方块的情形，判断规则类似破坏方块的情形
     */
    @EventHandler
    public void interact(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission(CommandPermission.ADMIN)) {
            return;
        }
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        checkBehaviourWithPlayer(player, clickedBlock.getLocation(), clickedBlock.getType(), event, Msg.ARENA_PROTECTION_INTERACT, ArenaType.PresetType.PROTECTION_INTERACT);
    }

    /*
    倾倒液体的情形，判断规则类似破坏方块的情形
     */
    @EventHandler
    public void pourLiquid(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission(CommandPermission.ADMIN)) {
            return;
        }
        checkBehaviourWithPlayer(player, event.getBlockClicked().getLocation(), event.getBlockClicked().getType(), event, Msg.ARENA_PROTECTION_POUR_LIQUID, ArenaType.PresetType.PROTECTION_POUR_LIQUID);
    }

    /*
    捞取液体的情形，判断规则类似破坏方块的情形
     */
    @EventHandler
    public void fillLiquid(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission(CommandPermission.ADMIN)) {
            return;
        }
        checkBehaviourWithPlayer(player, event.getBlockClicked().getLocation(), event.getBlockClicked().getType(), event, Msg.ARENA_PROTECTION_GET_LIQUID, ArenaType.PresetType.PROTECTION_GET_LIQUID);
    }

    /*
    阻止实体破坏空闲场地的门
    根据相关预设存在与否决定是否阻止实体在比赛过程中破坏门
     */
    @EventHandler
    public void entityBreakDoor(EntityBreakDoorEvent event) {
        checkBehaviourWithoutPlayer(event.getEntity().getLocation(), null, event, ArenaType.PresetType.PROTECTION_ENTITY_BREAK_DOOR);
    }

    /*
    阻止实体在空闲场地生成（包括人为的和自然的）
    根据预设白名单决定是否阻止实体在比赛过程中生成
     */
    @EventHandler
    public void entitySpawned(EntitySpawnEvent event) {
        checkBehaviourWithoutPlayer(event.getEntity().getLocation(), event.getEntityType(), event, ArenaType.PresetType.PROTECTION_ENTITY_SPAWN);
    }

    /*
    方块被点燃的情形，判断规则类似实体生成的情形，但考虑场地边界情况时，着火方块的位置和火焰的位置实际上不一致，所以要考虑以着火点为中心对邻域进行考虑
     */
    @EventHandler
    public void blockIgnited(BlockIgniteEvent event) {
        Block ignitedBlock = event.getBlock();
        Block[] blocks = {ignitedBlock, ignitedBlock.getRelative(BlockFace.DOWN), ignitedBlock.getRelative(BlockFace.UP),
                ignitedBlock.getRelative(BlockFace.EAST), ignitedBlock.getRelative(BlockFace.SOUTH),
                ignitedBlock.getRelative(BlockFace.WEST), ignitedBlock.getRelative(BlockFace.NORTH)};
        for (Block block : blocks) {
            checkBehaviourWithoutPlayer(block.getLocation(), block.getType(), event, ArenaType.PresetType.PROTECTION_BLOCK_IGNITED);
        }
    }

    /*
    方块持续燃烧的情形，判断规则类似实体生成和方块被点燃的情形
     */
    @EventHandler
    public void blockBurning(BlockBurnEvent event) {
        Block burningBlock = event.getBlock();
        Block[] blocks = {burningBlock, burningBlock.getRelative(BlockFace.DOWN), burningBlock.getRelative(BlockFace.UP),
                burningBlock.getRelative(BlockFace.EAST), burningBlock.getRelative(BlockFace.SOUTH),
                burningBlock.getRelative(BlockFace.WEST), burningBlock.getRelative(BlockFace.NORTH)};
        for (Block block : blocks) {
            checkBehaviourWithoutPlayer(block.getLocation(), block.getType(), event, ArenaType.PresetType.PROTECTION_BLOCK_BURNING);
        }
    }

    /*
    阻止任何情形下通过活塞推入/推出方块
     */
    @EventHandler
    public void blockMovedIntoOrOutByPiston(BlockPistonExtendEvent event) {
        List<Block> blockPushedList = event.getBlocks();
        Block pistonBlock = event.getBlock();
        BaseArena pistonArena = UtilGeometry.getArena(pistonBlock.getLocation());
        if (pistonArena == null) {
            //活塞在场地外
            for (Block blockPushed : blockPushedList) {
                //注意这里一定要根据活塞的前进方向，确认被推方块要到达的位置再进行判断，而不是直接利用被推方块的位置判断，下同
                BaseArena arena = UtilGeometry.getArena(blockPushed.getRelative(event.getDirection()).getLocation());
                if (arena != null) {
                    //目标位置为某个场地，阻止
                    event.setCancelled(true);
                }
            }
        } else {
            //活塞在场地内
            for (Block blockPushed : blockPushedList) {
                BaseArena targetLocArena = UtilGeometry.getArena(blockPushed.getRelative(event.getDirection()).getLocation());
                if (targetLocArena == null || !targetLocArena.getId().equals(pistonArena.getId())) {
                    //目标位置非原场地，阻止
                    event.setCancelled(true);
                }
            }
        }
    }

    /*
    阻止任何情形下液体流入场地
     */
    @EventHandler
    public void liquidFlowInto(BlockFromToEvent event) {
        BaseArena arena = UtilGeometry.getArena(event.getToBlock().getLocation());
        if (arena != null) {
            event.setCancelled(true);
        }
    }

    //用于减少重复代码，本类中，有几个明确有玩家参与且涉及单个方块的事件具有相似的判定规则
    private void checkBehaviourWithPlayer(Player player, Location blockLocation, Material blockType, Cancellable event, Msg informMsg, ArenaType.PresetType presetType) {
        //分别获取玩家当前所属的竞技场和破坏方块所属的竞技场
        BaseArena playerArena = DuelTimePlugin.getInstance().getArenaManager().getOf(player);
        BaseArena blockArena = UtilGeometry.getArena(blockLocation);
        if (playerArena == null) {
            //当前玩家不在比赛中，那么判断如果当前的方块处于某个竞技场内，则直接阻止
            if (blockArena == null) {
                return;
            }
            event.setCancelled(true);
            MsgBuilder.sendActionBar(MsgBuilder.get(informMsg, player, blockArena.getName()), player, true);
        } else {
            //当前玩家在比赛中，则判断当前方块在不在自己所属的竞技场中，如果不在则一律阻止，反之则根据白名单选择性阻止
            if (blockArena == null || !blockArena.getId().equals(playerArena.getId())) {
                event.setCancelled(true);
            } else {
                if (!playerArena.getArenaType().getPresets().containsKey(presetType)) {
                    return;
                }
                Object data = playerArena.getArenaType().getPresets().get(presetType);
                if (data != null && ((List<Material>) data).contains(blockType)) {
                    return;
                }
                event.setCancelled(true);
            }
        }
    }

    //用于减少重复代码，本类中，有几个不明确有玩家参与且涉及单个方块/实体的事件具有相似的判定规则
    private void checkBehaviourWithoutPlayer(Location blockLocation, Object involvedObj, Cancellable event, ArenaType.PresetType presetType) {
        BaseArena arena = UtilGeometry.getArena(blockLocation);
        if (arena == null) {
            return;
        }
        if (arena.getState() == BaseArena.State.WAITING || arena.getState() == BaseArena.State.DISABLED) {
            event.setCancelled(true);
        } else {
            if (!arena.getArenaType().getPresets().containsKey(presetType)) {
                return;
            }
            Object data = arena.getArenaType().getPresets().get(presetType);
            if (presetType.getDataType() != null && data != null && ((List<?>) data).contains(involvedObj)) {
                //如果预设规定要提供白名单，且该类型的竞技场定义了非空白名单，且当前方块/实体在白名单中，则不阻止
                return;
            }
            event.setCancelled(true);
        }
    }

    /*
    正常结束比赛时累加提交给bstats的数据
     */
    @EventHandler
    public void onArenaEnd(ArenaEndEvent event) {
        Metrics metrics = DuelTimePlugin.getInstance().getMetrics();
        metrics.accumulateGameNumber();
    }
}
