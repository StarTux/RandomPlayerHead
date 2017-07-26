package com.winthier.rph;

import com.winthier.custom.util.Dirty;
import lombok.Value;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

@Value
final class Head {
    final String name;
    final String id;
    final String texture;

    void give(Player player) {
        String cmd = "minecraft:give %s minecraft:skull 1 3 {display:{Name:\"%s\"},SkullOwner:{Id:\"%s\",Name:\"%s\",Properties:{textures:[{Value:\"%s\"}]}}}";
        consoleCommand(cmd, player.getName(), name, id, name, texture);
    }

    void consoleCommand(String cmd, Object... args) {
        if (args.length > 0) cmd = String.format(cmd, args);
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), cmd);
    }

    ItemStack toItemStack(){
        return Dirty.setSkullOwner(new ItemStack(Material.SKULL_ITEM), getName(), UUID.fromString(id), getTexture());
    }
}
