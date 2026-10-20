package net.cyperia.commons.bukkit.utils.menu.positions;

import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.menu.MenuClickInfo;
import net.cyperia.commons.bukkit.utils.menu.MenuPosition;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SlottedMenuPosition implements MenuPosition {

  protected final Map<Integer, Consumer<? super MenuClickInfo>> actions = new HashMap<>();
  protected boolean emptySound = true;

  @Override
  public void handleClick(@NotNull MenuClickInfo info) {
    Consumer<? super MenuClickInfo> action = actions.get(info.getSlot());
    if (action == null) {
      if (emptySound) SoundSample.CLICK.play(info.getPlayer());
      return;
    }

    action.accept(info);
  }

  @NotNull
  public SlottedMenuPosition setAction(int slot, @NotNull Consumer<? super MenuClickInfo> action) {
    actions.put(slot, action);
    return this;
  }

  @NotNull
  public SlottedMenuPosition setPlayerAction(int slot, @NotNull Consumer<? super Player> action) {
    return setAction(slot, info -> action.accept(info.getPlayer()));
  }

  @NotNull
  public SlottedMenuPosition setAction(int slot, @NotNull Runnable action) {
    return setAction(slot, info -> action.run());
  }

  public SlottedMenuPosition setEmptySound(boolean playSound) {
    this.emptySound = playSound;
    return this;
  }

}
