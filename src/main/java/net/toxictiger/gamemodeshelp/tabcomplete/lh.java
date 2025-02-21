package net.toxictiger.gamemodeshelp.tabcomplete;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class lh implements TabCompleter {
    private final FileConfiguration config;

    public lh(FileConfiguration config) {
        this.config = config;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            // Add "reload" option if the sender has permission
            if (sender.hasPermission("gamemodeshelp.reload")) {
                suggestions.add("reload");
            }

			// Add "ver" option
            suggestions.add("ver");
			
            // Add game modes from config
            if (config.getConfigurationSection("gameModes") != null) {
                Set<String> gameModes = config.getConfigurationSection("gameModes").getKeys(false);
                for (String mode : gameModes) {
                    if (sender.hasPermission("gamemodeshelp.help." + mode)) {
                        suggestions.add(mode);
                    }
                }
            }
        } else if (args.length == 2) {
            String gameMode = args[0].toLowerCase();
            if (config.contains("help." + gameMode)) {
                Set<String> pages = config.getConfigurationSection("help." + gameMode).getKeys(false);
                suggestions.addAll(pages);
            }
        }

        return suggestions;
    }
}
