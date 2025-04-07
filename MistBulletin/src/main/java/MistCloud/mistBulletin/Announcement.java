package MistCloud.mistBulletin;

import java.util.List;

public class Announcement {
    private final String id;
    private final String message;
    private final int interval;
    private final boolean loop;
    private final List<String> servers;
    private final String clickableText;
    private final String clickableCommand;
    private long lastSentTime;
    private final boolean clickableEnabled;

    public Announcement(String id, String message, int interval, boolean loop, List<String> servers, String clickableText, String clickableCommand, boolean clickableEnabled) {
        this.id = id;
        this.message = message;
        this.interval = interval;
        this.loop = loop;
        this.servers = servers;
        this.clickableText = clickableText;
        this.clickableCommand = clickableCommand;
        this.lastSentTime = 0;
        this.clickableEnabled = clickableEnabled;
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public int getInterval() {
        return interval;
    }

    public boolean isLoop() {
        return loop;
    }

    public long getLastSentTime() {
        return lastSentTime;
    }

    public void updateLastSentTime() {
        this.lastSentTime = System.currentTimeMillis();
    }

    public boolean canSend() {
        return System.currentTimeMillis() - lastSentTime >= interval * 1000L;
    }

    public List<String> getServers() {
        return servers;
    }

    public boolean shouldSendToServer(String serverName) {
        return servers.isEmpty() || servers.contains(serverName);
    }

    public String getClickableText() {
        return clickableText;
    }

    public String getClickableCommand() {
        return clickableCommand;
    }

    public boolean isClickableEnabled() {
        return clickableEnabled;
    }
} 