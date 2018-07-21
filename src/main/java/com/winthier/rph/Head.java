package com.winthier.rph;

import lombok.Value;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Value
final class Head {
    final String name;
    final String id;
    final String texture;

    void give(Player player) {
        String cmd = "minecraft:give %s minecraft:player_head 1 {display:{Name:\"%s\"},SkullOwner:{Id:\"%s\",Name:\"%s\",Properties:{textures:[{Value:\"%s\"}]}}}";
        consoleCommand(cmd, player.getName(), name, id, name, texture);
    }

    void consoleCommand(String cmd, Object... args) {
        if (args.length > 0) cmd = String.format(cmd, args);
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), cmd);
    }
}
