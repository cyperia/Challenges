package net.cyperia.challenges.plugin.management.scheduler;

import net.cyperia.challenges.plugin.management.scheduler.policy.IPolicy;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.cyperia.challenges.plugin.management.scheduler.task.TimerTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PoliciesContainer {

  private final List<IPolicy> policies = new ArrayList<>();

  public PoliciesContainer(@NotNull ScheduledTask annotation) {
    addPolicies(
      annotation.challengePolicy(),
      annotation.timerPolicy(),
      annotation.playerPolicy(),
      annotation.worldPolicy(),
      annotation.freshnessPolicy()
    );
  }

  public PoliciesContainer(@NotNull TimerTask annotation) {
    addPolicies(
      annotation.challengePolicy(),
      annotation.playerPolicy(),
      annotation.worldPolicy(),
      annotation.freshnessPolicy()
    );
  }

  private void addPolicies(@NotNull IPolicy... policies) {
    this.policies.addAll(Arrays.asList(policies));
  }

  public boolean allPoliciesAreTrue(@NotNull Object holder) {
    for (IPolicy policy : policies) {
      if (!policy.isApplicable(holder)) continue;
      if (!policy.check(holder))
        return false;
    }
    return true;
  }

}
