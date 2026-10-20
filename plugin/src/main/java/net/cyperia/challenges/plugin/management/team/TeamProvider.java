package net.cyperia.challenges.plugin.management.team;

import net.cyperia.commons.bukkit.utils.logging.Logger;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TeamProvider {

  private final ConcurrentHashMap<UUID, Team> registeredTeams = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<UUID, UUID> userTeams = new ConcurrentHashMap<>();

  public TeamProvider() {

  }

  public Team getPlayerTeam(Player player) {
    UUID uuid = userTeams.get(player.getUniqueId());
    if (uuid != null) {
      Team team = registeredTeams.get(uuid);
      if (team != null) {
        return team;
      }
    }
    return new PlayerTeam(player);
  }

  public List<Team> getAllTeams() {
    return new LinkedList<>(List.copyOf(registeredTeams.values()));
  }

  public List<UUID> getTeamMembers(UUID teamId) {
    Team team = registeredTeams.get(teamId);
    if (team == null) {
      return new LinkedList<>();
    }
    return team.getMembers();
  }

  public List<Player> getOnlineTeamMembers(UUID teamId) {
    Team team = registeredTeams.get(teamId);
    if (team == null) {
      return new LinkedList<>();
    }
    return team.getOnlineMembers();
  }

  Optional<Team> getRegisteredTeam(UUID uuid) {
    return Optional.ofNullable(registeredTeams.get(uuid));
  }

  public void setTeam(Player player, UUID teamId) {
    Team team = registeredTeams.get(teamId);
    if (!(team instanceof RegisteredTeam)) {
      userTeams.remove(player.getUniqueId());
      return;
    }
    userTeams.put(player.getUniqueId(), teamId);
  }

  public void registerTeam(Team team) {
    if (!(team instanceof RegisteredTeam registeredTeam)) {
      Logger.error("Attempted to register a temporary team as a team!");
      return;
    }
    registeredTeam.supplyTeamProvider(this);
    registeredTeams.put(team.getUniqueId(), team);
  }

  public void unregisterTeam(Team team) {
    if (!(team instanceof RegisteredTeam)) {
      Logger.error("Attempted to unregister a temporary team!");
      return;
    }
    registeredTeams.remove(team.getUniqueId());
  }
}
