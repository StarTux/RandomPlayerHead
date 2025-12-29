package com.winthier.rph;

import com.winthier.rph.gui.Gui;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.RegistryBuilderFactory;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.DialogRegistryEntry;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput.MultilineOptions;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

@RequiredArgsConstructor
public final class HeadSearchDialog {
    private final Player player;

    public void open() {
        player.showDialog(Dialog.create(this::dialog));
    }

    private void dialog(RegistryBuilderFactory<Dialog, ? extends DialogRegistryEntry.Builder> factory) {
        factory.empty()
            .base(
                DialogBase.builder(text("Head Search"))
                .inputs(
                    List.of(
                        DialogInput.text(
                            "name", // key
                            text("") // label
                        )
                        .initial("")
                        .labelVisible(false)
                        .maxLength(32)
                        .multiline(MultilineOptions.create(1, null))
                        .build()
                    )
                )
                .build()
            )
            .type(
                DialogType.notice(
                    ActionButton.builder(text("Search"))
                    .action(
                        DialogAction.customClick(
                            this::search,
                            ClickCallback.Options.builder()
                            .lifetime(Duration.ofMinutes(10))
                            .uses(1)
                            .build()
                        )
                    )
                    .build()
                )
            );
    }

    private void search(DialogResponseView response, Audience audience) {
        String name = response.getText("name");
        if (name == null || name.isEmpty() || name.length() > 32) {
            return;
        }
        final List<Head> headList = RandomPlayerHeadPlugin.getInstance().findHeads(name);
        if (headList.isEmpty()) {
            player.playSound(player, Sound.UI_BUTTON_CLICK, 0.5f, 0.5f);
            player.sendMessage(text("No heads found: " + name, RED));
            return;
        }
        while (headList.size() > 32) {
            headList.remove(headList.size() - 1);
        }
        Gui gui = HeadStoreCommand.openMerchantGui(player, headList, text(name));
        gui.setItem(Gui.OUTSIDE, null, click -> {
                if (!click.isLeftClick()) return;
                player.playSound(player, Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                RandomPlayerHeadPlugin.getInstance().getHeadStoreCommand().open(player);
            });
    }
}
