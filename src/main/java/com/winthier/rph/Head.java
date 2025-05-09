package com.winthier.rph;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import static net.kyori.adventure.text.Component.text;

@Value @RequiredArgsConstructor
public final class Head {
    public final String name;
    public final UUID uuid;
    public final String texture;
    public final String signature;
    public final String category;
    public final Set<String> tags;

    public Head(final String name, final UUID uuid, final String texture, final String signature) {
        this(name, uuid, texture, signature, "unknown", Set.of());
    }

    public boolean give(Player player) {
        return player.getInventory().addItem(getItem()).isEmpty();
    }

    public ItemStack getItem() {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.displayName(text(name));
        PlayerProfile profile = Bukkit.createProfile(uuid);
        ProfileProperty property = new ProfileProperty("textures", texture, signature);
        profile.setProperty(property);
        meta.setPlayerProfile(profile);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        PlayerProfile profile = Bukkit.createProfile(uuid);
        ProfileProperty property = new ProfileProperty("textures", texture, signature);
        profile.setProperty(property);
        meta.setPlayerProfile(profile);
        item.setItemMeta(meta);
        return item;
    }
}
