package net.cyperia.commons.bukkit.utils.bstats.chart;

import net.cyperia.commons.bukkit.utils.bstats.JsonObjectBuilder;
import net.cyperia.commons.bukkit.utils.bstats.JsonObjectBuilder.JsonObject;

import java.util.function.BiConsumer;

public abstract class CustomChart {

  private final String chartId;

  protected CustomChart(String chartId) {
    if (chartId == null) {
      throw new IllegalArgumentException("chartId must not be null");
    }
    this.chartId = chartId;
  }

  public JsonObject getRequestJsonObject(
    BiConsumer<String, Throwable> errorLogger, boolean logErrors) {
    JsonObjectBuilder builder = new JsonObjectBuilder();
    builder.appendField("chartId", chartId);
    try {
      JsonObject data = getChartData();
      if (data == null) {
        // If the data is null we don't send the chart.
        return null;
      }
      builder.appendField("data", data);
    } catch (Throwable t) {
      if (logErrors) {
        errorLogger.accept("Failed to get data for custom chart with id " + chartId, t);
      }
      return null;
    }
    return builder.build();
  }

  protected abstract JsonObject getChartData() throws Exception;
}
