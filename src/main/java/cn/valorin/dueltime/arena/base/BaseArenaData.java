package cn.valorin.dueltime.arena.base;

import cn.valorin.dueltime.arena.type.ArenaType;
import org.bukkit.Location;

import java.util.HashMap;

/**
 * 竞技场场地的基础数据
 */
public class BaseArenaData {
    //ID，竞技场的唯一标识
    private final String id;
    //竞技场名称
    private String name;
    //类型id
    private final String typeId;
    //对角点位置1，用于确立三维空间
    private final Location diagonalPointLocation1;
    //对角点位置2，用于确立三维空间
    private final Location diagonalPointLocation2;
    //最小开始人数
    private final int minPlayerNumber;
    //最大人数，为非正数代表无限制
    private final int maxPlayerNumber;
    //拓展功能Map，功能名和相关数据一一对应
    private HashMap<String, Object[]> functions;

    public BaseArenaData(String id, String name, String typeId, Location diagonalPointLocation1, Location diagonalPointLocation2, int minPlayerNumber, int maxPlayerNumber, HashMap<String, Object[]> functions) {
        this.id = id;
        this.name = name;
        this.typeId = typeId;
        this.diagonalPointLocation1 = diagonalPointLocation1;
        this.diagonalPointLocation2 = diagonalPointLocation2;
        this.minPlayerNumber = minPlayerNumber;
        this.maxPlayerNumber = maxPlayerNumber;
        this.functions = functions;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTypeId() {
        return typeId;
    }

    public Location getDiagonalPointLocation1() {
        return diagonalPointLocation1;
    }

    public Location getDiagonalPointLocation2() {
        return diagonalPointLocation2;
    }

    public int getMinPlayerNumber() {
        return minPlayerNumber;
    }

    public int getMaxPlayerNumber() {
        return maxPlayerNumber;
    }

    public HashMap<String, Object[]> getFunctions() {
        return functions;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addFunction(String functionId, Object[] data) {
        if (functions == null) functions = new HashMap<>();
        functions.put(functionId, data);
    }

    public void removeFunction(String functionId) {
        if (this.functions != null) {
            functions.remove(functionId);
            if (functions.isEmpty()) {
                //如果没有元素了，设为null，避免在数据库中以空列表形式占用内存
                functions = null;
            }
        }
    }

    public void setFunctions(HashMap<String, Object[]> functions) {
        this.functions = functions;
    }

    public boolean hasFunction(String functionId) {
        if (this.functions == null) {
            return false;
        }
        return this.functions.containsKey(functionId);
    }

    public boolean hasFunction(ArenaType.FunctionInternalType function) {
        return hasFunction(function.getId());
    }

    public Object[] getFunctionData(ArenaType.FunctionInternalType function) {
        return getFunctionData(function.getId());
    }

    public Object[] getFunctionData(String functionId) {
        return functions.get(functionId);
    }
}
