package cn.valorin.dueltime.progress;

import cn.valorin.dueltime.yaml.message.Msg;
import cn.valorin.dueltime.yaml.message.MsgBuilder;
import org.bukkit.entity.Player;

public class Step {
    private final Player player;
    private final Object tip;
    private final Object finishTitle;
    private final Object finishSubTitle;
    private final Class<?> dataType;
    /**
     * 关于自动上传
     * 在步骤中，对于部分类型的数据，可能不需要额外编写逻辑，因为本插件已预先写好了一些常用的上传逻辑
     * 当前已有的逻辑有：
     * String类型：通过聊天框输入上传，可自动替换颜色符号
     * Integer类型：通过聊天框输入上传，自动识别格式，可自动筛选出正值
     * Double/Float类型：通过聊天框输入上传，自动识别格式，可自动筛选出正值
     * Location类型：通过点击方块/直接点击屏幕上传
     * ItemStack类型：手持物品点击屏幕上传
     * 如果上述逻辑符合你的需求，可以直接将autoUpload设置为true
     * 如果上述逻辑不符合你的需求，或不包含你需求的类型，请将autoUpdate设置为false，并自行额外编写逻辑
     */
    private final boolean autoUpload;
    private final AutoUploadTag[] autoUploadTags;
    private Object data;

    public Step(Object tip, Object finishTitle, Object finishSubTitle, Player player, Class<?> dataType, boolean autoUpload, AutoUploadTag... autoUploadTags) {
        if (!(tip instanceof String) && !(tip instanceof Msg)) {
            throw new IllegalArgumentException("The 1st argument must be String or Msg");
        }
        if (!(finishTitle instanceof String) && !(finishTitle instanceof Msg)) {
            throw new IllegalArgumentException("The 2nd argument must be String or Msg");
        }
        if (!(finishSubTitle instanceof String) && !(finishSubTitle instanceof Msg)) {
            throw new IllegalArgumentException("The 3rd argument must be String or Msg");
        }
        this.tip = tip;
        this.finishTitle = finishTitle;
        this.finishSubTitle = finishSubTitle;
        this.player = player;
        this.autoUpload = autoUpload;
        this.autoUploadTags = autoUploadTags;
        this.dataType = dataType;
    }

    public String getTip() {
        return (tip instanceof String) ?
                (String) tip :
                MsgBuilder.get((Msg) tip, player);
    }

    public String getFinishTitle() {
        return (finishTitle instanceof String) ?
                (String) finishTitle :
                MsgBuilder.get((Msg) finishTitle, player);
    }

    public String getFinishSubTitle() {
        return (finishSubTitle instanceof String) ?
                (String) finishSubTitle :
                MsgBuilder.get((Msg) finishSubTitle, player);
    }

    public Player getPlayer() {
        return player;
    }

    public Class<?> getDataType() {
        return dataType;
    }

    public boolean isAutoUpload() {
        return autoUpload;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public AutoUploadTag[] getAutoUploadTags() {
        return autoUploadTags == null ? new AutoUploadTag[0] : autoUploadTags;
    }

    public boolean hasAutoUploadTags(AutoUploadTag autoUploadTag) {
        if (autoUploadTags == null) {
            return false;
        }
        for (AutoUploadTag autoUploadTagDefined : autoUploadTags) {
            if (autoUploadTagDefined.equals(autoUploadTag)) {
                return true;
            }
        }
        return false;
    }

    public enum AutoUploadTag {
        STRING_CONDITION_ID_STYLE, //只容许英文字母的字符串
        STRING_FUNCTION_REPLACE_BLANK, //自动替换字符串中的空格
        STRING_FUNCTION_REPLACE_COLOR_SYMBOL, //自动替换颜色符号
        STRING_FUNCTION_TO_UPPERCASE, //自动转为大写
        STRING_FUNCTION_TO_LOWERCASE, //自动转为小写
        INTEGER_CONDITION_POSITIVE_VALUE, //只容许正值的整数
        DOUBLE_CONDITION_POSITIVE_VALUE, //只容许正值的小数
        LOCATION_CONDITION_CLICK_AIR, //点击空气记录当前站立点
        LOCATION_CONDITION_CLICK_BLOCK, //点击方块记录方块点
        LOCATION_CONDITION_THE_SAME_WORLD, //位置必须和上一个步骤的位置位于同一个世界
        LOCATION_CONDITION_DIFFERENT_BLOCK, //位置必须和上一个步骤的位置不在同一个方块
        LOCATION_CONDITION_CANNOT_OVERLAP_WITH_OTHER_ARENA, //位置必须和上一个步骤的位置所构成的三维区域不能与其他竞技场的三维区域有交叠
        LIST_CONDITION_STRING_INTEGER_PAIR, //列表内容必须为字符串-整数对，用英文冒号来分割，例如物品种类检测情境下，"wool:3"表示子id为3的染色羊毛（淡蓝色）
        LIST_CONDITION_STRING_INTEGER_PAIR_LOOSE, //意义和LIST_CONDITION_STRING_INTEGER_PAIR类似，但容许只提供字符串，如"wool"表示任何一种羊毛
        LIST_CONDITION_IDENTITY_COMMAND_PAIR, //列表内容为字符串对，前者为身份(player,op,console)，后者为指令内容，例如"console:fly {player}"表示通过后台执行让玩家飞行的指令（注意要自行将{player}等占位符在功能逻辑中替换）
        LIST_CONDITION_NULLABLE, //列表内容可以为空
    }
}
