package net.cyperia.challenges.plugin.spigot.listener;

import net.cyperia.challenges.plugin.content.i18n.impl.format.ComponentFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HelpListenerTest {

  @Test
  void aLinkOpensHttpsAndNamesTheAddressInItsHover() {
    Component link = ComponentFormatter.MINI_MESSAGE.deserialize(HelpListener.link(true, "domiirl.svrt.dev", "DomiIRL"));
    Component text = link.children().isEmpty() ? link : link.children().get(0);

    assertEquals("DomiIRL", ComponentFormatter.serializePlainText(link));
    assertEquals(ClickEvent.openUrl("https://domiirl.svrt.dev"), text.clickEvent());
    HoverEvent<?> hover = text.hoverEvent();
    assertEquals("Links-Klick ┃ Öffne domiirl.svrt.dev", ComponentFormatter.serializePlainText((Component) hover.value()));
  }

}
