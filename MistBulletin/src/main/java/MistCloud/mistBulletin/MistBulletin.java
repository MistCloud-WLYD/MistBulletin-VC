package MistCloud.mistBulletin;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.nio.file.Path;

@Plugin(
        id = "mistbulletin",
        name = "MistBulletin",
        version = "1.1.1",
        authors = {"Jens_Hon", "Kenty_Xu", "ANshiwang"}
)
public class MistBulletin {
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private Config config;
    private AnnouncementManager announcementManager;

    @Inject
    public MistBulletin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // 加载配置
        config = new Config(this, dataDirectory);
        config.loadConfig();

        // 初始化公告管理器
        announcementManager = new AnnouncementManager(this);

        // 注册命令
        server.getCommandManager().register("mistbulletin", new BulletinCommand(this));

        // 插件加载信息
        server.getConsoleCommandSource().sendMessage(Component.text("[MistBulletin] 插件已加载").color(NamedTextColor.GREEN));
        server.getConsoleCommandSource().sendMessage(Component.text("MistCloud - 雾里云端").color(NamedTextColor.GOLD));
        server.getConsoleCommandSource().sendMessage(
            Component.text("交流群: ").color(NamedTextColor.AQUA)
                .append(Component.text("983387954")
                    .color(NamedTextColor.LIGHT_PURPLE)
                    .decorate(TextDecoration.BOLD))
        );

        // 显示像素 LOGO
        Component[] logo = {
            Component.text("雾里云端祝您服运昌隆").color(NamedTextColor.LIGHT_PURPLE),
            Component.text("███╗   ███╗  ").color(NamedTextColor.GREEN)
                .append(Component.text("██╗██╗  ").color(NamedTextColor.AQUA))
                .append(Component.text("███████╗  ").color(NamedTextColor.YELLOW))
                .append(Component.text("████████╗").color(NamedTextColor.RED)),
            Component.text("████╗ ████║  ").color(NamedTextColor.GREEN)
                .append(Component.text("██║██║  ").color(NamedTextColor.AQUA))
                .append(Component.text("██╔════╝  ").color(NamedTextColor.YELLOW))
                .append(Component.text("╚══██╔══╝").color(NamedTextColor.RED)),
            Component.text("██╔████╔██║  ").color(NamedTextColor.GREEN)
                .append(Component.text("██║██║  ").color(NamedTextColor.AQUA))
                .append(Component.text("███████╗  ").color(NamedTextColor.YELLOW))
                .append(Component.text("   ██║   ").color(NamedTextColor.RED)),
            Component.text("██║╚██╔╝██║  ").color(NamedTextColor.GREEN)
                .append(Component.text("██║██║  ").color(NamedTextColor.AQUA))
                .append(Component.text("╚════██║  ").color(NamedTextColor.YELLOW))
                .append(Component.text("   ██║   ").color(NamedTextColor.RED)),
            Component.text("██║ ╚═╝ ██║  ").color(NamedTextColor.GREEN)
                .append(Component.text("██║██║  ").color(NamedTextColor.AQUA))
                .append(Component.text("███████║  ").color(NamedTextColor.YELLOW))
                .append(Component.text("   ██║   ").color(NamedTextColor.RED)),
            Component.text("╚═╝     ╚═╝  ").color(NamedTextColor.GREEN)
                .append(Component.text("╚═╝╚═╝  ").color(NamedTextColor.AQUA))
                .append(Component.text("╚══════╝  ").color(NamedTextColor.YELLOW))
                .append(Component.text("   ╚═╝   ").color(NamedTextColor.RED))
        };
        for (Component line : logo) {
            server.getConsoleCommandSource().sendMessage(line);
        }
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (announcementManager != null) {
            announcementManager.stopTask();
        }
        server.getConsoleCommandSource().sendMessage(Component.text("[MistBulletin] 插件已卸载").color(NamedTextColor.RED));
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }

    public Config getConfig() {
        return config;
    }

    public AnnouncementManager getAnnouncementManager() {
        return announcementManager;
    }
}
