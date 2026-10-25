package net.cyperia.challenges.plugin.management.menu.generator;

import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

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
  public Inventory createInventory(@NotNull Player viewer, int page) {
    throw new UnsupportedOperationException("NoOpMenuGenerator does not support inventory generation");
  }

  @Override
  public void renderPage(@NotNull Inventory inventory, int page, @NotNull Player viewer) {
    // noop
  }

  @Override
  public int getInventorySize() {
    return 0;
  }

  @Override
  public int getPageCount(@NotNull Player viewer) {
    return 0;
  }

}
