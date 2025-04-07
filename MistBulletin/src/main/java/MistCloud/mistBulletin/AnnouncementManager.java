package MistCloud.mistBulletin;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;

public class AnnouncementManager {
    private final MistBulletin plugin;
    private ScheduledTask announcementTask;
    private long lastPerformanceLog = 0;
    private int announcementsSent = 0;

    public AnnouncementManager(MistBulletin plugin) {
        this.plugin = plugin;
        startAnnouncementTask();
    }

    private void startAnnouncementTask() {
        // 每秒检查一次是否需要发送公告
        announcementTask = plugin.getServer().getScheduler()
                .buildTask(plugin, this::checkAndSendAnnouncements)
                .repeat(1L, TimeUnit.SECONDS)
                .schedule();
    }

    private void checkAndSendAnnouncements() {
        long start = System.currentTimeMillis();
        
        if (plugin.getServer().getAllPlayers().isEmpty()) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        for (Announcement announcement : plugin.getConfig().getAnnouncements().values()) {
            if (announcement.isLoop()) {
                if (currentTime - announcement.getLastSentTime() < announcement.getInterval() * 1000L) {
                    continue;
                }
            }
            
            try {
                broadcastAnnouncement(announcement);
                announcement.updateLastSentTime();
            } catch (Exception e) {
                plugin.getLogger().error("发送公告时发生错误: " + announcement.getId(), e);
            }
        }
        
        // 性能监控
        long end = System.currentTimeMillis();
        if (end - lastPerformanceLog >= 300000 && plugin.getConfig().isPerformanceReportEnabled()) {
            String performanceMessage = String.format(
                "&c[MistBulletin] 性能报告: &a处理用时 %dms, 已发送公告数 %d, 当前在线玩家 %d", 
                (end - start), 
                announcementsSent,
                plugin.getServer().getAllPlayers().size()
            );
            plugin.getServer().getConsoleCommandSource().sendMessage(
                LegacyComponentSerializer.legacyAmpersand().deserialize(performanceMessage)
            );
            lastPerformanceLog = end;
        }
    }

    private void broadcastAnnouncementInBatches(Component message, Announcement announcement) {
        List<Player> players = new ArrayList<>(plugin.getServer().getAllPlayers());
        boolean batchSending = plugin.getConfig().isBatchSendingEnabled();
        
        if (!batchSending) {
            // 如果未启用分批发送，直接发送给所有玩家
            broadcastToPlayers(players, message, announcement);
            return;
        }
        
        int batchSize = plugin.getConfig().getBatchSize();
        long batchDelay = plugin.getConfig().getBatchDelay();
        
        // 计算需要多少批次
        int totalBatches = (players.size() + batchSize - 1) / batchSize;
        
        for (int i = 0; i < totalBatches; i++) {
            final int batchIndex = i;
            // 延迟发送每一批
            plugin.getServer().getScheduler()
                .buildTask(plugin, () -> {
                    int fromIndex = batchIndex * batchSize;
                    int toIndex = Math.min(fromIndex + batchSize, players.size());
                    List<Player> batch = players.subList(fromIndex, toIndex);
                    broadcastToPlayers(batch, message, announcement);
                })
                .delay(batchDelay * i, TimeUnit.MILLISECONDS)
                .schedule();
        }
    }
    
    private void broadcastToPlayers(List<Player> players, Component message, Announcement announcement) {
        for (Player player : players) {
            player.getCurrentServer().ifPresent(serverConnection -> {
                String serverName = serverConnection.getServerInfo().getName();
                if (announcement.shouldSendToServer(serverName)) {
                    try {
                        player.sendMessage(message);
                        announcementsSent++;
                        if (plugin.getConfig().isDebugLogEnabled()) {
                            plugin.getLogger().info(String.format(
                                "向玩家 %s 发送公告 %s (服务器: %s)",
                                player.getUsername(),
                                announcement.getId(),
                                serverName
                            ));
                        }
                    } catch (Exception e) {
                        plugin.getLogger().error("向玩家发送公告时发生错误: " + player.getUsername(), e);
                    }
                } else if (plugin.getConfig().isDebugLogEnabled()) {
                    plugin.getLogger().info(String.format(
                        "跳过向玩家 %s 发送公告 %s (当前服务器 %s 不在目标服务器列表中)",
                        player.getUsername(),
                        announcement.getId(),
                        serverName
                    ));
                }
            });
        }
    }

    public void broadcastAnnouncement(Announcement announcement) {
        Component message = buildAnnouncementMessage(announcement);
        broadcastAnnouncementInBatches(message, announcement);
    }

    private Component buildAnnouncementMessage(Announcement announcement) {
        // 先构建基础消息
        String messageContent = announcement.getMessage();
        
        // 如果启用了点击功能，将点击文本添加到消息内容中
        if (announcement.isClickableEnabled() && 
            announcement.getClickableText() != null && 
            announcement.getClickableCommand() != null) {
            messageContent = messageContent + "\n" + announcement.getClickableText();
        }
        
        // 使用完整消息替换格式中的占位符
        String formattedMessage = plugin.getConfig().getFormat()
                .replace("{message}", messageContent);
        
        // 如果启用了点击功能，创建带点击效果的组件
        if (announcement.isClickableEnabled() && 
            announcement.getClickableText() != null && 
            announcement.getClickableCommand() != null) {
            
            String[] parts = formattedMessage.split(announcement.getClickableText());
            if (parts.length > 1) {
                Component beforeClick = LegacyComponentSerializer.legacyAmpersand()
                    .deserialize(parts[0]);
                    
                Component clickable = LegacyComponentSerializer.legacyAmpersand()
                    .deserialize(announcement.getClickableText())
                    .clickEvent(ClickEvent.runCommand(announcement.getClickableCommand()))
                    .hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacyAmpersand()
                        .deserialize("&7点击执行: &e" + announcement.getClickableCommand())))
                    .decoration(TextDecoration.UNDERLINED, true);
                    
                Component afterClick = LegacyComponentSerializer.legacyAmpersand()
                    .deserialize(parts[1]);
                    
                return beforeClick.append(clickable).append(afterClick);
            }
        }
        
        return LegacyComponentSerializer.legacyAmpersand()
            .deserialize(formattedMessage);
    }

    public void broadcastAnnouncementById(String id) {
        Announcement announcement = plugin.getConfig().getAnnouncements().get(id);
        if (announcement != null) {
            try {
                broadcastAnnouncement(announcement);
                announcement.updateLastSentTime();
                plugin.getLogger().info("[MistBulletin] 已手动发送公告: " + id);
            } catch (Exception e) {
                plugin.getLogger().error("[MistBulletin] 手动发送公告时发生错误: " + id, e);
            }
        } else {
            plugin.getLogger().error("[MistBulletin] 未找到ID为 " + id + " 的公告");
        }
    }

    public void stopTask() {
        if (announcementTask != null) {
            announcementTask.cancel();
        }
    }
} 