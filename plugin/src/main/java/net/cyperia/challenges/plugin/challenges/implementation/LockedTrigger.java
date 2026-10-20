package net.cyperia.challenges.plugin.challenges.implementation;

import net.cyperia.challenges.plugin.challenges.custom.settings.trigger.ChallengeTrigger;
import org.jetbrains.annotations.NotNull;

// Never fires: it only stands in the editor's list
public class LockedTrigger extends ChallengeTrigger implements LockedEntry {

  public LockedTrigger(@NotNull String name) {
    super(name);
  }

}
