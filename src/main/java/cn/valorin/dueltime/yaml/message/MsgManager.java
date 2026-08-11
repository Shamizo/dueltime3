package cn.valorin.dueltime.yaml.message;

import cn.valorin.dueltime.DuelTimePlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MsgManager {
    protected static final Map<String, YamlConfiguration> languageYamlFileMap = new HashMap<>();

    public MsgManager() {
        check();
        reload();
        MsgBuilder.prefix = DuelTimePlugin.getInstance().getCfgManager().getPrefix();
    }

    /**
     * 获取文件名-文件Map
     */
    public Map<String, YamlConfiguration> getLanguageYamlFileMap() {
        return languageYamlFileMap;
    }

    /**
     * 动态更新前缀缓存，一般在CfgManager重载时被调用
     *
     * @param prefix 从配置文件中传入的前缀
     */
    public void updatePrefix(String prefix) {
        MsgBuilder.prefix = prefix;
    }

    /**
     * 检查数据文件夹中的语言文件夹(languages)是否存在，若不存在则从预设语言文件中逐个复制过去
     */
    public void check() {
        String[] presetFileNames = {"简体中文.yml", "繁體中文.yml", "繁體中文(機翻).yml",};
        File targetLanguageFolder = new File(DuelTimePlugin.getInstance().getDataFolder(), "languages");

        if (!targetLanguageFolder.exists()) {
            targetLanguageFolder.mkdirs();
            for (String fileName : presetFileNames) {
                DuelTimePlugin.getInstance().saveResource("languages/" + fileName, false);
            }
        } else {
            for (String fileName : targetLanguageFolder.list()) {
                if (!Arrays.asList(presetFileNames).contains(fileName)) {
                    continue;
                }
                InputStream inputStream = DuelTimePlugin.getInstance().getResource("languages/" + fileName);
                YamlConfiguration originalYaml = YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                File languageFile = new File(targetLanguageFolder, fileName);
                YamlConfiguration targetYaml;
                try {
                    targetYaml = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(languageFile), StandardCharsets.UTF_8));
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                Set<String> originalKeys = originalYaml.getKeys(true);
                for (String key : originalKeys) {
                    if (!targetYaml.contains(key)) {
                        targetYaml.set(key, originalYaml.get(key));
                        System.out.println("[DT插件]填补的key: " + key + "   这条debug后续版本会删掉");
                    }
                }
                try {
                    targetYaml.save(languageFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * （重新）加载语言文件到缓存中
     */
    public void reload() {
        File languageFolder = new File("plugins/DuelTime/languages");
        if (!languageFolder.exists()) {
            return;
        }
        languageYamlFileMap.clear();
        for (File installedLanguageFile : languageFolder.listFiles()) {
            String fileName = installedLanguageFile.getName();
            if (!fileName.endsWith(".yml") && !fileName.endsWith(".yaml")) {
                continue;
            }
            fileName = fileName.substring(0, fileName.length() - (fileName.endsWith(".yml") ? 4 : 5));
            languageYamlFileMap.put(fileName, YamlConfiguration.loadConfiguration(installedLanguageFile));
        }
    }
}


