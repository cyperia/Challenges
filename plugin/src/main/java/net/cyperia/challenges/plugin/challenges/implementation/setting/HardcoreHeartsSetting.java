package net.cyperia.challenges.plugin.challenges.implementation.setting;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;

import java.lang.reflect.Field;
import java.util.List;

public class HardcoreHeartsSetting extends Setting {

  private PacketListener packetListener;

  public HardcoreHeartsSetting() {
    super(true);
  }

  @Override
  protected void onEnable() {
    ProtocolManager manager = ProtocolLibrary.getProtocolManager();
    manager.addPacketListener(packetListener = createPacketListener());
  }

  @Override
  protected void onDisable() {
    if (packetListener != null) {
      ProtocolManager manager = ProtocolLibrary.getProtocolManager();
      manager.removePacketListener(packetListener);
    }
  }

  private PacketAdapter createPacketListener() {
    return new PacketAdapter(plugin, ListenerPriority.NORMAL,
      PacketType.Play.Server.LOGIN, PacketType.Play.Server.RESPAWN) {

      @Override
      public void onPacketSending(PacketEvent event) {
        PacketContainer packet = event.getPacket();

        // Dynamically look up the index for safety
        int hardcoreIndex = getHardcoreFieldIndex(packet);

        if (hardcoreIndex != -1) {
          // Found it cleanly by name!
          packet.getBooleans().write(hardcoreIndex, true);
        } else {
          // Fallback default just in case a future mapping changes things
          if (packet.getBooleans().size() > 0) {
            packet.getBooleans().write(0, true);
          }
        }
      }
    };
  }

  /**
   * Finds the ProtocolLib boolean index for the 'hardcore' flag.
   * * @param packet The packet container to scan (e.g., LOGIN or RESPAWN)
   *
   * @return The integer index for .getBooleans(), or -1 if not found.
   */
  public int getHardcoreFieldIndex(PacketContainer packet) {
    // Grab the internal list of fields filtered down to just booleans
    List<FieldAccessor> booleanFields = packet.getBooleans().getFields();

    for (int i = 0; i < booleanFields.size(); i++) {
      Field field = booleanFields.get(i).getField();

      // Match against Mojang official field name
      if (field.getName().equals("hardcore")) {
        return i;
      }
    }

    // Field wasn't found by name
    return -1;
  }
}
