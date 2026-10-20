package net.cyperia.challenges.plugin.management.menu.format;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.ReleaseInfo;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.management.menu.MenuManager;
import net.cyperia.challenges.plugin.management.menu.generator.VersionedContentContainer;
import net.cyperia.commons.bukkit.utils.item.ItemBuilder;
import net.cyperia.commons.bukkit.utils.menu.MenuClickType;
import net.cyperia.commons.common.version.Version;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Assembles a menu item out of the parts every menu item is made of, so that the surrounding formatting
 * ({@code menu.generic.format.*}) is defined once instead of at every call site.
 */
@Getter
public class ItemFormatter {

  @Getter
  @RequiredArgsConstructor
  public enum Convention {
    MENU("name"),
    HOTBAR("name_hotbar");

    private final String nameKey;
  }

  private ItemStack preset;
  private Convention convention = Convention.MENU;
  private LocalizableMessage name;
  private List<LocalizableMessage> suffix;
  private LocalizableMessage desc;
  private List<LocalizableMessage> state;
  private InteractionTooltips tooltips;

  public ItemFormatter() {
    this(Material.BARRIER);
  }

  public ItemFormatter(@NotNull ItemStack preset) {
    this.preset = preset;
  }

  public ItemFormatter(@NotNull ItemBuilder preset) {
    this.preset = preset.build();
  }

  public ItemFormatter(@NotNull Material item) {
    this(new ItemStack(item));
  }

  @NotNull
  public ItemBuilder builder(@NotNull Locale locale) {
    if (preset == null) {
      preset = new ItemStack(Material.STRUCTURE_VOID);
    }

    ItemBuilder item = new ItemBuilder(preset).hideAttributes();

    Component name = createName(locale);
    if (name != null) item.setName(name);

    // appended, not set: a preset may be a real item whose own lore has to survive
    item.appendLore(createLore(locale));

    return item;
  }

  @NotNull
  public List<Component> buildLines(@NotNull Locale locale) {
    Component name = createName(locale);
    List<Component> lore = createLore(locale);
    if (name == null) return lore;

    List<Component> lines = new ArrayList<>(lore.size() + 1);
    lines.add(name);
    lines.addAll(lore);
    return lines;
  }

  @Nullable
  protected Component createName(@NotNull Locale locale) {
    Component result = name == null ? null
      : MessageKey.of("menu.generic.format." + convention.getNameKey()).asComponent(locale, name);
    if (suffix == null) return result;

    Component rendered = MessageKey.of("menu.generic.format.suffix").asComponent(locale, LocalizableMessage.join(
      MessageKey.of("menu.generic.format.suffix_delimiter"), null, suffix.size(), suffix.toArray()
    ));
    return result == null ? rendered : result.append(rendered);
  }

  @NotNull
  protected List<Component> createLore(@NotNull Locale locale) {
    boolean hasTooltips = tooltips != null && !tooltips.isEmpty();
    if (desc == null && state == null && !hasTooltips) return List.of();

    List<Component> result = new ArrayList<>(6); // every block renders as a blank separator line plus its content

    if (desc != null) {
      result.addAll(MessageKey.of("menu.generic.format.desc").asComponents(locale, desc));
    }
    if (state != null) {
      result.addAll(MessageKey.of("menu.generic.format.state").asComponents(locale, state));
    }
    if (hasTooltips) {
      result.addAll(MessageKey.of("menu.generic.format.tooltip").asComponents(locale, tooltips.render(locale)));
    }

    return result;
  }

  @NotNull
  public ItemStack build(@NotNull Locale locale) {
    return builder(locale).build();
  }

  @NotNull
  public ItemFormatter naming(@NotNull ItemFormatter.Convention convention) {
    this.convention = convention;
    return this;
  }

  @NotNull
  public ItemFormatter item(@NotNull ItemStack preset) {
    this.preset = preset;
    return this;
  }

  @NotNull
  public ItemFormatter item(@NotNull ItemBuilder preset) {
    this.preset = preset.build();
    return this;
  }

  @NotNull
  public ItemFormatter name(@Nullable LocalizableMessage name) {
    this.name = name;
    return this;
  }

