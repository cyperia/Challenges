package net.cyperia.challenges.plugin.content.i18n;

import net.cyperia.commons.common.config.document.GsonDocument;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TimerColorsTest {

  @Test
  void buildsAGradientFromHexAndNamedColors() {
    Map<String, String[]> colorizers = TimerColors.colorizers(new GsonDocument("{\"counting-up\": [\"#111111\", \"gold\", \"#222222\"]}"), warning -> {});
    assertArrayEquals(new String[]{"<gradient:#111111:gold:#222222:{1}><b>{0}</b></gradient>"}, colorizers.get("timer.bar.up.*colorize*"));
  }

  @Test
  void ignoresInvalidGradientsWithAWarning() {
    List<String> warnings = new ArrayList<>();
    Map<String, String[]> colorizers = TimerColors.colorizers(new GsonDocument("{\"paused\": [\"nope\", \"#000000\"], \"countdown\": [\"#123456\"]}"), warnings::add);
    assertFalse(colorizers.containsKey("timer.bar.paused.*colorize*"));
    assertFalse(colorizers.containsKey("timer.bar.countdown.*colorize*"));
    assertEquals(2, warnings.size());
  }

  @Test
  void leavesStatesWithoutColorsAlone() {
    assertEquals(Map.of(), TimerColors.colorizers(new GsonDocument("{}"), warning -> {}));
  }

}
