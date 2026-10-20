package net.cyperia.commons.bukkit.utils.bstats.chart;

import net.cyperia.commons.bukkit.utils.bstats.JsonObjectBuilder;
import net.cyperia.commons.bukkit.utils.bstats.JsonObjectBuilder.JsonObject;

import java.util.concurrent.Callable;

public class SimplePie extends CustomChart {

  private final Callable<String> callable;

  /**
   * Class constructor.
   *
   * @param chartId  The id of the chart.
   * @param callable The callable which is used to request the chart data.
   */
  public SimplePie(String chartId, Callable<String> callable) {
    super(chartId);
    this.callable = callable;
  }

  @Override
  protected JsonObject getChartData() throws Exception {
    String value = callable.call();
    if (value == null || value.isEmpty()) {
      // Null = skip the chart
      return null;
    }
    return new JsonObjectBuilder().appendField("value", value).build();
  }
}
