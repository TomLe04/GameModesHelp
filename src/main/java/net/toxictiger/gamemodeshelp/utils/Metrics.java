package net.toxictiger.gamemodeshelp.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Metrics {
    private final JavaPlugin plugin;
    private static final String LATEST_VERSION_URL = "https://api.spigotmc.org/legacy/update.php?resource=YOUR_RESOURCE_ID"; // Change this to your Spigot resource ID

    public Metrics(JavaPlugin plugin) {
        this.plugin = plugin;
        checkForUpdates();
    }

    private void checkForUpdates() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(LATEST_VERSION_URL).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String latestVersion = reader.readLine();
                reader.close();

                String currentVersion = plugin.getDescription().getVersion();

                if (!currentVersion.equalsIgnoreCase(latestVersion)) {
                    // Log to the console
                    Bukkit.getLogger().warning("[GameModesHelp] A new version is available: " + latestVersion + " (You have " + currentVersion + ")");
                    Bukkit.getLogger().warning("[GameModesHelp] Download the latest update here: https://www.spigotmc.org/resources/YOUR_RESOURCE_ID/");
                    
                    // Notify players who are running the server (OPs)
                    notifyServerOwners(latestVersion, currentVersion);
                } else {
                    Bukkit.getLogger().info("[GameModesHelp] You are using the latest version: " + currentVersion);
                }
            } catch (Exception e) {
                Bukkit.getLogger().warning("[GameModesHelp] Failed to check for updates.");
            }
        });
    }

    private void notifyServerOwners(String latestVersion, String currentVersion) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isOp()) { // Only notify server owners (OPs)
                    player.sendMessage("§c[GameModesHelp] A new version is available: §e" + latestVersion);
                    player.sendMessage("§7You have: §f" + currentVersion);
                    player.sendMessage("§aDownload the latest update here: §bhttps://www.spigotmc.org/resources/YOUR_RESOURCE_ID/");
                }
            }
        });
    }
}