package net.cyperia.challenges.plugin.content.i18n.hook;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public interface LanguageProvider {

  /**
   * Default behavior currently implements one global language set in the plugin.yml.
   * Locale only contains the language tag, no country code or variant.
   */
  @NotNull
  Locale getPlayerLanguage(@NotNull Player player);

  /**
   * Whether this provider uses the default global language set in the plugin.yml.
   * Return {@code false} when implementing custom behavior.
   */
  boolean isDefaultBehaviour();

  boolean isUserSpecific();

}
