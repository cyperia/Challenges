package net.cyperia.challenges.plugin.challenges.custom.settings.action;

import net.cyperia.challenges.plugin.challenges.custom.settings.sub.SubSettingsBuilder;

import java.util.function.Supplier;

public abstract class EntityTargetAction extends ChallengeAction implements IEntityTargetAction {

  public EntityTargetAction(String name, SubSettingsBuilder subSettingsBuilder) {
    super(name, subSettingsBuilder);
  }

  public EntityTargetAction(String name) {
    super(name);
  }

  public EntityTargetAction(String name, Supplier<SubSettingsBuilder> builderSupplier) {
    super(name, builderSupplier);
  }

}
