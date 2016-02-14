package com.winthier.rph;

import java.io.BufferedReader;
import java.io.File;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONValue;

public class RandomPlayerHeadPlugin extends JavaPlugin
{
    List<Head> heads = new ArrayList<>();
    final Random random = new Random(System.currentTimeMillis());
    
    @Override
    public void onEnable()
    {
        loadHeads();
    }

    void loadHeads()
    {
        File headsFolder = new File(getDataFolder(), "heads");
        if (!headsFolder.isDirectory()) {
            getLogger().warning("Folder 'heads' not present! No heads were loaded!");
            return;
        }
        Set<Head> heads = new HashSet<>();
        try {
            for (File file: headsFolder.listFiles()) {
                int count = 0;
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                for (Map<?, ?> map: config.getMapList("heads")) {
                    String name = map.get("Name").toString();
                    String id = map.get("Id").toString();
                    String texture = map.get("Texture").toString();
                    Head head = new Head(name, id, texture);
                    if (heads.add(head)) {
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
        this.heads.addAll(heads);
        getLogger().info("Loaded " + heads.size() + " heads");
    }

    Head randomHead()
    {
        if (heads.isEmpty()) return null;
        return heads.get(random.nextInt(heads.size()));
    }

    List<Head> findHeads(String term)
    {
        term = term.toLowerCase();
        List<Head> result = new ArrayList<>();
        for (Head head : heads) {
            if (head.getName().toLowerCase().contains(term)) result.add(head);
        }
        return result;
    }

    List<Head> findHeadsExact(String term)
    {
        List<Head> result = new ArrayList<>();
        for (Head head : heads) {
            if (head.getName().equalsIgnoreCase(term)) result.add(head);
        }
        return result;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (args.length == 0) {
            return false;
        } else if (args[0].equals("-search") && args.length > 1) {
            StringBuilder sb = new StringBuilder(args[1]);
            for (int i = 2; i < args.length; ++i) sb.append(" ").append(args[i]);
            String term = sb.toString();
            List<Head> heads = findHeads(term);
            if (heads.isEmpty()) {
                sender.sendMessage("Pattern not found: " + term);
                return true;
            } else {
                sb = new StringBuilder("Found " + heads.size() + " heads: ");
                int count = 0;
                for (Head head : heads) {
                    if (count++ > 0) {
                        sb.append(", ");
                    }
                    sb.append(head.getName());
                }
                sender.sendMessage(sb.toString());
            }
        } else if (args[0].equals("-reload") && args.length == 1) {
            loadHeads();
            sender.sendMessage("Loaded " + heads.size() + " heads.");
        } else if (args.length == 1) {
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
        } else if (args.length >= 2) {
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
            List<Head> heads = findHeadsExact(name);
            for (Head head: heads) {
                head.give(player);
                sender.sendMessage("Gave head \"" + name + "\" to " + player.getName());
            }
            sender.sendMessage("" + heads.size() + " heads given.");
        } else {
            return false;
        }
        return true;
    }
}                             
