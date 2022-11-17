package com.winthier.rph;

import com.cavetale.core.event.player.PlayerInteractNpcEvent;
import com.winthier.rph.gui.Gui;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class RandomPlayerHeadPlugin extends JavaPlugin implements Listener {
    protected final List<Head> heads = new ArrayList<>();
    protected final Map<String, Map<String, List<Head>>> headGroups = new HashMap<>();
    protected final Set<String> categories = new HashSet<>();
    protected final Random random = new Random(System.currentTimeMillis());
    protected final Set<String> textureSet = new HashSet<>();
    private final HeadStoreCommand headStoreCommand = new HeadStoreCommand(this);

    @Override
    public void onEnable() {
        loadHeads();
        new RandomPlayerHeadCommand(this).enable();
        new MakePlayerHeadCommand(this).enable();
        new HeadCommand(this).enable();
        headStoreCommand.enable();
        Gui.enable(this);
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    protected void loadHeads() {
        heads.clear();
        categories.clear();
        textureSet.clear();
        loadHeads(new File(getDataFolder(), "heads"));
        loadHeads(new File("/home/mc/public/heads"));
        if (heads.isEmpty()) {
            getLogger().warning("No heads were loaded!");
        }
    }

    private void loadHeads(File headsFolder) {
        if (!headsFolder.isDirectory()) return;
        try {
            for (File file : headsFolder.listFiles()) {
                if (!file.getName().endsWith(".yml")) continue;
                if (!file.isFile()) continue;
                int count = 0;
                int errors = 0;
                int dupes = 0;
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                for (Map<?, ?> map: config.getMapList("heads")) {
                    ConfigurationSection section = config.createSection("tmp", map);
                    final String texture = section.getString("Texture");
                    if (!textureSet.add(texture)) {
                        dupes += 1;
                        continue;
                    }
                    final String name = section.getString("Name");
                    final String id = section.getString("Id");
                    if (name == null || id == null || texture == null) {
                        getLogger().warning("name=" + name + " id=" + id + " texture=" + texture);
                        errors += 1;
                        continue;
                    }
                    final String signature = section.getString("Signature");
                    final String category = section.getString("Category", "unknown");
                    final Set<String> tags = Set.copyOf(section.getStringList("Tags"));
                    final Head head = new Head(name, UUID.fromString(id), texture, signature, category, tags);
                    final Map<String, List<Head>> group = headGroups.computeIfAbsent(category, c -> new HashMap<>());
                    heads.add(head);
                    if (tags.isEmpty()) {
                        group.computeIfAbsent(category, o -> new ArrayList<>()).add(head);
                    } else {
                        for (String tag : tags) {
                            group.computeIfAbsent(tag, t -> new ArrayList<>()).add(head);
                        }
                    }
                    categories.add(category);
                    count += 1;
                }
                getLogger().info("Loaded " + count + " heads, " + errors + " errors, " + dupes + " dupes from " + file.getPath());
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
        getLogger().info("Loaded " + heads.size() + " heads");
    }

    public Head randomHead() {
        if (heads.isEmpty()) return null;
        return heads.get(random.nextInt(heads.size()));
    }

    public List<Head> findHeads(String term) {
        term = term.toLowerCase();
        List<Head> result = new ArrayList<>();
        for (Head head : heads) {
            if (head.getName().toLowerCase().contains(term)) result.add(head);
        }
        return result;
    }

    public List<Head> findHeadsExact(String term) {
        List<Head> result = new ArrayList<>();
        for (Head head : heads) {
            if (head.getName().equalsIgnoreCase(term)) result.add(head);
        }
        return result;
    }

    @EventHandler
    private void onPlayerInteractNpc(PlayerInteractNpcEvent event) {
        if ("HeadStore".equals(event.getName())) {
            event.setCancelled(true);
            if (event.getPlayer().hasPermission("rph.store.open")) {
                headStoreCommand.open(event.getPlayer());
            }
        }
    }
}
