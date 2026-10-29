package net.cyperia.challenges.plugin.challenges.implementation.goal;

import net.cyperia.challenges.plugin.challenges.type.abstraction.FindItemGoal;
import org.bukkit.Material;

public class FindTotemGoal extends FindItemGoal {

  public FindTotemGoal() {
    super(Material.TOTEM_OF_UNDYING);
  }

}
