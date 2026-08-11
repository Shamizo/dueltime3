package cn.valorin.dueltime.request;

import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestReceiver {
    private final String receiverName;
    private final Map<String, RequestData> requestDataMap;


    public RequestReceiver(String receiverName) {
        this.receiverName = receiverName;
        this.requestDataMap = new HashMap<>();
    }

    /**
     * @return 未超时请求的玩家名列表
     */
    public List<String> getValidSenderNames() {
        List<String> validRequests = new ArrayList<>();
        for (Map.Entry<String, RequestData> kv : requestDataMap.entrySet()) {
            RequestData requestData = kv.getValue();
            if (System.currentTimeMillis() > requestData.getEndTime()) {
                //超时请求则跳过
                continue;
            }
            String senderName = kv.getKey();
            if (Bukkit.getPlayerExact(senderName) == null ||
                    !Bukkit.getPlayerExact(senderName).isOnline()) {
                //请求方下线了则跳过
                continue;
            }
            validRequests.add(kv.getKey());
        }
        return validRequests;
    }

    /**
     * @return 返回无法接受某玩家请求的原因（枚举）
     */
    public InvalidReason getInvalidReason(String senderNameEntered) {
        if (requestDataMap.containsKey(senderNameEntered)) {
            if (System.currentTimeMillis() > requestDataMap.get(senderNameEntered).getEndTime()) {
                return InvalidReason.TIME_OUT;
            } else {
                return InvalidReason.OFFLINE;
            }
        } else {
            return InvalidReason.HAS_NOT_SENT;
        }
    }

    public enum InvalidReason {
        TIME_OUT,//请求超时
        OFFLINE,//请求方下线了
        HAS_NOT_SENT//对方没给自己发送过请求
    }


    /**
     * 在请求列表中添加请求方的玩家名
     *
     * @param senderName 请求方玩家名
     */
    public void add(String senderName, String arenaEditName) {
        long startTime = System.currentTimeMillis();
        requestDataMap.put(senderName, new RequestData(startTime, startTime + 120 * 1000, arenaEditName));
    }

    /**
     * @return 某个请求方的请求数据
     */
    public RequestData get(String senderName) {
        return requestDataMap.get(senderName);
    }

    /**
     * 移除某个玩家的请求，一般应用于接收方拒绝了该玩家的请求
     */
    public void remove(String senderName) {
        requestDataMap.remove(senderName);
    }

    /**
     * 清空请求列表。一般应用于接受方开始了比赛时
     */
    public void clear() {
        requestDataMap.clear();
    }
}
