package MistCloud.mistBulletin;

import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BulletinCommand implements SimpleCommand {
    private final MistBulletin plugin;

    public BulletinCommand(MistBulletin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!invocation.source().hasPermission("mistbulletin.admin")) {
            invocation.source().sendMessage(Component.text("你没有权限使用此命令！").color(NamedTextColor.RED));
            return;
        }

        String[] args = invocation.arguments();

        if (args.length == 0) {
            showHelp(invocation);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                plugin.getConfig().reload();
                invocation.source().sendMessage(Component.text("配置文件已重新加载！").color(NamedTextColor.GREEN));
                break;

            case "send":
                if (args.length < 2) {
                    invocation.source().sendMessage(Component.text("用法: /mistbulletin send <公告ID>").color(NamedTextColor.RED));
                    return;
                }
                String id = args[1];
                if (plugin.getConfig().getAnnouncements().containsKey(id)) {
                    plugin.getAnnouncementManager().broadcastAnnouncementById(id);
                    invocation.source().sendMessage(Component.text("已发送公告: " + id).color(NamedTextColor.GREEN));
                } else {
                    invocation.source().sendMessage(Component.text("找不到指定ID的公告！").color(NamedTextColor.RED));
                }
                break;

            case "list":
                showAnnouncementList(invocation);
                break;

            case "temp":
                if (args.length < 3) {
                    invocation.source().sendMessage(Component.text("用法: /mistbulletin temp <服务器名/all> <公告内容>").color(NamedTextColor.RED));
                    return;
                }
                String server = args[1];
                // 将剩余参数组合成完整的消息
                StringBuilder message = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    message.append(args[i]).append(" ");
                }
                
                List<String> servers = new ArrayList<>();
                if (!server.equalsIgnoreCase("all")) {
                    servers.add(server);
                }
                
                // 创建临时公告
                Announcement tempAnnouncement = new Announcement(
                    "temp_" + System.currentTimeMillis(),
                    message.toString().trim(),
                    0,
                    false,
                    servers,
                    null,
                    null,
                    false
                );
                
                plugin.getAnnouncementManager().broadcastAnnouncement(tempAnnouncement);
                invocation.source().sendMessage(Component.text("已发送临时公告到 " + 
                    (server.equalsIgnoreCase("all") ? "所有服务器" : server))
                    .color(NamedTextColor.GREEN));
                break;

            default:
                showHelp(invocation);
                break;
        }
    }

    private void showHelp(Invocation invocation) {
        invocation.source().sendMessage(Component.text("=== MistBulletin 帮助 ===").color(NamedTextColor.GOLD));
        invocation.source().sendMessage(Component.text("/mistbulletin reload - 重新加载配置文件").color(NamedTextColor.YELLOW));
        invocation.source().sendMessage(Component.text("/mistbulletin send <公告ID> - 立即发送指定公告").color(NamedTextColor.YELLOW));
        invocation.source().sendMessage(Component.text("/mistbulletin list - 显示所有公告列表").color(NamedTextColor.YELLOW));
        invocation.source().sendMessage(Component.text("/mistbulletin temp <服务器名/all> <公告内容> - 发送临时公告").color(NamedTextColor.YELLOW));
    }

    private void showAnnouncementList(Invocation invocation) {
        Map<String, Announcement> announcements = plugin.getConfig().getAnnouncements();
        invocation.source().sendMessage(Component.text("=== 公告列表 ===").color(NamedTextColor.GOLD));
        for (Map.Entry<String, Announcement> entry : announcements.entrySet()) {
            Announcement announcement = entry.getValue();
            Component message = Component.text()
                .content("ID: " + entry.getKey())
                .color(NamedTextColor.YELLOW)
                .append(Component.text(" | 循环: " + (announcement.isLoop() ? "是" : "否"))
                    .color(NamedTextColor.GRAY))
                .append(Component.text(" | 间隔: " + announcement.getInterval() + "秒")
                    .color(NamedTextColor.GRAY))
                .build();
            invocation.source().sendMessage(message);
        }
    }
} 