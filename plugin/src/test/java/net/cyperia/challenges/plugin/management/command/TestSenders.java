package net.cyperia.challenges.plugin.management.command;

import org.bukkit.command.CommandSender;

import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class TestSenders {

  private TestSenders() {
  }

  public static CommandSender senderWith(String... permissions) {
    Set<String> granted = Set.of(permissions);
    CommandSender sender = mock(CommandSender.class);
    when(sender.hasPermission(anyString())).thenAnswer(invocation -> granted.contains(invocation.<String>getArgument(0)));
    return sender;
  }

}
