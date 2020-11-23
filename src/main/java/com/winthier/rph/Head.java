package com.winthier.rph;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import java.util.UUID;
import lombok.Value;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

@Value
final class Head {
    public final String name;
    public final UUID uuid;
    public final String texture;
    public final String signature;

    void give(Player player) {
        player.getInventory().addItem(getItem());
    }

    public ItemStack getItem() {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setDisplayNameComponent(new BaseComponent[] {new TextComponent(name)});
        PlayerProfile profile = Bukkit.createProfile(uuid);
        ProfileProperty property = new ProfileProperty("textures", texture, signature);
        profile.setProperty(property);
        meta.setPlayerProfile(profile);
        item.setItemMeta(meta);
        return item;
    }
}
