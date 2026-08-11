package cn.valorin.dueltime.util;

import cn.valorin.dueltime.DuelTimePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 调度器工具类
 * Folia兼容版本 - 基于Dominion插件的Folia兼容实现，使用反射避免编译时依赖
 */
public class SchedulerUtil {

    private static final boolean IS_PAPER;
    private static Method getGlobalRegionSchedulerMethod;
    private static Method getAsyncSchedulerMethod;
    private static Method getPlayerSchedulerMethod;
    private static boolean initialized = false;

    static {
        boolean paper = false;
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask");
            paper = true;
        } catch (ClassNotFoundException e) {
            paper = false;
        }
        IS_PAPER = paper;

        if (IS_PAPER) {
            initializeReflectionMethods();
        }
    }

    private static void initializeReflectionMethods() {
        if (initialized) return;
        try {
            Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
            getGlobalRegionSchedulerMethod = bukkitClass.getMethod("getGlobalRegionScheduler");
            getAsyncSchedulerMethod = bukkitClass.getMethod("getAsyncScheduler");

            Class<?> playerClass = Class.forName("org.bukkit.entity.Player");
            getPlayerSchedulerMethod = playerClass.getMethod("getScheduler");

            initialized = true;
        } catch (Exception e) {
            // 如果反射初始化失败，则禁用Folia功能
            initialized = false;
        }
    }

    public static boolean isFolia() {
        return IS_PAPER && initialized;
    }

    /**
     * 创建一个实现 Consumer 接口的代理，用于包装要执行的任务。
     * Paper/Folia 的调度器接收的是 Consumer<ScheduledTask>，而非 BukkitTask。
     */
    private static Object createConsumerProxy(Runnable task) {
        return java.lang.reflect.Proxy.newProxyInstance(
                SchedulerUtil.class.getClassLoader(),
                new Class[]{Consumer.class},
                (proxy, method, args) -> {
                    if ("accept".equals(method.getName())) {
                        task.run();
                    }
                    return null;
                }
        );
    }

    /**
     * 在主线程同步执行任务
     */
    public static void runTask(Runnable task) {
        if (IS_PAPER && initialized) {
            try {
                Object globalRegionScheduler = getGlobalRegionSchedulerMethod.invoke(null);
                Method runMethod = globalRegionScheduler.getClass().getMethod("run", Plugin.class, Consumer.class);
                runMethod.invoke(globalRegionScheduler, DuelTimePlugin.getInstance(), createConsumerProxy(task));
            } catch (Exception e) {
                Bukkit.getScheduler().runTask(DuelTimePlugin.getInstance(), task);
            }
        } else {
            Bukkit.getScheduler().runTask(DuelTimePlugin.getInstance(), task);
        }
    }

    /**
     * 在主线程同步执行任务（带延迟）
     */
    public static void runTaskLater(Runnable task, long delayTicks) {
        if (IS_PAPER && initialized) {
            try {
                Object globalRegionScheduler = getGlobalRegionSchedulerMethod.invoke(null);
                Method runDelayedMethod = globalRegionScheduler.getClass().getMethod("runDelayed", Plugin.class, Consumer.class, long.class);
                runDelayedMethod.invoke(globalRegionScheduler, DuelTimePlugin.getInstance(), createConsumerProxy(task), delayTicks);
            } catch (Exception e) {
                Bukkit.getScheduler().runTaskLater(DuelTimePlugin.getInstance(), task, delayTicks);
            }
        } else {
            Bukkit.getScheduler().runTaskLater(DuelTimePlugin.getInstance(), task, delayTicks);
        }
    }

    /**
     * 在主线程同步执行定时任务
     */
    public static BukkitTask runTaskTimer(Runnable task, long delayTicks, long periodTicks) {
        if (IS_PAPER && initialized) {
            try {
                Object globalRegionScheduler = getGlobalRegionSchedulerMethod.invoke(null);
                Method runAtFixedRateMethod = globalRegionScheduler.getClass().getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class);
                Object foliaTask = runAtFixedRateMethod.invoke(globalRegionScheduler, DuelTimePlugin.getInstance(), createConsumerProxy(task), delayTicks, periodTicks);

                return new PaperBukkitTask(foliaTask);
            } catch (Exception e) {
                return Bukkit.getScheduler().runTaskTimer(DuelTimePlugin.getInstance(), task, delayTicks, periodTicks);
            }
        } else {
            return Bukkit.getScheduler().runTaskTimer(DuelTimePlugin.getInstance(), task, delayTicks, periodTicks);
        }
    }

    /**
     * 在异步线程执行任务
     */
    public static void runTaskAsync(Runnable task) {
        if (IS_PAPER && initialized) {
            try {
                Object asyncScheduler = getAsyncSchedulerMethod.invoke(null);
                Method runNowMethod = asyncScheduler.getClass().getMethod("runNow", Plugin.class, Consumer.class);
                runNowMethod.invoke(asyncScheduler, DuelTimePlugin.getInstance(), createConsumerProxy(task));
            } catch (Exception e) {
                Bukkit.getScheduler().runTaskAsynchronously(DuelTimePlugin.getInstance(), task);
            }
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(DuelTimePlugin.getInstance(), task);
        }
    }

    /**
     * 在异步线程执行任务（带延迟）
     */
    public static void runTaskLaterAsync(Runnable task, long delayTicks) {
        if (IS_PAPER && initialized) {
            try {
                Object asyncScheduler = getAsyncSchedulerMethod.invoke(null);
                Method runDelayedMethod = asyncScheduler.getClass().getMethod("runDelayed", Plugin.class, Consumer.class, long.class, TimeUnit.class);
                runDelayedMethod.invoke(asyncScheduler, DuelTimePlugin.getInstance(), createConsumerProxy(task), delayTicks * 50L, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                Bukkit.getScheduler().runTaskLaterAsynchronously(DuelTimePlugin.getInstance(), task, delayTicks);
            }
        } else {
            Bukkit.getScheduler().runTaskLaterAsynchronously(DuelTimePlugin.getInstance(), task, delayTicks);
        }
    }

    /**
     * 在异步线程执行定时任务
     * AsyncScheduler.runAtFixedRate 的签名在不同Paper/Folia版本中有所差异，这里尝试两种候选签名：
     * 1. runAtFixedRate(Plugin, Consumer, long, long, TimeUnit) —— Paper/Folia 1.21.x
     * 2. runAtFixedRate(Plugin, Consumer, long, TimeUnit, long, TimeUnit) —— 其它版本
     */
    public static BukkitTask runTaskTimerAsync(Runnable task, long delayTicks, long periodTicks) {
        if (IS_PAPER && initialized) {
            try {
                Object asyncScheduler = getAsyncSchedulerMethod.invoke(null);
                long delayMs = delayTicks * 50L;
                long periodMs = periodTicks * 50L;
                Object consumer = createConsumerProxy(task);
                Method runAtFixedRateMethod;
                try {
                    runAtFixedRateMethod = asyncScheduler.getClass().getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class, TimeUnit.class);
                    Object foliaTask = runAtFixedRateMethod.invoke(asyncScheduler, DuelTimePlugin.getInstance(), consumer, delayMs, periodMs, TimeUnit.MILLISECONDS);
                    return new PaperBukkitTask(foliaTask);
                } catch (NoSuchMethodException e1) {
                    runAtFixedRateMethod = asyncScheduler.getClass().getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, TimeUnit.class, long.class, TimeUnit.class);
                    Object foliaTask = runAtFixedRateMethod.invoke(asyncScheduler, DuelTimePlugin.getInstance(), consumer, delayMs, TimeUnit.MILLISECONDS, periodMs, TimeUnit.MILLISECONDS);
                    return new PaperBukkitTask(foliaTask);
                }
            } catch (Exception e) {
                return Bukkit.getScheduler().runTaskTimerAsynchronously(DuelTimePlugin.getInstance(), task, delayTicks, periodTicks);
            }
        } else {
            return Bukkit.getScheduler().runTaskTimerAsynchronously(DuelTimePlugin.getInstance(), task, delayTicks, periodTicks);
        }
    }

    /**
     * 在玩家所在区域执行任务
     * Player.getScheduler() 返回的是 EntityScheduler，其 run 签名为 (Plugin, Consumer, Runnable)；
     * 旧版本可能只有 (Plugin, Consumer)，这里两种签名都尝试。
     */
    public static void runTaskForPlayer(Player player, Runnable task) {
        if (IS_PAPER && initialized) {
            try {
                Object playerScheduler = getPlayerSchedulerMethod.invoke(player);
                Object consumer = createConsumerProxy(task);
                try {
                    Method runMethod = playerScheduler.getClass().getMethod("run", Plugin.class, Consumer.class, Runnable.class);
                    runMethod.invoke(playerScheduler, DuelTimePlugin.getInstance(), consumer, (Runnable) null);
                } catch (NoSuchMethodException e1) {
                    Method runMethod = playerScheduler.getClass().getMethod("run", Plugin.class, Consumer.class);
                    runMethod.invoke(playerScheduler, DuelTimePlugin.getInstance(), consumer);
                }
            } catch (Exception e) {
                Bukkit.getScheduler().runTask(DuelTimePlugin.getInstance(), task);
            }
        } else {
            Bukkit.getScheduler().runTask(DuelTimePlugin.getInstance(), task);
        }
    }

    /**
     * 延迟执行重复任务（Folia兼容）
     * 用于替代定时任务的循环逻辑
     */
    public static void runDelayedRepeating(Runnable task, long delayTicks, long periodTicks, java.util.function.Consumer<BukkitTask> cancelCallback) {
        if (IS_PAPER && initialized) {
            try {
                Object globalRegionScheduler = getGlobalRegionSchedulerMethod.invoke(null);
                Method runAtFixedRateMethod = globalRegionScheduler.getClass().getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class);
                Object foliaTask = runAtFixedRateMethod.invoke(globalRegionScheduler, DuelTimePlugin.getInstance(), createConsumerProxy(task), delayTicks, periodTicks);

                if (cancelCallback != null) {
                    cancelCallback.accept(new PaperBukkitTask(foliaTask));
                }
            } catch (Exception e) {
                // 如果反射失败，使用普通调度器
                BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimer(DuelTimePlugin.getInstance(), task, delayTicks, periodTicks);
                if (cancelCallback != null) {
                    cancelCallback.accept(bukkitTask);
                }
            }
        } else {
            BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimer(DuelTimePlugin.getInstance(), task, delayTicks, periodTicks);
            if (cancelCallback != null) {
                cancelCallback.accept(bukkitTask);
            }
        }
    }

    /**
     * 安全的传送方法
     */
    public static CompletableFuture<Boolean> safeTeleport(Player player, Location location) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        if (player == null || !player.isOnline()) {
            future.complete(false);
            return future;
        }

        if (location == null || location.getWorld() == null) {
            future.complete(false);
            return future;
        }

        // 检查玩家是否在传送冷却中
        if (player.getNoDamageTicks() > 0) {
            runTaskLater(() -> safeTeleport(player, location).thenAccept(future::complete), 5);
            return future;
        }

        // 在Folia环境中，跨世界传送应使用全局调度器
        if (IS_PAPER && initialized && player.getWorld() != location.getWorld()) {
            // 使用反射调用全局调度器处理跨世界传送，避免region冲突
            try {
                Object globalRegionScheduler = getGlobalRegionSchedulerMethod.invoke(null);
                Method runMethod = globalRegionScheduler.getClass().getMethod("run", Plugin.class, Consumer.class);
                Runnable teleportTask = () -> {
                    try {
                        if (player.isOnline() && location.getWorld() != null) {
                            Location safeLocation = SafeLocationFinder.findNearestSafeLocation(location);
                            if (safeLocation != null) {
                                // 只有当目标位置没有预设朝向时才保留玩家原有朝向
                                // 如果SafeLocationFinder返回的位置保持了原始的朝向值，说明我们预设了朝向，不应覆盖
                                Location originalLocation = location; // 传入的原始位置
                                // 检查safeLocation是否保留了原始位置的朝向（说明我们预设了特定朝向）
                                if (Math.abs(safeLocation.getYaw() - originalLocation.getYaw()) < 0.1f &&
                                        Math.abs(safeLocation.getPitch() - originalLocation.getPitch()) < 0.1f) {
                                    // safeLocation保持了原始位置的朝向，说明我们有意设置了特定朝向，不替换
                                } else {
                                    // safeLocation的朝向被SafeLocationFinder改变了（可能是默认值），可以用玩家朝向覆盖
                                    Location playerLocation = player.getLocation();
                                    if (playerLocation != null) {
                                        safeLocation.setYaw(playerLocation.getYaw());
                                        safeLocation.setPitch(playerLocation.getPitch());
                                    }
                                }
                                boolean success = player.teleport(safeLocation);
                                future.complete(success);
                            } else {
                                future.complete(false);
                            }
                        } else {
                            future.complete(false);
                        }
                    } catch (Exception e) {
                        future.complete(false);
                    }
                };
                runMethod.invoke(globalRegionScheduler, DuelTimePlugin.getInstance(), createConsumerProxy(teleportTask));
            } catch (Exception e) {
                // 如果反射失败，使用普通调度器
                runTaskForPlayer(player, () -> {
                    try {
                        if (player.isOnline() && location.getWorld() != null) {
                            Location safeLocation = SafeLocationFinder.findNearestSafeLocation(location);
                            if (safeLocation != null) {
                                // 保留玩家的朝向
                                Location playerLocation = player.getLocation();
                                // 检查目标位置是否已经有明确的朝向设定（不是默认的0,0朝向）
                                // 如果是0,0朝向，说明没有预设朝向，可以保留玩家原有朝向
                                // 如果不是0,0朝向，说明有预设朝向，应该使用预设的朝向
                                if (Math.abs(safeLocation.getYaw()) < 0.1f && Math.abs(safeLocation.getPitch()) < 0.1f) {
                                    if (playerLocation != null) {
                                        safeLocation.setYaw(playerLocation.getYaw());
                                        safeLocation.setPitch(playerLocation.getPitch());
                                    }
                                }
                                // 否则保持safeLocation中已设置的预设朝向
                                boolean success = player.teleport(safeLocation);
                                future.complete(success);
                            } else {
                                future.complete(false);
                            }
                        } else {
                            future.complete(false);
                        }
                    } catch (Exception ex) {
                        future.complete(false);
                    }
                });
            }
        } else {
            // 非Folia环境下的传统处理方式
            runTaskForPlayer(player, () -> {
                try {
                    if (player.isOnline() && location.getWorld() != null) {
                        Location safeLocation = SafeLocationFinder.findNearestSafeLocation(location);
                        if (safeLocation != null) {
                            // 只有当目标位置没有预设朝向时才保留玩家原有朝向
                            // 如果SafeLocationFinder返回的位置保持了原始的朝向值，说明我们预设了朝向，不应覆盖
                            Location originalLocation = location; // 传入的原始位置
                            // 检查safeLocation是否保留了原始位置的朝向（说明我们预设了特定朝向）
                            if (Math.abs(safeLocation.getYaw() - originalLocation.getYaw()) < 0.1f &&
                                    Math.abs(safeLocation.getPitch() - originalLocation.getPitch()) < 0.1f) {
                                // safeLocation保持了原始位置的朝向，说明我们有意设置了特定朝向，不替换
                            } else {
                                // safeLocation的朝向被SafeLocationFinder改变了（可能是默认值），可以用玩家朝向覆盖
                                Location playerLocation = player.getLocation();
                                if (playerLocation != null) {
                                    safeLocation.setYaw(playerLocation.getYaw());
                                    safeLocation.setPitch(playerLocation.getPitch());
                                }
                            }
                            boolean success = player.teleport(safeLocation);
                            future.complete(success);
                        } else {
                            future.complete(false);
                        }
                    } else {
                        future.complete(false);
                    }
                } catch (Exception e) {
                    future.complete(false);
                }
            });
        }

        return future;
    }

    /**
     * Paper/Folia 环境下 BukkitTask 的包装类
     * 用于兼容原有的 BukkitTask 接口
     */
    private static class PaperBukkitTask implements BukkitTask {
        private final Object scheduledTask; // 使用Object类型避免编译时依赖

        public PaperBukkitTask(Object scheduledTask) {
            this.scheduledTask = scheduledTask;
        }

        @Override
        public int getTaskId() {
            return -1; // 在Paper/Folia中不适用
        }

        @Override
        public JavaPlugin getOwner() {
            return DuelTimePlugin.getInstance();
        }

        @Override
        public boolean isSync() {
            return false; // Paper/Folia中的全局调度器是异步的
        }

        @Override
        public boolean isCancelled() {
            if (scheduledTask == null) return true;
            try {
                return (Boolean) scheduledTask.getClass().getMethod("isCancelled").invoke(scheduledTask);
            } catch (Exception e) {
                return true; // 如果无法检查，则假定已取消
            }
        }

        @Override
        public void cancel() {
            if (scheduledTask != null) {
                try {
                    scheduledTask.getClass().getMethod("cancel").invoke(scheduledTask);
                } catch (Exception e) {
                    // 取消失败，忽略
                }
            }
        }
    }

}
