package net.cyperia.challenges.plugin.management.cloud;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;

class CloudSupportManagerTest {

  private static class CountingSupport implements CloudSupport {
    int roundsOver;

    @Override public void setIngame() { }
    @Override public void setLobby() { }
    @Override public void startNewService() { }

    @Override
    public void setRoundOver() {
      roundsOver++;
    }
  }

  @Test
  void theSupportHearsThatTheRoundIsOver() {
    CountingSupport support = new CountingSupport();

    CloudSupportManager.roundOver(support);

    assertEquals(1, support.roundsOver);
  }

  @Test
  void withoutASupportNothingHappens() {
    assertDoesNotThrow(() -> CloudSupportManager.roundOver(null));
  }

  @Test
  void aSupportThatTakesTheResetEndsIt() {
    CloudSupport support = new CountingSupport() {
      @Override
      public boolean handOverReset(CommandSender requestedBy) {
        return true;
      }
    };

    assertTrue(CloudSupportManager.handOverReset(support, null));
  }

  @Test
  void aMissingOrUnwillingSupportLeavesTheResetHere() {
    assertFalse(CloudSupportManager.handOverReset(new CountingSupport(), null));
    assertFalse(CloudSupportManager.handOverReset(null, null));
  }
}
