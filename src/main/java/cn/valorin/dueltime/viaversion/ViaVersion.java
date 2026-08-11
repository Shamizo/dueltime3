package cn.valorin.dueltime.viaversion;

import cn.valorin.dueltime.DuelTimePlugin;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static cn.valorin.dueltime.viaversion.ViaVersion.TitleType.*;

public class ViaVersion {

    private static Class<?> getNmsClass(String name)
            throws ClassNotFoundException {
        if (DuelTimePlugin.serverVersionInt >= 17) {
            return Class.forName("net.minecraft.server." + name);
        } else {
            return Class.forName("net.minecraft.server." + DuelTimePlugin.serverVersion + "." + name);
        }
    }

    private static Class<?> getCbClass(String name)
            throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit."
                + DuelTimePlugin.serverVersion + "." + name);
    }

    private static Class<?> iChatBaseComponent;
    private static Class<?> chatComponentText;
    private static Class<?> packet;
    private static Class<?> packetPlayOutTitle;
    private static Class<?> enumTitleAction;

    public static void getClassesForTitleAndAction() {
        try {
            iChatBaseComponent = getNmsClass("IChatBaseComponent");
            chatComponentText = getNmsClass("ChatComponentText");
            packet = getNmsClass("Packet");
            packetPlayOutTitle = getNmsClass("PacketPlayOutTitle");
            enumTitleAction = getNmsClass("PacketPlayOutTitle$EnumTitleAction");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public enum TitleType {
        TITLE, SUBTITLE, PARALLEL, LINE
    }

    /*
    发送可自定义淡入、停留、淡出时间的Title文字，如果为1.8以下的低版本，可以选择是否以屏幕文字的形式发送主title或副title或都发送（并行）
     */
    public static void sendTitle(Player player, String title, String subTitle,
                                 int fadeIn, int stay, int fadeOut, TitleType titleTypeAsMessage) {
        int version = DuelTimePlugin.serverVersionInt;
        if (DuelTimePlugin.serverVersionInt <= 7) {
            if (titleTypeAsMessage != null) {
                if (titleTypeAsMessage == TITLE)
                    player.sendMessage(title);
                else if (titleTypeAsMessage == SUBTITLE)
                    player.sendMessage(subTitle);
                else if (titleTypeAsMessage == PARALLEL) {
                    player.sendMessage(title);
                    player.sendMessage(subTitle);
                } else
                    player.sendMessage(title + " " + subTitle);
            }
        } else if (version <= 9) {
            try {
                Enum<?>[] enumConstants = (Enum<?>[]) enumTitleAction
                        .getEnumConstants();
                Object enumTITLE = null;
                Object enumSUBTITLE = null;
                Object enumTIMES = null;
                for (Enum<?> enum1 : enumConstants) {
                    String name = enum1.name();
                    if (name.equals("TITLE")) {
                        enumTITLE = enum1;
                    }
                    if (name.equals("SUBTITLE")) {
                        enumSUBTITLE = enum1;
                    }
                    if (name.equals("TIMES")) {
                        enumTIMES = enum1;
                    }
                }
                Object chatComponentTextTitleInstance = chatComponentText
                        .getConstructor(String.class).newInstance(title);
                Object chatComponentTextSubTitleInstance = chatComponentText
                        .getConstructor(String.class).newInstance(subTitle);
                Object packetPlayOutTitleTitleInstance = packetPlayOutTitle
                        .getConstructor(enumTitleAction, iChatBaseComponent)
                        .newInstance(enumTITLE, chatComponentTextTitleInstance);
                Object packetPlayOutTitleSubTitleInstance = packetPlayOutTitle
                        .getConstructor(enumTitleAction, iChatBaseComponent)
                        .newInstance(enumSUBTITLE,
                                chatComponentTextSubTitleInstance);
                Object packetPlayOutTitleTimeInstance = packetPlayOutTitle
                        .getConstructor(enumTitleAction, iChatBaseComponent,
                                int.class, int.class, int.class).newInstance(
                                enumTIMES, null, fadeIn, stay, fadeOut);
                Object craftPlayer = getCbClass("entity.CraftPlayer").cast(
                        player);
                Object entityPlayer = craftPlayer.getClass()
                        .getMethod("getHandle").invoke(craftPlayer);
                Object playerConnection = entityPlayer.getClass()
                        .getField("playerConnection").get(entityPlayer);
                playerConnection
                        .getClass()
                        .getMethod("sendPacket", packet)
                        .invoke(playerConnection,
                                packetPlayOutTitleTitleInstance);
                playerConnection
                        .getClass()
                        .getMethod("sendPacket", packet)
                        .invoke(playerConnection,
                                packetPlayOutTitleSubTitleInstance);
                playerConnection
                        .getClass()
                        .getMethod("sendPacket", packet)
                        .invoke(playerConnection,
                                packetPlayOutTitleTimeInstance);
            } catch (Exception ignored) {
            }
        } else {
            player.sendTitle(title, subTitle, fadeIn, stay, fadeOut);
        }
    }


    /*
    发送ActionBar文字，如果为1.8以下的低版本，可以选择是否以屏幕文字的形式发送主title
    高版本的API提供了直接发送ActionBar的方法，但我是基于1.12.2的API开发的，所以这里用的是相对落后的发送方法
     */
    public static void sendActionBar(Player player, String actionbar, boolean considerLowVersion) {
        int version = DuelTimePlugin.serverVersionInt;
        if (version <= 7) {
            if (considerLowVersion) {
                player.sendMessage(actionbar);
            }
        } else if (version <= 9) {
            try {
                Class<?> craftPlayerClass = getCbClass("entity.CraftPlayer");
                Class<?> packetPlayOutChatClass = getNmsClass("PacketPlayOutChat");
                Class<?> iChatBaseComponentClass = getNmsClass("IChatBaseComponent");
                Class<?> chatSerializerClass = getNmsClass("ChatSerializer");
                Class<?> packetClass = getNmsClass("Packet");
                Object craftPlayer = craftPlayerClass.cast(player);
                Object entityPlayer = craftPlayerClass.getMethod("getHandle").invoke(craftPlayer);
                Object playerConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
                Object chatBaseComponent = chatSerializerClass.getMethod("a", String.class)
                        .invoke(null, "{\"text\":\"" + actionbar + "\"}");
                Object packet = packetPlayOutChatClass.getConstructor(iChatBaseComponentClass, byte.class)
                        .newInstance(chatBaseComponent, (byte) 2);
                playerConnection.getClass().getMethod("sendPacket", packetClass).invoke(playerConnection, packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(actionbar));
        }
    }

    //用于生成染色粒子。我承认这是一个屎山方法，但nms的版本差异实在太大了！以后慢慢改
    public static void spawnRedstoneParticle(Player viewer, Location location, float colorR, float colorG, float colorB) {
        try {
            Object packet;
            if (DuelTimePlugin.serverVersionInt >= 17) {
                //还没研究清楚1.17及以上的发包形式，就暂时不采取不发包
                Class<?> dustOptionsClass = Class.forName("org.bukkit.Particle$DustOptions");
                Constructor<?> dustOptionsConstructor = dustOptionsClass.getConstructor(Color.class, float.class);
                Object dustOptions = dustOptionsConstructor.newInstance(Color.fromRGB((int) colorR, (int) colorG, (int) colorB), 1);
                Method spawnParticleMethod = viewer.getWorld().getClass().getMethod("spawnParticle", Particle.class, Location.class, int.class, double.class, double.class, double.class, Object.class);
                spawnParticleMethod.invoke(viewer.getWorld(), Particle.DUST, location, 0, 0, 0, 0, dustOptions);
            } else {
                Class<?> packetClass = getNmsClass("Packet");
                try {
                    //在搞清楚临界版本之前，先用try-catch分类讨论
                    Class<?> packetPlayOutWorldParticlesClass = getNmsClass("PacketPlayOutWorldParticles");
                    try {
                        //至少知道是1.16.5
                        Class<?> particleParamRedstoneClass = getNmsClass("ParticleParamRedstone");
                        Class<?> particleParamClass = getNmsClass("ParticleParam");
                        Constructor<?> particleParamRedstoneConstructor = particleParamRedstoneClass.getConstructor(float.class, float.class, float.class, float.class);
                        Object particleParamRedstone = particleParamRedstoneConstructor.newInstance(
                                colorR / 255.0f, colorG / 255.0f, colorB / 255.0f, 1.0f);
                        Object particleParam = particleParamClass.cast(particleParamRedstone);
                        packet = packetPlayOutWorldParticlesClass.getConstructor(
                                particleParamClass, boolean.class, double.class, double.class, double.class, float.class, float.class, float.class, float.class, int.class
                        ).newInstance(
                                particleParam, false, location.getX(), location.getY(), location.getZ(), 0, 0, 0, 1, 0);
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                        packet = packetPlayOutWorldParticlesClass.getConstructor(
                                String.class,
                                float.class,
                                float.class,
                                float.class,
                                float.class,
                                float.class,
                                float.class,
                                float.class,
                                int.class
                        ).newInstance(
                                "reddust",
                                (float) location.getX(),
                                (float) location.getY(),
                                (float) location.getZ(),
                                colorR / 255,
                                colorG / 255,
                                colorB / 255,
                                1.0f, // 粒子大小
                                0 // 粒子数量
                        );
                    }
                } catch (ClassNotFoundException | NoSuchMethodException e1) {
                    Class<?> packetPlayOutWorldParticlesClass = getNmsClass("PacketPlayOutWorldParticles");
                    Class<?> enumParticleClass = getNmsClass("EnumParticle");
                    Object reddustEnum = enumParticleClass.getField("REDSTONE").get(null);
                    packet = packetPlayOutWorldParticlesClass.getConstructor(
                            enumParticleClass,
                            boolean.class,
                            float.class,
                            float.class,
                            float.class,
                            float.class,
                            float.class,
                            float.class,
                            float.class,
                            int.class,
                            int[].class
                    ).newInstance(
                            reddustEnum, // 粒子类型
                            true, // 总是显示
                            (float) location.getX(),
                            (float) location.getY(),
                            (float) location.getZ(),
                            colorR / 255,
                            colorG / 255,
                            colorB / 255,
                            1.0f, // 粒子大小
                            0, // 粒子数量
                            new int[0] // 额外参数
                    );
                }
                Class<?> craftPlayerClass = getCbClass("entity.CraftPlayer");
                Object craftPlayer = craftPlayerClass.cast(viewer);
                Object entityPlayer = craftPlayerClass.getMethod("getHandle").invoke(craftPlayer);
                Object playerConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
                playerConnection.getClass().getMethod("sendPacket", packetClass).invoke(playerConnection, packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Object getCraftSplashPotion() {
        try {
            return getCbClass("entity.CraftSplashPotion");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object getCraftProjectile() {
        try {
            return getCbClass("entity.CraftProjectile");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object getCraftProjectileSource(Entity entity,
                                                  String entityName) {
        Object ps = null;
        try {
            Class<?> clazz = getCbClass("entity." + entityName);
            Method method = clazz.getMethod("getShooter");
            ps = method.invoke(entity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ps;
    }

    public static Sound getSound(String... soundNames) {
        try {
            Class<?> soundClass = Class.forName("org.bukkit.Sound");
            Enum<?>[] enumConstants = (Enum<?>[]) soundClass.getEnumConstants();
            if (enumConstants == null) {
                return null;
            }
            Sound enumACTIONBAR = null;
            for (String soundName : soundNames) {
                for (Enum<?> enum1 : enumConstants) {
                    if (enum1.name().equals(soundName)) {
                        enumACTIONBAR = Sound.valueOf(enum1.name());
                        break;
                    }
                }
            }
            return enumACTIONBAR;
        } catch (ClassNotFoundException ignored) {
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }


    /**
     * 以1.7及以后的API，方法Bukkit.getOnlinePlayers()的返回类型与之前的版本不同
     *
     * @return 玩家集合
     */
    public static List<Player> getOnlinePlayers() {
        List<Player> players = null;
        try {
            Class<?> clazz = Class.forName("org.bukkit.Bukkit");
            Method method = clazz.getMethod("getOnlinePlayers");
            if (method.getReturnType().equals(Collection.class)) {
                Collection<?> rawPlayers = (Collection<?>) (method
                        .invoke(Bukkit.getServer()));
                players = new ArrayList<>();
                for (Object o : rawPlayers) {
                    if (o instanceof Player) {
                        players.add((Player) o);
                    }
                }
            } else {
                players = Arrays.asList((Player[]) method.invoke(Bukkit
                        .getServer()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return players;
    }
}
