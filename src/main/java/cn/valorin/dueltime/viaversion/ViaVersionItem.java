package cn.valorin.dueltime.viaversion;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.projectiles.ProjectileSource;

import java.lang.reflect.Method;

public class ViaVersionItem {
    /**
     * 用于GUI装饰
     * 根据种类id获取染色玻璃板物品
     * 适用于没有染色玻璃物品的版本（1.7以下）与通过枚举名对玻璃板Material进行严格区分的版本（1.13以上）
     *
     * @param type 颜色种类id
     * @return 用于GUI装饰的玻璃板ItemStack
     */
    public static ItemStack getGlassPaneType(int type) {
        ItemStack itemStack;
        if (Material.getMaterial("STAINED_GLASS_PANE") != null) {
            itemStack = new ItemStack(Material.getMaterial("STAINED_GLASS_PANE"), 1, (short) type);
        } else {
            String materialName = "WHITE_STAINED_GLASS_PANE";
            switch (type) {
                case 1:
                    materialName = "ORANGE_STAINED_GLASS_PANE";
                    break;
                case 2:
                    materialName = "MAGENTA_STAINED_GLASS_PANE";
                    break;
                case 3:
                    materialName = "LIGHT_BLUE_STAINED_GLASS_PANE";
                    break;
                case 4:
                    materialName = "YELLOW_STAINED_GLASS_PANE";
                    break;
                case 5:
                    materialName = "LIME_STAINED_GLASS_PANE";
                    break;
                case 6:
                    materialName = "PINK_STAINED_GLASS_PANE";
                    break;
                case 7:
                    materialName = "GRAY_STAINED_GLASS_PANE";
                    break;
                case 8:
                    materialName = "LIGHT_GRAY_STAINED_GLASS_PANE";
                    break;
                case 9:
                    materialName = "CYAN_STAINED_GLASS_PANE";
                    break;
                case 10:
                    materialName = "PURPLE_STAINED_GLASS_PANE";
                    break;
                case 11:
                    materialName = "BLUE_STAINED_GLASS_PANE";
                    break;
                case 12:
                    materialName = "BROWN_STAINED_GLASS_PANE";
                    break;
                case 13:
                    materialName = "GREEN_STAINED_GLASS_PANE";
                    break;
                case 14:
                    materialName = "RED_STAINED_GLASS_PANE";
                    break;
                case 15:
                    materialName = "BLACK_STAINED_GLASS_PANE";
                    break;
            }
            itemStack = new ItemStack(Material.getMaterial(materialName), 1);
        }
        return itemStack;
    }

    /**
     * @return 地图
     */
    public static Material getMapMaterial() {
        if (Material.getMaterial("EMPTY_MAP") != null) {
            return Material.getMaterial("EMPTY_MAP");
        } else {
            return Material.getMaterial("MAP");
        }
    }

    /**
     * 在较低版本的MC中，表示物品枚举名的“...制”的修饰成分时直接用了名词，后来被改为形容词
     * 例如：gold->golden,wood->wooden
     *
     * @return 获取金斧头Material
     */
    public static Material getGoldenAxeMaterial() {
        if (Material.getMaterial("GOLD_AXE") != null) {
            return Material.getMaterial("GOLD_AXE");
        } else {
            return Material.getMaterial("GOLDEN_AXE");
        }
    }

    /**
     * @return 获取金剑Material
     */
    public static Material getGoldenSwordMaterial() {
        if (Material.getMaterial("GOLD_SWORD") != null) {
            return Material.getMaterial("GOLD_SWORD");
        } else {
            return Material.getMaterial("GOLDEN_SWORD");
        }
    }

    /**
     * @return 获取木剑Material
     */
    public static Material getWoodenSwordMaterial() {
        if (Material.getMaterial("WOOD_SWORD") != null) {
            return Material.getMaterial("WOOD_SWORD");
        } else {
            return Material.getMaterial("WOODEN_SWORD");
        }
    }

    public static Material getExpBottleMaterial() {
        if (Material.getMaterial("EXPERIENCE_BOTTLE") != null) {
            return Material.getMaterial("EXPERIENCE_BOTTLE");
        } else {
            return Material.getMaterial("EXP_BOTTLE");
        }
    }

    public static Material getWatchMaterial() {
        if (Material.getMaterial("WATCH") != null) {
            return Material.getMaterial("WATCH");
        } else {
            return Material.getMaterial("CLOCK");
        }
    }

    /**
     * 待完善！！还有1.20的挂式木牌
     *
     * @return 点击的是否为壁式木牌
     */
    public static boolean isWallSignMaterial(Material material) {
        String name = material.name();
        if (Material.getMaterial("WALL_SIGN") != null) {
            return name.equals("WALL_SIGN");
        } else {
            switch (name) {
                case "ACACIA_WALL_SIGN":
                case "BIRCH_WALL_SIGN":
                case "CRIMSON_WALL_SIGN":
                case "DARK_OAK_WALL_SIGN":
                case "JUNGLE_WALL_SIGN":
                case "OAK_WALL_SIGN":
                case "WARPED_WALL_SIGN":
                    return true;
            }
        }
        return false;
    }

