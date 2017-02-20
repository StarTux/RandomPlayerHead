package com.winthier.rph;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class RandomPlayerHeadPlugin extends JavaPlugin {
    private List<Head> heads = new ArrayList<>();
    private final Random random = new Random(System.currentTimeMillis());

    @Override
    public void onEnable() {
        loadHeads();
    }

    void loadHeads() {
        File headsFolder = new File(getDataFolder(), "heads");
        if (!headsFolder.isDirectory()) {
            getLogger().warning("Folder 'heads' not present! No heads were loaded!");
            return;
        }
        Set<Head> headSet = new HashSet<>();
        try {
            for (File file: headsFolder.listFiles()) {
                int count = 0;
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                for (Map<?, ?> map: config.getMapList("heads")) {
                    String name = map.get("Name").toString();
                    String id = map.get("Id").toString();
                    String texture = map.get("Texture").toString();
                    Head head = new Head(name, id, texture);
                    if (headSet.add(head)) {
                        count += 1;
                    }
                }
                getLogger().info("Loaded " + count + " heads from " + file.getCanonicalPath());
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
        this.heads.clear();
        this.heads.addAll(headSet);
        getLogger().info("Loaded " + headSet.size() + " heads");
    }

    Head randomHead() {
        if (heads.isEmpty()) return null;
        return heads.get(random.nextInt(heads.size()));
    }

    List<Head> findHeads(String term) {
        term = term.toLowerCase();
        List<Head> result = new ArrayList<>();
        for (Head head : heads) {
            if (head.getName().toLowerCase().contains(term)) result.add(head);
        }
        return result;
    }

    List<Head> findHeadsExact(String term) {
        List<Head> result = new ArrayList<>();
        for (Head head : heads) {
            if (head.getName().equalsIgnoreCase(term)) result.add(head);
        }
        return result;
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
            List<Head> headList = findHeads(term);
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
            loadHeads();
            sender.sendMessage("Loaded " + heads.size() + " heads.");
        } else if (args.length == 1) {
            // Random head for a player.
            if (heads.isEmpty()) {
                sender.sendMessage("No heads loaded");
                return true;
            }
            Player player = getServer().getPlayer(args[0]);
            if (player == null) {
                sender.sendMessage("Player not found: " + args[0]);
                return true;
            }
            Head head = randomHead();
            head.give(player);
            sender.sendMessage("Head spawned in: " + head.getName());
        } else if (args[0].equals("-all") && args.length > 1) {
            // Give all heads matching name to yourself.
            if (heads.isEmpty()) {
                sender.sendMessage("No heads loaded");
                return true;
            }
            Player player = sender instanceof Player ? (Player)sender : null;
            if (player == null) {
                sender.sendMessage("Player expected.");
                return true;
            }
            StringBuilder sb = new StringBuilder(args[1]);
            for (int i = 2; i < args.length; ++i) sb.append(" ").append(args[i]);
            String name = sb.toString();
            List<Head> headList = findHeadsExact(name);
            for (Head head: headList) {
                head.give(player);
                sender.sendMessage("Gave head \"" + name + "\" to " + player.getName());
            }
            sender.sendMessage("" + headList.size() + " heads given.");
        } else if (args.length >= 2) {
            // Give one head matching name to a player.
            if (heads.isEmpty()) {
                sender.sendMessage("No heads loaded");
                return true;
            }
            Player player = getServer().getPlayer(args[0]);
            if (player == null) {
                sender.sendMessage("Player not found: " + args[0]);
                return true;
            }
            StringBuilder sb = new StringBuilder(args[1]);
            for (int i = 2; i < args.length; ++i) sb.append(" ").append(args[i]);
            String name = sb.toString();
            List<Head> headList = findHeadsExact(name);
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
