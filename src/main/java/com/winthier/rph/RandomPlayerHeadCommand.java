package com.winthier.rph;

import com.cavetale.core.font.GuiOverlay;
import com.cavetale.mytems.Mytems;
import com.winthier.rph.gui.Gui;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

@RequiredArgsConstructor
public final class RandomPlayerHeadCommand implements CommandExecutor {
    private final RandomPlayerHeadPlugin plugin;

    public RandomPlayerHeadCommand enable() {
        plugin.getCommand("randomplayerhead").setExecutor(this);
        return this;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return false;
        } else if (args[0].equals("-search") && args.length > 1) {
            // Search heads database.
            if (!(sender instanceof Player player)) {
                sender.sendMessage(text("[rph:search] Player expected", RED));
                return true;
            }
            final String term = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            final List<Head> headList = plugin.findHeads(term);
            if (headList.isEmpty()) {
                sender.sendMessage(text("Pattern not found: " + term, RED));
                return true;
            }
            openHeadList(player, headList, 0);
            player.sendMessage(text(headList.size() + " results for " + term, YELLOW));
            return true;
        } else if (args[0].equals("-reload") && args.length == 1) {
            // Reload configuration.
            plugin.loadHeads();
            sender.sendMessage("Loaded " + plugin.getHeads().size() + " heads.");
        } else if (args.length == 1) {
            // Random head for a player.
            if (plugin.getHeads().isEmpty()) {
                sender.sendMessage("No heads loaded");
                return true;
            }
            Player player = plugin.getServer().getPlayerExact(args[0]);
            if (player == null) {
                sender.sendMessage("Player not found: " + args[0]);
                return true;
            }
            Head head = plugin.randomHead();
            if (!head.give(player)) {
                sender.sendMessage(ChatColor.RED + "Inventory full!");
            } else {
                sender.sendMessage("Head spawned in: " + head.getName());
            }
        } else if ((args[0].equals("-all") || args[0].equals("-allm")) && args.length > 1) {
            boolean match = false;
            if (args[0].endsWith("m")) match = true;
            // Give all heads matching name to yourself.
            if (plugin.getHeads().isEmpty()) {
                sender.sendMessage("No heads loaded");
                return true;
            }
            Player player = sender instanceof Player ? (Player) sender : null;
            if (player == null) {
                sender.sendMessage("Player expected.");
                return true;
            }
            StringBuilder sb = new StringBuilder(args[1]);
            for (int i = 2; i < args.length; ++i) sb.append(" ").append(args[i]);
            String name = sb.toString();
            List<Head> headList;
            if (match) {
                headList = plugin.findHeads(name);
            } else {
                headList = plugin.findHeadsExact(name);
            }
            int count = 0;
            for (Head head: headList) {
                if (!head.give(player)) {
                    sender.sendMessage(ChatColor.RED + "Inventory full!");
                    break;
                }
                sender.sendMessage("Gave head \"" + name + "\" to " + player.getName());
                count += 1;
            }
            if (count > 0) {
                sender.sendMessage("" + count + " heads given.");
            }
        } else if (args.length >= 2) {
            // Give one head matching name to a player.
            if (plugin.getHeads().isEmpty()) {
                sender.sendMessage("No heads loaded");
                return true;
            }
            Player player = plugin.getServer().getPlayer(args[0]);
            if (player == null) {
                sender.sendMessage("Player not found: " + args[0]);
                return true;
            }
            StringBuilder sb = new StringBuilder(args[1]);
            for (int i = 2; i < args.length; ++i) sb.append(" ").append(args[i]);
            String name = sb.toString();
            List<Head> headList = plugin.findHeadsExact(name);
            if (headList.isEmpty()) {
                sender.sendMessage("Head not found: " + name);
                return true;
            }
            if (!headList.get(0).give(player)) {
                sender.sendMessage(ChatColor.RED + "Inventory full!");
            } else {
                sender.sendMessage("Gave head \"" + name + "\" to " + player.getName());
            }
        } else {
            return false;
        }
        return true;
    }

    private static void openHeadList(Player player, List<Head> headList, int page) {
        final int size = 6 * 9;
        final int pageSize = 5 * 9;
        final int pageCount = (headList.size() - 1) / pageSize + 1;
        final int listOffset = page * pageSize;
        final Gui gui = new Gui()
            .size(size)
            .title(GuiOverlay.BLANK.builder(size, WHITE)
                   .layer(GuiOverlay.TOP_BAR, WHITE)
                   .title(text("Head List Page " + (page + 1) + "/" + pageCount))
                   .build());
        for (int i = 0; i < pageSize; i += 1) {
            final int listIndex = listOffset + i;
            final int guiIndex = 9 + i;
            if (listIndex >= headList.size()) break;
            Head head = headList.get(listIndex);
            gui.setItem(guiIndex, head.getItem(), click -> {
                    if (!click.isLeftClick()) return;
                    if (player.getInventory().addItem(head.getItem()).isEmpty()) {
                        player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, SoundCategory.MASTER, 1.0f, 1.0f);
                        player.sendMessage(text("Head spawned: " + head.getName(), GREEN));
                    } else {
                        player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, SoundCategory.MASTER, 1.0f, 0.5f);
                        player.sendMessage(text("Inventory full", RED));
                    }
                });
        }
        if (page > 0) {
            gui.setItem(0, Mytems.ARROW_LEFT.createIcon(List.of(text("To page " + page, GRAY))), click -> {
                    if (!click.isLeftClick()) return;
                    player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, SoundCategory.MASTER, 1.0f, 1.0f);
                    openHeadList(player, headList, page - 1);
                });
        }
        if (page < pageCount - 1) {
            gui.setItem(8, Mytems.ARROW_RIGHT.createIcon(List.of(text("To page " + (page + 2), GRAY))), click -> {
                    if (!click.isLeftClick()) return;
                    player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, SoundCategory.MASTER, 1.0f, 1.0f);
                    openHeadList(player, headList, page + 1);
                });
        }
        gui.open(player);
    }
}
