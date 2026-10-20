package net.cyperia.commons.bukkit.utils.bstats.chart;

import net.cyperia.commons.bukkit.utils.bstats.JsonObjectBuilder;
import net.cyperia.commons.bukkit.utils.bstats.JsonObjectBuilder.JsonObject;

import java.util.concurrent.Callable;

public class SingleLineChart extends CustomChart {

  private final Callable<Integer> callable;

  /**
   * Class constructor.
   *
   * @param chartId  The id of the chart.
   * @param callable The callable which is used to request the chart data.
   */
  public SingleLineChart(String chartId, Callable<Integer> callable) {
    super(chartId);
    this.callable = callable;
  }

  @Override
  protected JsonObject getChartData() throws Exception {
    int value = callable.call();
    if (value == 0) {
      // Null = skip the chart
      return null;
    }
    return new JsonObjectBuilder().appendField("value", value).build();
  }
}
