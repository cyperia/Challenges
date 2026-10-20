package net.cyperia.challenges.plugin.content.i18n.impl.format;

import io.papermc.paper.advancement.AdvancementDisplay;
import net.cyperia.challenges.plugin.content.i18n.impl.format.rewrite.ComponentAdvancementNames;
import net.cyperia.challenges.plugin.content.i18n.impl.format.rewrite.ComponentRewriter;
import net.cyperia.challenges.plugin.utils.misc.EntityUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.translation.Translatable;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.function.Supplier;

public final class ComponentArguments {

  private ComponentArguments() {
  }

  @NotNull
  public static Component convertToComponent(@Nullable Object obj) {
    return switch (obj) {
      case null -> Component.empty();
      case Supplier<?> dynamic -> convertToComponent(dynamic.get());
      case Iterable<?> list -> convertIterableToComponent(list);
      case Component component -> component;
      case ComponentLike like -> like.asComponent();
      case Material material -> getDetailedMaterialWithSprite(material);
      case EntityType entityType -> getEntityTypeWithSprite(entityType);
      case LootTable lootTable -> getLootTableName(lootTable);
      case Advancement advancement -> getAdvancementNameWithSprite(advancement);
      case Translatable translatable -> // net.kyori.adventure.translation.Translatable
        Component.translatable(translatable);
      case Player player -> player.displayName();
      case String string -> // ! user input must be escaped via Component.text(...)
        ComponentFormatter.deserializeText(string);
      // values with a server side translation are already resolved into a MessageHolder by
      // MessageKeyImpl#localizeArgs; anything left here has none, so prettify the constant name
      case Enum<?> enumObj -> Component.text(stringifyEnumName(enumObj.name()));
      default -> Component.text(obj.toString());
    };
  }

  @NotNull
  public static Component convertIterableToComponent(@NotNull Iterable<?> objs) {
    TextComponent.Builder builder = Component.text();

    Iterator<?> iterator = objs.iterator();
    while (iterator.hasNext()) {
      builder.append(convertToComponent(iterator.next()));

      if (iterator.hasNext()) {
        // #appendNewLine available since 4.12.0 (not packages for older versions)
        builder.append(Component.newline());
      }
    }

    return builder.asComponent();
  }

  @NotNull
  public static Component getDetailedMaterialWithSprite(@NotNull Material material) {
    return Component.text().append(getItemSpriteOrEmpty(material)).append(getDetailedMaterialTranslatable(material)).asComponent();
  }

  @NotNull
  public static Component getItemSpriteOrEmpty(@Nullable ItemStack item) {
    return formatSpriteOrEmpty(ItemSprites.sprite(item));
  }

  @NotNull
  public static Component getItemSpriteOrEmpty(@NotNull Material item) {
    return formatSpriteOrEmpty(ItemSprites.sprite(item));
  }

  @NotNull
  private static Component formatSpriteOrEmpty(@NotNull Component sprite) {
    if (sprite.equals(Component.empty())) return sprite; // don't append space
    // white keeps a sprite from picking up the surrounding message color; colorIfAbsent
    // so it doesn't also erase the tint ItemSprites puts on greyscale foliage/leather
    return sprite.colorIfAbsent(NamedTextColor.WHITE).appendSpace();
  }

  @NotNull
  public static Component getDetailedMaterialTranslatable(@NotNull Material material) {
    String key = material.translationKey(); // e.g., "item.minecraft.music_disc_strad"
    Component baseComponent = Component.translatable(key);
    String name = material.name();

    if (name.startsWith("MUSIC_DISC_")) {
      String songTranslationKey = key.replace("item.minecraft.music_disc_", "jukebox_song.minecraft.");

      return appendDetailsTranslatable(baseComponent, songTranslationKey);
    }

    // Default behavior for all other non-ambiguous items
    return baseComponent;
  }

  @NotNull
  private static Component appendDetailsTranslatable(@NotNull Component baseComponent, @NotNull String translationKey) {
    return baseComponent
      .append(Component.text(" ("))
      .append(Component.translatable(translationKey))
      .append(Component.text(")"));
  }

  /**
   * Advancements are {@link org.bukkit.Keyed} but not {@link Translatable}; their client side key is
   * derived from the namespaced key, e.g. {@code minecraft:story/mine_stone -> advancements.story.mine_stone.title}.
   * The description is attached as a hover, mirroring how the client renders advancement toasts.
   * <p>
   * Both ways of building the name end up looking the same: no square brackets around it and
   * {@link ComponentRewriter#REWRITE_COLOR} instead of the vanilla green.
   */
  @NotNull
  public static Component getAdvancementName(@NotNull Advancement advancement) {
    return ComponentAdvancementNames.format(advancement.displayName());
  }

  @NotNull
  public static Component getAdvancementNameWithSprite(@NotNull Advancement advancement) {
    Component name = getAdvancementName(advancement);
    AdvancementDisplay display = advancement.getDisplay();
    if (display == null) return name;
    return Component.text().append(getItemSpriteOrEmpty(display.icon()), name).asComponent();
  }

  @NotNull
  public static Component getEntityTypeWithSprite(@NotNull EntityType entityType) {
    Material spawnEgg = EntityUtils.getSpawnEgg(entityType);
    Component name = Component.translatable(entityType.translationKey());
    if (spawnEgg == null) return name;
    return Component.text().append(getItemSpriteOrEmpty(spawnEgg), name).asComponent();
  }

  @NotNull
  public static Component getLootTableName(@NotNull LootTable lootTable) {
    NamespacedKey key = lootTable.getKey();
    String path = key.getKey(); // e.g., "entities/zombie", "chests/simple_dungeon"

    if (path.startsWith("entities/")) {
      // entity loot tables have a matching client-side entity translation
      return Component.translatable("entity." + key.getNamespace() + "." + path.substring("entities/".length()));
    }

    // no client translation keys exist for other loot tables, derive a readable name from the key
    return Component.text(stringifyEnumName(path.substring(path.lastIndexOf('/') + 1)));
  }

  @NotNull
  public static String stringifyEnumName(@NotNull String enumName) {
    // EXAMPLE_ENUM -> "Example Enum"
    int len = enumName.length();
    if (len == 0) return "";

    char[] result = new char[len];
    boolean nextUpperCase = true;
    for (int i = 0; i < len; i++) {
      char letter = enumName.charAt(i);
      if (letter == '_') {
        result[i] = ' ';
        nextUpperCase = true;
      } else {
        result[i] = nextUpperCase ? Character.toUpperCase(letter) : Character.toLowerCase(letter);
        nextUpperCase = false;
      }
    }
    return new String(result);
  }
}
