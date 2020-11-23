package com.winthier.rph;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public final class RandomPlayerHeadCommand implements CommandExecutor {
    private final RandomPlayerHeadPlugin plugin;

    public RandomPlayerHeadCommand enable() {
        plugin.getCommand("randomplayerhead").setExecutor(this);
        return this;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return false;
        } else if (args[0].equals("-search") && args.length > 1) {
            // Search heads database.
            StringBuilder sb = new StringBuilder(args[1]);
            for (int i = 2; i < args.length; ++i) sb.append(" ").append(args[i]);
            String term = sb.toString();
            List<Head> headList = plugin.findHeads(term);
            if (headList.isEmpty()) {
                sender.sendMessage("Pattern not found: " + term);
                return true;
            } else {
                sb = new StringBuilder("Found " + headList.size() + " heads: ");
                int count = 0;
                for (Head head : headList) {
                    if (count++ > 0) {
                        sb.append(", ");
                    }
                    sb.append(head.getName());
                }
                sender.sendMessage(sb.toString());
            }
        } else if (args[0].equals("-reload") && args.length == 1) {
            // Reload configuration.
            plugin.loadHeads();
            sender.sendMessage("Loaded " + plugin.getHeads().size() + " heads.");
        } else if (args.length == 1) {
            // Random head for a player.
            if (plugin.getHeads().isEmpty()) {
                sender.sendMessage("No heads loaded");
                return true;
            }
            Player player = plugin.getServer().getPlayerExact(args[0]);
            if (player == null) {
                sender.sendMessage("Player not found: " + args[0]);
                return true;
            }
            Head head = plugin.randomHead();
            head.give(player);
            sender.sendMessage("Head spawned in: " + head.getName());
        } else if ((args[0].equals("-all") || args[0].equals("-allm")) && args.length > 1) {
            boolean match = false;
            if (args[0].endsWith("m")) match = true;
            // Give all heads matching name to yourself.
            if (plugin.getHeads().isEmpty()) {
                sender.sendMessage("No heads loaded");
                return true;
            }
            Player player = sender instanceof Player ? (Player) sender : null;
            if (player == null) {
                sender.sendMessage("Player expected.");
                return true;
            }
            StringBuilder sb = new StringBuilder(args[1]);
            for (int i = 2; i < args.length; ++i) sb.append(" ").append(args[i]);
            String name = sb.toString();
            List<Head> headList;
            if (match) {
                headList = plugin.findHeads(name);
            } else {
                headList = plugin.findHeadsExact(name);
            }
            for (Head head: headList) {
                head.give(player);
                sender.sendMessage("Gave head \"" + name + "\" to " + player.getName());
            }
            sender.sendMessage("" + headList.size() + " heads given.");
        } else if (args.length >= 2) {
            // Give one head matching name to a player.
            if (plugin.getHeads().isEmpty()) {
                sender.sendMessage("No heads loaded");
                return true;
            }
            Player player = plugin.getServer().getPlayer(args[0]);
            if (player == null) {
                sender.sendMessage("Player not found: " + args[0]);
                return true;
            }
            StringBuilder sb = new StringBuilder(args[1]);
            for (int i = 2; i < args.length; ++i) sb.append(" ").append(args[i]);
            String name = sb.toString();
            List<Head> headList = plugin.findHeadsExact(name);
            if (headList.isEmpty()) {
                sender.sendMessage("Head not found: " + name);
                return true;
            }
            headList.get(0).give(player);
            sender.sendMessage("Gave head \"" + name + "\" to " + player.getName());
        } else {
            return false;
        }
        return true;
    }
}
