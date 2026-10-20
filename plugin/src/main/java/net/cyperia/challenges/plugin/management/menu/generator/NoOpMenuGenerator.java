package net.cyperia.challenges.plugin.management.menu.generator;

import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class NoOpMenuGenerator extends AbstractMenuGenerator {

  public void playDenyEffect(@NotNull Player player) {
    SoundSample.BASS_OFF.play(player);
  }

  @Override
  public void openMenu(@NotNull Player player, int page) {
    playDenyEffect(player);
  }

  @NotNull
  @Override
  public GeneratorMenuPosition createMenuPosition(int page, @NotNull Player player) {
    throw new UnsupportedOperationException("NoOpMenuGenerator does not support menu position creation");
  }

  @NotNull
  @Override
  public Inventory getOrInitInventory(@NotNull Locale locale, int page) {
    throw new UnsupportedOperationException("NoOpMenuGenerator does not support inventory generation");
  }

  @Override
  public int getInventorySize() {
    return 0;
  }

  @Override
  public void updateOrGeneratePages(@NotNull Locale locale) {
    // noop
  }

  @Override
  public void updatePages() {
    // noop
  }

  @Override
  public void updatePage(int page) {
    // noop
  }

  @Override
  public int getPageCount() {
    return 0;
  }

}
