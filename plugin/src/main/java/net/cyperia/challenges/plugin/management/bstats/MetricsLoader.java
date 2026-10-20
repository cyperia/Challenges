package net.cyperia.challenges.plugin.management.bstats;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.content.loader.LanguageLoader;
import net.cyperia.challenges.plugin.utils.misc.MemoryConverter;
import net.cyperia.commons.bukkit.utils.bstats.Metrics;
import net.cyperia.commons.bukkit.utils.bstats.chart.AdvancedPie;
import net.cyperia.commons.bukkit.utils.bstats.chart.SimplePie;
import net.cyperia.commons.bukkit.utils.bstats.chart.SingleLineChart;
import net.cyperia.commons.common.misc.StringUtils;

import java.util.HashMap;

public class MetricsLoader {

  public void start() {
    Challenges plugin = Challenges.getInstance();

    Metrics metrics = new Metrics(plugin, 11494);
    metrics.addCustomChart(new SimplePie("language", () -> Challenges.getInstance().getLoaderRegistry()
      .getFirstLoaderByClass(LanguageLoader.class)
      .map(LanguageLoader::getConfigLanguageTag)
      .orElse("NULL")));
    metrics.addCustomChart(new SimplePie("cloudType", () -> StringUtils.getEnumName(plugin.getCloudSupportManager().getType())));
    metrics.addCustomChart(new SimplePie("databaseType", () -> StringUtils.getEnumName(plugin.getDatabaseManager().getType())));
    metrics.addCustomChart(new SingleLineChart("totalMemory", this::getMemory));
    metrics.addCustomChart(new SingleLineChart("totalCores", () -> Runtime.getRuntime().availableProcessors()));
    metrics.addCustomChart(new AdvancedPie("maxMemory", () -> {
      HashMap<String, Integer> map = new HashMap<>();
      if (Runtime.getRuntime().maxMemory() == Long.MAX_VALUE) return map;
      map.put(String.valueOf(getMemory()), 1);
      return map;
    }));

  }

  private int getMemory() {
    return MemoryConverter.getGB(Runtime.getRuntime().maxMemory());
  }

}
