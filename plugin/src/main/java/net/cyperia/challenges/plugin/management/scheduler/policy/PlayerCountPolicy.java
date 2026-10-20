package net.cyperia.challenges.plugin.management.scheduler.policy;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiPredicate;

public enum PlayerCountPolicy implements IPolicy {

  ALWAYS((online, max) -> true),
  EMPTY((online, max) -> online == 0),
  SOMEONE((online, max) -> online > 0),
  FULL(Integer::equals);

  private final BiPredicate<Integer, Integer> check;

  PlayerCountPolicy(@NotNull BiPredicate<Integer, Integer> check) {
    this.check = check;
  }

  @Override
  public boolean check(@NotNull Object holder) {
    return check.test(Bukkit.getOnlinePlayers().size(), Bukkit.getMaxPlayers());
  }

}