  @NotNull
  public ItemFormatter suffix(@NotNull LocalizableMessage suffix) {
    if (this.suffix == null) this.suffix = new ArrayList<>(2);
    this.suffix.add(suffix);
    return this;
  }

  @NotNull
  public ItemFormatter suffixIf(boolean condition, @NotNull LocalizableMessage suffix) {
    if (condition) suffix(suffix);
    return this;
  }

  @NotNull
  public ItemFormatter suffixIf(boolean condition, @NotNull String messageKey, @NotNull Object... args) {
    if (condition) suffix(MessageKey.of(messageKey).withArgs(args));
    return this;
  }

  @NotNull
  public ItemFormatter suffixIfNewUpdated(@NotNull Object annotationHolder) {
    if (annotationHolder instanceof VersionedContentContainer container) {
      int betaCount = container.getBetaCount();
      int updatedCount = container.getUpdatedCount();
      int newCount = container.getNewCount();
      if (betaCount > 0 || updatedCount > 0 || newCount > 0) {
        return suffixIfNewUpdated(betaCount, newCount, updatedCount);
      }
    }

    if (ReleaseInfo.isBeta(annotationHolder)) {
      return suffix(MessageKey.of("suffix.beta"));
    }
    MenuManager manager = Challenges.getInstance().getMenuManager();
    Version since = ReleaseInfo.getSince(annotationHolder);
    if (ReleaseInfo.isVersionNew(since) && manager.isNewSuffix()) {
      suffix(MessageKey.of("suffix.new").withArgs(since));
    }
    Version updated = ReleaseInfo.getUpdated(annotationHolder);
    if (ReleaseInfo.isVersionNew(updated) && manager.isUpdatedSuffix()) {
      suffix(MessageKey.of("suffix.updated").withArgs(updated));
    }
    return this;
  }

  @NotNull
  public ItemFormatter suffixIfNewUpdated(int betaCount, int newCount, int updatedCount) {
    MenuManager manager = Challenges.getInstance().getMenuManager();
    if (betaCount > 0) {
      suffix(MessageKey.of("suffix.beta-count").withArgs(betaCount));
    }
    if (manager.isNewSuffix() && newCount > 0) {
      suffix(MessageKey.of("suffix.new-count").withArgs(newCount));
    }
    if (manager.isUpdatedSuffix() && updatedCount > 0) {
      suffix(MessageKey.of("suffix.updated-count").withArgs(updatedCount));
    }
    return this;
  }

  @NotNull
  public ItemFormatter description(@Nullable LocalizableMessage desc) {
    this.desc = desc;
    return this;
  }

  @NotNull
  public ItemFormatter states(@NotNull Collection<? extends LocalizableMessage> states) {
    if (this.state == null) this.state = new ArrayList<>(states.size());
    this.state.addAll(states);
    return this;
  }

  @NotNull
  public ItemFormatter state(@NotNull LocalizableMessage state) {
    if (this.state == null) this.state = new ArrayList<>(2);
    this.state.add(state);
    return this;
  }

  @NotNull
  public ItemFormatter stateIf(boolean condition, @NotNull LocalizableMessage state) {
    if (condition) state(state);
    return this;
  }

  @NotNull
  public ItemFormatter tooltips(@Nullable InteractionTooltips tooltips) {
    this.tooltips = tooltips;
    return this;
  }

  @NotNull
  public ItemFormatter tooltip(@NotNull MenuClickType type, @NotNull LocalizableMessage description) {
    if (this.tooltips == null) this.tooltips = new InteractionTooltips();
    this.tooltips.on(type, description);
    return this;
  }

  @NotNull
  public ItemFormatter tooltipIf(boolean condition, @NotNull MenuClickType type, @NotNull LocalizableMessage description) {
    if (condition) tooltip(type, description);
    return this;
  }

  @NotNull
  public ItemFormatter tooltipIf(boolean condition, @NotNull MenuClickType type, @NotNull String descriptionKey) {
    if (condition) tooltip(type, MessageKey.of(descriptionKey));
    return this;
  }

}
