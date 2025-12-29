package com.winthier.rph;

import com.cavetale.core.command.AbstractCommand;
import com.cavetale.core.font.GuiOverlay;
import com.cavetale.mytems.Mytems;
import com.winthier.rph.gui.Gui;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import static com.cavetale.core.util.CamelCase.toCamelCase;
import static com.cavetale.mytems.util.Items.tooltipCustomName;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public final class HeadStoreCommand extends AbstractCommand<RandomPlayerHeadPlugin> {
    protected HeadStoreCommand(final RandomPlayerHeadPlugin plugin) {
        super(plugin, "headstore");
    }

    @Override
    protected void onEnable() {
        rootNode.denyTabCompletion()
            .description("Open the head store")
            .playerCaller(this::open);
    }

    public void open(Player player) {
        final int size = 6 * 9;
        Gui gui = new Gui().size(size);
        GuiOverlay.Builder overlay = GuiOverlay.BLANK.builder(size, GRAY)
            .title(text("Player Heads", BLACK));
        List<String> categories = new ArrayList<>(plugin.categories);
        Collections.sort(categories);
        for (int i = 0; i < categories.size(); i += 1) {
            String category = categories.get(i);
            Map<String, List<Head>> group = plugin.headGroups.get(category);
            Head head = group.values().iterator().next().get(0);
            ItemStack icon = head.getIcon();
            int count = 0;
            for (List<Head> heads : group.values()) {
                count += heads.size();
            }
            icon = tooltipCustomName(icon, List.of(text(toCamelCase(" ", List.of(category.split("-"))) + " (" + count + ")", AQUA)));
            int x = (i % 7) + 1;
            int y = (i / 7) + 2;
            gui.setItem(x, y, icon, click -> {
                    if (!click.isLeftClick()) return;
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                    openCategory(player, category, 0);
                });
        }
        gui.setItem(4, 0, Mytems.MAGNIFYING_GLASS.createIcon(List.of(text("Search...", BLUE))), click -> {
                if (!click.isLeftClick()) return;
                player.playSound(player, Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                new HeadSearchDialog(player).open();
            });
        gui.title(overlay.build());
        gui.open(player);
    }

    public void openCategory(Player player, String category, int page) {
        Map<String, List<Head>> group = plugin.headGroups.get(category);
        assert group != null;
        if (group.size() == 1) {
            String tag = group.keySet().iterator().next();
            openMerchant(player, category, tag, page);
            return;
        }
        final List<String> tags = new ArrayList<>(group.keySet());
        Collections.sort(tags);
        final int size = 4 * 9;
        final int pageSize = 3 * 9;
        final int pageCount = (tags.size() - 1) / pageSize + 1;
        final int pageOffset = page * pageSize;
        Gui gui = new Gui().size(size);
        GuiOverlay.Builder overlay = GuiOverlay.BLANK.builder(size, GRAY)
            .title(text(toCamelCase(" ", List.of(category.split("-")))
                        + (pageCount > 1
                           ? " " + (page + 1) + "/" + pageCount
                           : ""), BLACK))
            .layer(GuiOverlay.TOP_BAR, DARK_GRAY);
        for (int i = 0; i < pageSize; i += 1) {
            final int slotIndex = 9 + i;
            final int tagIndex = pageOffset + i;
            if (tagIndex >= tags.size()) break;
            String tag = tags.get(tagIndex);
            List<Head> heads = group.get(tag);
            Head head = heads.get(0);
            ItemStack icon = head.getIcon();
            icon = tooltipCustomName(icon, List.of(text(tag + " (" + heads.size() + ")", AQUA)));
            gui.setItem(slotIndex, icon, click -> {
                    if (!click.isLeftClick()) return;
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                    openMerchant(player, category, tag, page);
                });
        }
        gui.setItem(Gui.OUTSIDE, null, click -> {
                if (!click.isLeftClick()) return;
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                open(player);
            });
        if (page > 0) {
            gui.setItem(0, Mytems.ARROW_LEFT.createIcon(List.of(text("Previous Page", GRAY))), click -> {
                    if (!click.isLeftClick()) return;
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                    openCategory(player, category, page - 1);
                });
        }
        if (page < pageCount - 1) {
            gui.setItem(8, Mytems.ARROW_RIGHT.createIcon(List.of(text("Next Page", GRAY))), click -> {
                    if (!click.isLeftClick()) return;
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                    openCategory(player, category, page + 1);
                });
        }
        gui.title(overlay.build());
        gui.open(player);
    }

    public void openMerchant(Player player, String category, String tag, int page) {
        Map<String, List<Head>> group = plugin.headGroups.get(category);
        assert group != null;
        List<Head> heads = group.get(tag);
        assert heads != null;
        Gui gui = openMerchantGui(player, heads, text(toCamelCase(" ", List.of(tag.split(" "))) + " (" + heads.size() + ")"));
        gui.setItem(Gui.OUTSIDE, null, click -> {
                if (!click.isLeftClick()) return;
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                if (group.size() == 1) {
                    open(player);
                } else {
                    openCategory(player, category, page);
                }
            });
    }

    public static Gui openMerchantGui(Player player, List<Head> heads, Component title) {
        List<MerchantRecipe> recipes = new ArrayList<>();
        for (Head head : heads) {
            MerchantRecipe recipe = new MerchantRecipe(head.getItem(), 4);
            if (player.getGameMode() == GameMode.CREATIVE) {
                recipe.setIngredients(List.of(new ItemStack(Material.DIAMOND)));
            } else {
                recipe.setIngredients(
                    List.of(
                        Mytems.RUBY.createItemStack(),
                        Mytems.GOLDEN_COIN.createItemStack()
                    )
                );
            }
            recipes.add(recipe);
        }
        Merchant merchant = Bukkit.createMerchant(title);
        merchant.setRecipes(recipes);
        Gui gui = new Gui();
        gui.setEditable(true);
        gui.doNotOpen(player);
        player.openMerchant(merchant, true);
        return gui;
    }
}
