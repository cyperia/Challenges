package net.cyperia.challenges.plugin.content.i18n;

import net.cyperia.challenges.catalog.MessageFormatter;
import net.cyperia.commons.common.config.Document;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The gradients of the timer in the action bar, which the config may set. The action bar around the time stays as it is.
 */
final class TimerColors {

  // config key -> timer bar state
  private static final Map<String, String> STATES = Map.of(
    "counting-up", "up",
    "counting-down", "down",
    "paused", "paused",
    "countdown", "countdown"
  );

  private TimerColors() {
  }

  @NotNull
  static Map<String, String[]> colorizers(@NotNull Document colors, @NotNull Consumer<String> warn) {
    Map<String, String[]> colorizers = new HashMap<>();
    STATES.forEach((setting, state) -> {
      List<String> gradient = colors.getStringList(setting);
      if (gradient.isEmpty()) return;
      if (gradient.size() < 2 || !gradient.stream().allMatch(TimerColors::isColor)) {
        warn.accept("Ignoring timer-colors." + setting + " " + gradient + ": a gradient needs two or more colors, as hex (#9B8CFF) or names (gold)");
        return;
      }
      // {1} is the phase that animates the gradient
      colorizers.put("timer.bar." + state + "." + MessageFormatter.COLORIZE_KEY,
        new String[]{"<gradient:" + String.join(":", gradient) + ":{1}><b>{0}</b></gradient>"});
    });
    return colorizers;
  }

  private static boolean isColor(@NotNull String color) {
    return TextColor.fromHexString(color) != null || NamedTextColor.NAMES.value(color) != null;
  }

}
