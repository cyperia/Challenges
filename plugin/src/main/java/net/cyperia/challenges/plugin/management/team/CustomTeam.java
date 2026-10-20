package net.cyperia.challenges.plugin.management.team;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class CustomTeam implements Team, RegisteredTeam {

  private final UUID uniqueId;
  private final String displayName;

  private TeamProvider provider;

  public CustomTeam(String displayName) {
    this(UUID.randomUUID(), displayName);
  }

  @Override
  public void supplyTeamProvider(TeamProvider teamProvider) {
    this.provider = teamProvider;
  }

  @Override
  public List<UUID> getMembers() {
    return provider.getTeamMembers(getUniqueId());
  }

  @Override
  public List<Player> getOnlineMembers() {
    return provider.getOnlineTeamMembers(getUniqueId());
  }
}
