package com.winthier.rph;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class RandomPlayerHeadPlugin extends JavaPlugin {
    private List<Head> heads = new ArrayList<>();
    private final Random random = new Random(System.currentTimeMillis());

    @Override
    public void onEnable() {
        loadHeads();
        new RandomPlayerHeadCommand(this).enable();
        new MakePlayerHeadCommand(this).enable();
        new HeadCommand(this).enable();
    }

    public void loadHeads() {
        File headsFolder = new File(getDataFolder(), "heads");
        if (!headsFolder.isDirectory()) {
            getLogger().warning("Folder 'heads' not present! No heads were loaded!");
            return;
        }
        Set<Head> headSet = new HashSet<>();
        try {
            for (File file: headsFolder.listFiles()) {
                if (!file.getName().endsWith(".yml")) continue;
                if (!file.isFile()) continue;
                int count = 0;
                int errors = 0;
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                for (Map<?, ?> map: config.getMapList("heads")) {
                    ConfigurationSection section = config.createSection("tmp", map);
                    String name = section.getString("Name");
                    String id = section.getString("Id");
                    String texture = section.getString("Texture");
                    String signature = section.getString("Signature");
                    if (name == null || id == null || texture == null) {
                        getLogger().warning("name=" + name + " id=" + id + " texture=" + texture);
                        errors += 1;
                        continue;
                    }
                    Head head = new Head(name, UUID.fromString(id), texture, signature);
                    if (headSet.add(head)) {
                        count += 1;
                    }
                }
                getLogger().info("Loaded " + count + " heads, " + errors + " errors from " + file.getPath());
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
        heads.clear();
        heads.addAll(headSet);
        getLogger().info("Loaded " + headSet.size() + " heads");
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

}
