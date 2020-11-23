package com.winthier.rph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public final class MakePlayerHeadCommand implements CommandExecutor {
    private final RandomPlayerHeadPlugin plugin;

    public MakePlayerHeadCommand enable() {
        plugin.getCommand("makeplayerhead").setExecutor(this);
        return this;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Player expected");
            return true;
        }
        if (args.length < 3) return false;
        Player player = (Player) sender;
        int index = args.length - 1;
        String texture = args[index--];
        if (index < 1) return false;
        UUID uuid;
        String id = args[index--];
        if (id.matches("-?[0-9]+")) {
            if (index < 3) return false;
            index -= 3;
            List<Integer> ints = new ArrayList<>(4);
            for (int i = 0; i < 3; i += 1) {
                String id2 = args[index + i + 1];
                try {
                    ints.add(Integer.parseInt(id2));
                } catch (NumberFormatException nfe) {
                    player.sendMessage(ChatColor.RED + "Not a number: " + id2);
                    return true;
                }
            }
            try {
                ints.add(Integer.parseInt(id));
            } catch (NumberFormatException nfe) {
                player.sendMessage(ChatColor.RED + "Not a number: " + id);
                return true;
            }
            uuid = RawSkull.intToUuid(ints);
        } else {
            try {
                uuid = UUID.fromString(id);
            } catch (IllegalArgumentException iae) {
                player.sendMessage(ChatColor.RED + "Invalid uuid: " + id);
                return true;
            }
        }
        if (index < 0) return false;
        String name = String.join(" ", Arrays.copyOfRange(args, 0, index + 1));
        Head head = new Head(name, uuid, texture, null);
        head.give(player);
        player.sendMessage(ChatColor.GREEN + "Head given: " + head.getName());
        return true;
    }
}
