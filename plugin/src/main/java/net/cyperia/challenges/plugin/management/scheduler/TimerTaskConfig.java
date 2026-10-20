package net.cyperia.challenges.plugin.management.scheduler;

import lombok.EqualsAndHashCode;
import net.cyperia.challenges.plugin.management.scheduler.task.TimerTask;
import net.cyperia.challenges.plugin.management.scheduler.timer.TimerStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@EqualsAndHashCode(callSuper = true) // MUST include super.async - see AbstractTaskConfig
public final class TimerTaskConfig extends AbstractTaskConfig {

  private final TimerStatus[] status;

  TimerTaskConfig(@NotNull TimerTask annotation) {
    this(annotation.status(), annotation.async());
  }

  TimerTaskConfig(@NotNull TimerStatus[] status, boolean async) {
    super(async);
    this.status = status;
  }

  @NotNull
  public TimerStatus[] getStatus() {
    return status;
  }

  public boolean acceptsStatus(@NotNull TimerStatus status) {
    return Arrays.asList(this.status).contains(status);
  }

}