    /**
     * 待完善！！还有1.20的挂式木牌
     *
     * @return 点击的是否为立式木牌
     */
    public static boolean isSignPostMaterial(Material material) {
        String name = material.name();
        if (Material.getMaterial("SIGN_POST") != null) {
            return name.equals("SIGN_POST");
        } else {
            switch (name) {
                case "ACACIA_SIGN":
                case "BIRCH_SIGN":
                case "CRIMSON_SIGN":
                case "DARK_OAK_SIGN":
                case "JUNGLE_SIGN":
                case "OAK_SIGN":
                case "WARPED_SIGN":
                    return true;
            }
        }
        return false;
    }

    /**
     * @return 玩家头颅Material
     */
    public static boolean isPlayerSkull(Material material) {
        String name = material.name();
        if (Material.getMaterial("SKULL") != null) {
            return name.equals("SKULL");
        } else {
            return name.equals("PLAYER_HEAD") || name.equals("PLAYER_WALL_HEAD");
        }
    }

    /**
     * @return 主手上拿着的物品
     */
    public static ItemStack getItemInMainHand(Player player) {
        ItemStack item = null;
        try {
            Class<?> playerClass = Class.forName("org.bukkit.entity.Player");
            Class<?> playerInventoryClass = Class
                    .forName("org.bukkit.inventory.PlayerInventory");
            try {
                Method method = playerClass.getMethod("getInventory");
                PlayerInventory inventory = (PlayerInventory) method
                        .invoke(player);
                Method method2 = playerInventoryClass
                        .getMethod("getItemInMainHand");
                item = (ItemStack) method2.invoke(inventory);
            } catch (NoSuchMethodException e) {
                Method method = playerClass.getMethod("getItemInHand");
                item = (ItemStack) method.invoke(player);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return item;
    }

    /**
     * 设置主手物品
     */
    public static void setItemInMainHand(Player player, ItemStack item) {
        try {
            Class<?> playerClass = Class.forName("org.bukkit.entity.Player");
            Class<?> itemStackClass = Class
                    .forName("org.bukkit.inventory.ItemStack");
            Class<?> playerInventoryClass = Class
                    .forName("org.bukkit.inventory.PlayerInventory");
            try {
                Method method2 = playerClass.getMethod("getInventory");
                PlayerInventory inventory = (PlayerInventory) method2
                        .invoke(player);
                Method method3 = playerInventoryClass.getMethod(
                        "setItemInMainHand", itemStackClass);
                method3.invoke(inventory, item);
            } catch (NoSuchMethodException e) {
                Method method = playerClass.getMethod("setItemInHand",
                        itemStackClass);
                method.invoke(player, item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ProjectileSource getProjectileSource(Entity entity,
                                                       String entityName) {
        ProjectileSource ps = null;
        try {
            Class<?> clazz = Class.forName("org.bukkit.entity." + entityName);
            Method method = clazz.getMethod("getShooter");
            ps = (ProjectileSource) (method.invoke(entity));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ps;
    }

    /**
     * @return 当前MC版本是否有ItemFlag属性
     */
    public static boolean isHasItemFlagMethod() {
        Class<?> clazz;
        try {
            clazz = Class.forName("org.bukkit.inventory.meta.ItemMeta");
            for (Method method : clazz.getMethods()) {
                if (method.getName().equals("addItemFlags")) {
                    return true;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @return 当前MC版本是否有副手功能
     */
    public static boolean isHasOffHandMethod() {
        Class<?> clazz;
        try {
            clazz = Class.forName("org.bukkit.inventory.PlayerInventory");
            for (Method method : clazz.getMethods()) {
                if (method.getName().equals("getItemInOffHand")) {
                    return true;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static ItemStack getItemInOffHand(Player player) {
        ItemStack item = null;
        try {
            Class<?> clazz = Class.forName("org.bukkit.entity.Player");
            Method method = clazz.getMethod("getInventory");
            PlayerInventory inventory = (PlayerInventory) method.invoke(player);
            Class<?> clazz2 = Class
                    .forName("org.bukkit.inventory.PlayerInventory");
            Method method2 = clazz2.getMethod("getItemInOffHand");
            item = (ItemStack) method2.invoke(inventory);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return item;
    }

    public static void setItemInOffHand(Player player, ItemStack item) {
        try {
            Class<?> clazzPlayer = Class.forName("org.bukkit.entity.Player");
            Class<?> clazzPlayerInventory = Class
                    .forName("org.bukkit.inventory.PlayerInventory");
            Class<?> itemStackClass = Class
                    .forName("org.bukkit.inventory.ItemStack");
            Method method = clazzPlayer.getMethod("getInventory");
            PlayerInventory inventory = (PlayerInventory) method.invoke(player);
            Method method2 = clazzPlayerInventory.getMethod("setItemInOffHand",
                    itemStackClass);
            method2.invoke(inventory, item);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
