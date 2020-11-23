package com.winthier.rph;

import com.destroystokyo.paper.profile.PlayerProfile;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

@RequiredArgsConstructor
public final class HeadCommand implements CommandExecutor {
    private final RandomPlayerHeadPlugin plugin;

    public HeadCommand enable() {
        plugin.getCommand("head").setExecutor(this);
        return this;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Player expected");
            return true;
        }
        if (args.length != 1) return false;
        Player player = (Player) sender;
        String name = args[0];
        PlayerProfile profile = Bukkit.createProfile(name);
        if (profile.completeFromCache(true, true)) {
            callback(player, profile, true);
        } else {
            player.sendMessage(ChatColor.YELLOW + "Fetching profile of " + name + "...");
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    boolean result = profile.complete(true, true);
                    Bukkit.getScheduler().runTask(plugin, () -> callback(player, profile, result));
                });
        }
        return true;
    }

    void callback(Player player, PlayerProfile profile, boolean success) {
        if (!success) {
            player.sendMessage(ChatColor.RED + "Could not fetch profile of " + profile.getName());
            return;
        }
        if (profile.getId() == null) {
            player.sendMessage(ChatColor.RED + "Could not fetch id of " + profile.getName());
            return;
        }
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setPlayerProfile(profile);
        item.setItemMeta(meta);
        player.getInventory().addItem(item);
        player.sendMessage(ChatColor.GREEN + "Player skull given: " + profile.getName());
    }
}
