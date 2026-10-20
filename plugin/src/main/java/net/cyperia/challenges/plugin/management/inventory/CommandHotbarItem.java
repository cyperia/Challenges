package net.cyperia.challenges.plugin.management.inventory;

import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.TranslationManager;
import net.cyperia.challenges.plugin.management.menu.format.ItemFormatter;
import net.cyperia.challenges.plugin.management.menu.format.ItemFormatter.Convention;
import net.cyperia.commons.bukkit.utils.item.ItemBuilder.SkullBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * A built-in hotbar item running a command, named by the {@code hotbar.<id>.name} translation.
 *
 * @param material {@link Material#PLAYER_HEAD} renders the head of the player holding the item
 */
public record CommandHotbarItem(@NotNull String id, @NotNull Material material, @NotNull String command,
                                boolean lobbyOnly, @NotNull Predicate<Player> availability) implements HotbarItem {

  @NotNull
  @Override
  public String getId() {
    return id;
  }

  @Override
  public boolean isAvailable(@NotNull Player player) {
    return availability.test(player);
  }

  @Override
  public boolean isLobbyOnly() {
    return lobbyOnly;
  }

  @NotNull
  @Override
  public ItemStack render(@NotNull Player player) {
    ItemFormatter formatter = material == Material.PLAYER_HEAD
      ? new ItemFormatter(new SkullBuilder().setOwner(player.getUniqueId(), player.getName()))
      : new ItemFormatter(material);
    return formatter
      .naming(Convention.HOTBAR)
      .name(MessageKey.of("hotbar." + id + ".name"))
      .build(TranslationManager.getLocaleOf(player));
  }

  @Override
  public void onUse(@NotNull Player player) {
    player.performCommand(command);
  }

}
