package net.toxictiger.gamemodeshelp.utils;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import net.toxictiger.gamemodeshelp.tabcomplete.lh;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GameModesHelp extends JavaPlugin implements CommandExecutor, TabCompleter {

    private FileConfiguration config;
    private File configFile;
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        configFile = new File(getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);
        getLogger().info("GameModesHelp has been enabled!");

        if (getCommand("help") != null) {
            getCommand("help").setExecutor(this);
            getCommand("help").setTabCompleter(new lh(config));
        } else {
            getLogger().severe("Command 'help' is not registered in plugin.yml!");
        }
        registerDynamicPermissions();
    }
// These codes allow admin/owner to grant each permissions for players in each game modes on luck perms
    // private void registerDynamicPermissions() {
        // if (config.getConfigurationSection("gameModes") != null) {
            // for (String mode : config.getConfigurationSection("gameModes").getKeys(false)) {
                // String permission = "gamemodeshelp.help." + mode;
                // if (getServer().getPluginManager().getPermission(permission) == null) {
                    // getServer().getPluginManager().addPermission(new Permission(permission, PermissionDefault.OP));
                    // getLogger().info("Registered permission: " + permission);
                // }
            // }
        // }
    // }


// Theese codes allow available game modes by Default group to true
    private void registerDynamicPermissions() {
    if (config.getConfigurationSection("gameModes") != null) {
        for (String mode : config.getConfigurationSection("gameModes").getKeys(false)) {
            String permission = "gamemodeshelp.help." + mode;

            // Check if permission already exists
            if (getServer().getPluginManager().getPermission(permission) == null) {
                Permission perm = new Permission(permission, PermissionDefault.TRUE); // ✅ Default to ALLOW
                getServer().getPluginManager().addPermission(perm);
                getLogger().info("Registered permission (default true): " + permission);
            }
        }
    }
}

	@Override
    public void onDisable() {
        getLogger().info("GameModesHelp has been disabled.");
    }

    private void reloadConfigIfChanged() {
        reloadConfig();
        config = getConfig();
        registerDynamicPermissions(); // Ensure new permissions are loaded
        getLogger().info("Config file reloaded.");
    }

    private void unregisterDynamicPermissions() {
        if (config.getConfigurationSection("gameModes") != null) {
            for (String mode : config.getConfigurationSection("gameModes").getKeys(false)) {
                String permission = "gamemodeshelp.help." + mode;
                if (getServer().getPluginManager().getPermission(permission) != null) {
                    getServer().getPluginManager().removePermission(permission);
                    getLogger().info("Unregistered permission: " + permission);
                }
            }
        }
        getServer().dispatchCommand(getServer().getConsoleSender(), "lp sync"); // Ensure LuckPerms updates immediately
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("gamemodeshelp.reload")) {
                reloadConfigIfChanged();
                sender.sendMessage(ChatColor.GREEN + "Configuration reloaded successfully.");
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to reload the configuration.");
            }
            return true;
        }
		
		if (args.length > 0 && args[0].equalsIgnoreCase("ver")) {
			String authors = getDescription().getAuthors().isEmpty()
				? "Unknown"
				: String.join(", ", getDescription().getAuthors());
			sender.sendMessage(ChatColor.GREEN + "GameModesHelp version: " + getDescription().getVersion());
			sender.sendMessage(ChatColor.YELLOW + "Author(s): " + ChatColor.GOLD + authors);
			return true;
		}
        
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        if (!config.getBoolean("helpcmd", true)) {
            player.sendMessage(ChatColor.RED + config.getString("disabled", "This command is disabled"));
            return true;
        }

        if (args.length == 0) {
            showHelpBoard(player);
            return true;
        }

        String gameMode = args[0].toLowerCase();
        if (!config.contains("help." + gameMode)) {
            player.sendMessage(ChatColor.RED + "No help available for this game mode.");
            return true;
        }

        if (!player.hasPermission("gamemodeshelp.help." + gameMode)) {
            player.sendMessage(ChatColor.RED + "You do not have permission to view this help section.");
            return true;
        }

        int page = 1;
        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid page number.");
                return true;
            }
        }

        if (!config.contains("help." + gameMode + "." + page)) {
            player.sendMessage(ChatColor.RED + config.getString("pageNA", "This page does not exist."));
            return true;
        }

        player.sendMessage(ChatColor.AQUA + "----------------------");
        player.sendMessage(ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() + "--- Help Guide ---");
        player.sendMessage(" "); // Added space
        player.sendMessage(ChatColor.GRAY + "Page " + page + ": " + ChatColor.AQUA + gameMode.substring(0, 1).toUpperCase() + gameMode.substring(1));
        player.sendMessage(" "); // Added space
        player.sendMessage(ChatColor.AQUA + "----------------------");

        List<String> helpMessages = config.getStringList("help." + gameMode + "." + page);
        for (String line : helpMessages) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
        }

        ComponentBuilder navigation = new ComponentBuilder("");
        if (page > 1) {
            TextComponent back = new TextComponent(ChatColor.GREEN + "[<< " + (page - 1) + "] ");
            back.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/help " + gameMode + " " + (page - 1)));
            navigation.append(back);
        }
        
		TextComponent currentPage = new TextComponent(ChatColor.GOLD + " Page " + page + " ");
        currentPage.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "")); // Makes it unclickable
        navigation.append(currentPage);
        
        if (config.contains("help." + gameMode + "." + (page + 1))) {
            TextComponent next = new TextComponent(ChatColor.GREEN + "[" + (page + 1) + " >>]");
            next.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/help " + gameMode + " " + (page + 1)));
            navigation.append(next);
        }
        
        player.spigot().sendMessage(navigation.create());
        
        return true;
    }

    private void showHelpBoard(Player player) {
        player.sendMessage(ChatColor.AQUA + "----------------------");
        player.sendMessage(ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() + "ServerName");
        player.sendMessage(" "); // Space added
        player.sendMessage(ChatColor.GRAY + "Use /help <gamemode> for specific help.");
        player.sendMessage(" "); // Space added
        player.sendMessage(ChatColor.GRAY + "Available game modes:");
        
        Set<String> gameModes = config.getConfigurationSection("help").getKeys(false);
        for (String mode : gameModes) {
            player.sendMessage(ChatColor.GOLD + "- " + mode);
        }
        
        player.sendMessage(ChatColor.AQUA + "----------------------");
    }
}
