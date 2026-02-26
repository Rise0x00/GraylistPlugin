package ru.rise0x00.graylist;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class GrayList extends JavaPlugin implements TabCompleter {

    @Override
    public void onEnable() {
        getCommand("graylist").setExecutor(this);
        getCommand("graylist").setTabCompleter(this);
        getLogger().info("Плагин GrayList включен!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Плагин GrayList выключен!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("graylist.use")) {
            sender.sendMessage(ChatColor.RED + "У тебя нет прав на использование этой команды!");
            return true;
        }

        // Проверяем, наличие подкоманды
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Использование: /graylist <add|remove|list> <ник>");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("add")) {
            if (args.length != 2) {
                sender.sendMessage(ChatColor.RED + "Использование: /graylist add <ник>");
                return true;
            }

            String playerName = args[1];
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);

            if (offlinePlayer.isWhitelisted()) {
                sender.sendMessage(ChatColor.YELLOW + "Игрок " + ChatColor.GREEN + playerName + ChatColor.YELLOW + " уже в вайтлисте!");
            } else {
                offlinePlayer.setWhitelisted(true);
                Bukkit.reloadWhitelist();
                sender.sendMessage(ChatColor.GREEN + "Игрок " + ChatColor.GREEN + playerName + ChatColor.GREEN + " успешно добавлен в вайтлист с оффлайн UUID!");
            }
            return true;
        }

        if (subCommand.equals("remove")) {
            if (args.length != 2) {
                sender.sendMessage(ChatColor.RED + "Использование: /graylist remove <ник>");
                return true;
            }

            String playerName = args[1];
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);

            if (!offlinePlayer.isWhitelisted()) {
                sender.sendMessage(ChatColor.YELLOW + "Игрок " + ChatColor.GREEN + playerName + ChatColor.YELLOW + " не находится в вайтлисте!");
            } else {
                offlinePlayer.setWhitelisted(false);
                Bukkit.reloadWhitelist();
                sender.sendMessage(ChatColor.GREEN + "Игрок " + ChatColor.GREEN + playerName + ChatColor.GREEN + " успешно удалён из вайтлиста!");
            }
            return true;
        }

        if (subCommand.equals("list")) {
            List<String> whitelisted = new ArrayList<>();
            for (OfflinePlayer player : Bukkit.getWhitelistedPlayers()) {
                whitelisted.add(player.getName() != null ? player.getName() : player.getUniqueId().toString());
            }
            sender.sendMessage(ChatColor.GOLD + "Список игроков в вайтлисте (" + whitelisted.size() + "):");
            sender.sendMessage(ChatColor.WHITE + String.join(", ", whitelisted));
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Неизвестная команда! Используй: /graylist add|remove|list");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        // Подкоманда
        if (args.length == 1) {
            completions.add("add");
            completions.add("remove");
            completions.add("list");

            // Фильтруем по введённому тексту
            String input = args[0].toLowerCase();
            if (!input.isEmpty()) {
                completions.removeIf(s -> !s.toLowerCase().startsWith(input));
            }
        }

        // Ник игрока (для add/remove)
        else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();

            // Для команды "add" показываем всех известных игроков (кроме уже в вайтлисте)
            if (subCommand.equals("add")) {
                for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                    String name = player.getName();
                    if (name != null && !player.isWhitelisted()) {
                        completions.add(name);
                    }
                }
            }
            // Для команды "remove" показываем только игроков из вайтлиста
            else if (subCommand.equals("remove")) {
                for (OfflinePlayer player : Bukkit.getWhitelistedPlayers()) {
                    String name = player.getName();
                    if (name != null) {
                        completions.add(name);
                    }
                }
            }

            // Фильтруем по введённому тексту
            String input = args[1].toLowerCase();
            if (!input.isEmpty()) {
                completions.removeIf(s -> !s.toLowerCase().startsWith(input));
            }
        }
        return completions;
    }
}