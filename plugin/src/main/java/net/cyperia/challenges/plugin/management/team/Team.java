package net.cyperia.challenges.plugin.management.team;

import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public interface Team {

  UUID getUniqueId();

  /**
   * Todo: replace with components
   */
  String getDisplayName();

  List<UUID> getMembers();

  List<Player> getOnlineMembers();

}
