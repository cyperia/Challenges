package net.cyperia.challenges.plugin.management.scheduler;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import org.jetbrains.annotations.NotNull;

@Getter
@EqualsAndHashCode(callSuper = true) // MUST include super.async - see AbstractTaskConfig
public final class ScheduledTaskConfig extends AbstractTaskConfig {

  private final int rate;

  ScheduledTaskConfig(@NotNull ScheduledTask annotation) {
    this(annotation.ticks(), annotation.async());
  }

  ScheduledTaskConfig(int rate, boolean async) {
    super(async);
    this.rate = rate;
  }

}
