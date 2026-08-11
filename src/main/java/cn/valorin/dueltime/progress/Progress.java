package cn.valorin.dueltime.progress;

import cn.valorin.dueltime.DuelTimePlugin;
import cn.valorin.dueltime.event.progress.ProgressFinishedEvent;
import cn.valorin.dueltime.util.UtilFormat;
import cn.valorin.dueltime.util.SchedulerUtil;
import cn.valorin.dueltime.viaversion.ViaVersion;
import cn.valorin.dueltime.yaml.message.Msg;
import cn.valorin.dueltime.yaml.message.MsgBuilder;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class Progress {
    //过程的唯一标识
    private final String id;
    //过程的名称（可为String也可为Msg）
    private final Object name;
    //执行该过程的玩家
    private final Player player;
    //开始过程时携带的一些数据
    private final Object data;
    //步骤集合
    private final Step[] steps;
    //步骤总数
    private final int totalStep;
    //当前完成步骤的总数，也可以善用巧合，把这理解为当前所操作的步骤的序号（从0开始）
    private int finishedStep;
    //是否使用BossBar进度条，若否，则使用纯文字提示（1.8及以下没有直接创建进度条的API）
    private final boolean isBossBarUsed;
    //用于显示进度的BossBar（Boss血量条）
    private BossBar progressBar;
    //BossBar原本的颜色，用于恢复暂停时重新恢复颜色（暂定时进度条会变成黄色）
    private BarColor barColor;
    //是否处于暂停状态，处于该状态时，无论做什么动作都不会触发上传数据的事件
    private boolean paused;
    //定时器，用于实现一些拓展功能
    private BukkitTask timer;

    public Progress(Plugin plugin, String id, Object name, Player player, Object data, boolean isBossBarUsed, Step... steps) {
        //确定过程的唯一标识，最终ID为“插件名+冒号+填写的ID”
        if (plugin == null) {
            throw new NullPointerException("The plugin cannot be null");
        }
        if (!id.contains(":") || !id.split(":")[0].equals(plugin.getDescription().getName().toLowerCase()) || !UtilFormat.isIDStyle(id.split(":")[1])) {
            throw new IllegalArgumentException("The format of the 2nd argument must be 'the lowercase of your plugin' + ':' + 'id',for example,'dueltime:test',and the id can only consist of English and numbers");
        }
        this.id = id;
        //确定过程的名称
        if (!(name instanceof String) && !(name instanceof Msg)) {
            throw new IllegalArgumentException("The type of 3rd argument must be String or Msg");
        }
        this.name = name;
        //确定执行该过程的玩家
        this.player = player;
        //传入开始过程时携带的一些数据
        this.data = data;
        //确定步骤信息（每个Step包括当前步骤的BossBar提示语、需要的数据类型、完成后的提示语等信息）
        this.steps = steps;
        this.totalStep = steps.length;
        this.finishedStep = 0;
        this.isBossBarUsed = isBossBarUsed;
        //设置现在不处于暂停状态
        this.paused = false;
    }

    public void initBossBar(BarColor barColor, BarStyle barStyle) {
        //显示首个步骤的BossBar标题
        String barTitle = MsgBuilder.get(Msg.PROGRESS_BOSSBAR_TIP, player,
                getName(), "0", "" + totalStep, steps[0].getTip());
        //传入BossBar原色，用于在恢复暂停时换回来
        this.barColor = barColor;
        //创建一个BossBar对象
        this.progressBar = Bukkit.createBossBar(barTitle, barColor, barStyle);
        //设置BossBar进度为0%
        this.progressBar.setProgress(0);
        //使BossBar向执行玩家展示
        this.progressBar.addPlayer(player);
    }

    public String getName() {
        return (name instanceof String) ?
                (String) name :
                MsgBuilder.get((Msg) name, player);
    }

    /**
     * 确认完成当前步骤并上传数据，同时进入下一步
     *
     * @param data 上传的数据
     */
    public void next(Object data) {
        if (finishedStep >= totalStep) {
            //如果已经是最后一步，则不处理
            return;
        }
        //获取当前所完成的步骤
        Step nowStep = steps[finishedStep];
        //上传当前步骤所需的数据
        nowStep.setData(data);
        //发送当前步骤完成后的Title提示语
        MsgBuilder.sendTitle(nowStep.getFinishTitle(),
                nowStep.getFinishSubTitle().replace("{}", UtilFormat.toString(nowStep.getData(),
                        UtilFormat.StringifyTag.LIST_LIMIT_LINE_LENGTH,
                        UtilFormat.StringifyTag.LIST_LIMIT_LIST_SIZE)),
                0, 30, 5, player, ViaVersion.TitleType.SUBTITLE);
        //已完成步骤数+1
        finishedStep++;
        //按照当前步骤更新提示内容
        updateProgressTip();
    }

    /**
     * 返回上一步，同时最近一次上传的数据会被删除
     */
    public void reverse() {
        if (finishedStep <= 0) {
            //如果当前没有完成任何步骤，则不处理
            return;
        }
        //删除数据
        steps[finishedStep].setData(null);
        //已完成步骤数-1
        finishedStep--;
        //按照当前步骤更新提示内容
        updateProgressTip();
    }

    private void updateProgressTip() {
        String tip = finishedStep == totalStep ?
                MsgBuilder.get(Msg.PROGRESS_BOSSBAR_MESSAGE_FINISH_STRING, player) : steps[finishedStep].getTip();
        if (isBossBarUsed) {
            progressBar.setTitle(
                    MsgBuilder.get(Msg.PROGRESS_BOSSBAR_TIP, player,
                            getName(), "" + finishedStep, "" + totalStep, tip));
            progressBar.setProgress((double) finishedStep / totalStep);
        } else {
            MsgBuilder.sends(Msg.PROGRESS_BOSSBAR_FREE_TIP, player, false,
                    getName(), "" + finishedStep, "" + totalStep,
                    finishedStep + " " + (totalStep - finishedStep),
                    tip);
        }
        //如果当前步为最后一步，则再额外执行finish方法
        if (finishedStep == totalStep) {
            finish(true);
        }
    }


    /**
     * 完成该过程
     * 如果所有步骤确实都已经完成，那么将使用当前完整的数据列表来创建对象
     *
     * @param delayed 是否开启视觉延迟（有利于让玩家看到完成提示语和满值的进度条，优化视觉体验）
     */
    public void finish(boolean delayed) {
        //若因为一些需求，初始化时Timer被创建，那么就关闭这个定时器
        if (timer != null) {
            timer.cancel();
        }
        //根据上传的数据创建对象。这里通过发布事件的方式，方便不同的插件实例监听
        if (finishedStep >= totalStep) {
            Bukkit.getServer().getPluginManager().callEvent(new ProgressFinishedEvent(player, this));
        }
        //根据需要实现视觉延时
        if (isBossBarUsed) {
            if (delayed) {
                SchedulerUtil.runTaskLaterAsync(progressBar::removeAll, 80);
                if (finishedStep >= totalStep) {
                    SchedulerUtil.runTaskLaterAsync(() -> {
                        MsgBuilder.send(Msg.PROGRESS_FINISH_MESSAGE, player, getName());
                        MsgBuilder.sendTitle(
                                MsgBuilder.get(Msg.PROGRESS_FINISH_TITLE, player),
                                MsgBuilder.get(Msg.PROGRESS_FINISH_SUBTITLE, player, getName()),
                                5, 80, 12, player);
                    }, 30);
                }
            } else {
                progressBar.removeAll();
            }
        } else {
            MsgBuilder.send(Msg.PROGRESS_FINISH_MESSAGE, player, getName());
        }
        //向管理器发送请求销毁自身
        DuelTimePlugin.getInstance().getProgressManager().cancel(player.getName());
    }

    public void exit() {
        if (isBossBarUsed) {
            progressBar.removeAll();
        }
        if (timer != null && !timer.isCancelled()) {
            timer.cancel();
        }
        //向管理器发送请求销毁自身
        DuelTimePlugin.getInstance().getProgressManager().cancel(player.getName());
    }

    public String getId() {
        return id;
    }

    public Player getPlayer() {
        return player;
    }

    public Object getData() {
        return data;
    }

    public int getFinishedStep() {
        return finishedStep;
    }

    public Step getNowStep() {
        return steps[finishedStep];
    }

    public Step[] getSteps() {
        return steps;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
        if (isBossBarUsed) {
            if (paused) {
                progressBar.setColor(BarColor.YELLOW);
            } else {
                progressBar.setColor(barColor);
            }
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isBossBarUsed() {
        return isBossBarUsed;
    }

    public BukkitTask getTimer() {
        return timer;
    }

    public void setTimer(BukkitTask timer) {
        this.timer = timer;
    }
}
