package net.cyperia.challenges.plugin.management.team;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class PlayerTeam implements Team {

  private final Player player;

  @Override
  public UUID getUniqueId() {
    return player.getUniqueId();
  }

  @Override
  public String getDisplayName() {
    return player.getDisplayName();
  }

  @Override
  public List<UUID> getMembers() {
    return new LinkedList<>(List.of(player.getUniqueId()));
  }

  @Override
  public List<Player> getOnlineMembers() {
    return new LinkedList<>(List.of(player));
  }
}
