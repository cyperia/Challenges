package net.cyperia.commons.bukkit.utils.menu.positions;

import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.menu.MenuClickInfo;
import net.cyperia.commons.bukkit.utils.menu.MenuPosition;
import org.jetbrains.annotations.NotNull;

public class EmptyMenuPosition implements MenuPosition {

  @Override
  public void handleClick(@NotNull MenuClickInfo info) {
    SoundSample.CLICK.play(info.getPlayer());
  }

}
