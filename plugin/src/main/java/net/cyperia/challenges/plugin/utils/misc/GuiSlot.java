package net.cyperia.challenges.plugin.utils.misc;

public final class GuiSlot {

  private GuiSlot() {
  }

  public static int at(int row, int col) {
    return row * 9 + col;
  }

}
