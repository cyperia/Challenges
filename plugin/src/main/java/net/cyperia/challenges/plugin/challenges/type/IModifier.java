package net.cyperia.challenges.plugin.challenges.type;

import org.jetbrains.annotations.NotNull;

public interface IModifier {

  int getValue();

  void setValue(int value);

  int getMinValue();

  int getMaxValue();

  void playValueChangeTitle();

  /**
   * What {@link #getValue()} stands for. Drives the item name, the lore and the click tooltips at once.
   */
  @NotNull
  default ValueUnit getValueUnit() {
    return ValueUnit.VALUE;
  }

}
