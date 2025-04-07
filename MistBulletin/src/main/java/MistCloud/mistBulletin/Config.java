package MistCloud.mistBulletin;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {
    private final MistBulletin plugin;
    private final Path dataDirectory;
    private final Path configFile;
    private ConfigurationNode config;
    private final Map<String, Announcement> announcements = new HashMap<>();
    private String format;
    private boolean batchSendingEnabled;
    private int batchSize;
    private long batchDelay;
    private boolean performanceReportEnabled = true;
    private boolean debugLogEnabled = false;

    public Config(MistBulletin plugin, Path dataDirectory) {
        this.plugin = plugin;
        this.dataDirectory = dataDirectory;
        this.configFile = dataDirectory.resolve("config.yml");
    }

    public void loadConfig() {
        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
                plugin.getLogger().info("创建配置目录");
            }

            if (!Files.exists(configFile)) {
                Files.copy(getClass().getResourceAsStream("/config.yml"), configFile);
                plugin.getLogger().info("创建默认配置文件");
            }

            config = YamlConfigurationLoader.builder()
                .file(configFile.toFile())
                .build()
                .load();
            
            loadAnnouncements();
            format = config.node("format").getString("{message}");
            plugin.getLogger().info("成功加载配置文件");
            plugin.getLogger().info("已加载 " + announcements.size() + " 个公告");

            // 加载性能优化设置
            ConfigurationNode performanceNode = config.node("performance");
            batchSendingEnabled = performanceNode.node("batch_sending").getBoolean(true);
            batchSize = performanceNode.node("batch_size").getInt(50);
            batchDelay = performanceNode.node("batch_delay").getLong(100);
            
            // 加载性能报告设置
            performanceReportEnabled = config.node("performance", "report_enabled").getBoolean(true);
            
            // 加载调试日志设置
            debugLogEnabled = config.node("performance", "debug_log").getBoolean(false);
            
        } catch (IOException e) {
            plugin.getLogger().error("加载配置文件时发生错误", e);
        }
    }

    private void loadAnnouncements() {
        announcements.clear();
        ConfigurationNode announcementsNode = config.node("announcements");
        
        for (Map.Entry<Object, ? extends ConfigurationNode> entry : announcementsNode.childrenMap().entrySet()) {
            String id = entry.getKey().toString();
            ConfigurationNode node = entry.getValue();
            
            try {
                String message = node.node("message").getString("");
                int interval = node.node("interval").getInt(300);
                boolean loop = node.node("loop").getBoolean(false);
                List<String> servers = node.node("servers").getList(String.class, new ArrayList<>());
                
                // 检查clickable节点是否存在
                ConfigurationNode clickableNode = node.node("clickable");
                boolean hasClickableConfig = !clickableNode.empty();
                
                // 如果clickable配置存在且enabled为true，才启用点击功能
                boolean clickableEnabled = hasClickableConfig && clickableNode.node("enabled").getBoolean(false);
                String clickableText = hasClickableConfig ? clickableNode.node("text").getString("") : "";
                String clickableCommand = hasClickableConfig ? clickableNode.node("command").getString("") : "";
                
                announcements.put(id, new Announcement(id, message, interval, loop, 
                                                     servers, clickableText, clickableCommand,
                                                     clickableEnabled));
            } catch (SerializationException e) {
                plugin.getLogger().error("Error loading announcement " + id, e);
            }
        }
    }

    public synchronized Map<String, Announcement> getAnnouncements() {
        return new HashMap<>(announcements);
    }

    public synchronized void reload() {
        loadConfig();
    }

    public String getFormat() {
        return format;
    }

    public boolean isBatchSendingEnabled() {
        return batchSendingEnabled;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public long getBatchDelay() {
        return batchDelay;
    }

    public boolean isPerformanceReportEnabled() {
        return performanceReportEnabled;
    }

    public boolean isDebugLogEnabled() {
        return debugLogEnabled;
    }
} 