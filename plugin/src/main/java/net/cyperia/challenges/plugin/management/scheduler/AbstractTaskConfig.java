package net.cyperia.challenges.plugin.management.scheduler;

import lombok.EqualsAndHashCode;
import lombok.Getter;

// EqualsAndHashCode over `async` is intentional and load-bearing: subclass configs are used as
// map keys to look up the shared task executor. If two configs at the same rate but different
// thread affinity (sync vs async) compared equal, a sync task could be dispatched onto the async
// executor (or vice versa). See ScheduledTaskConfig#callSuper.
@Getter
@EqualsAndHashCode
public abstract class AbstractTaskConfig {

  protected final boolean async;

  public AbstractTaskConfig(boolean async) {
    this.async = async;
  }

}
