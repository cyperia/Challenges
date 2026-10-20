package net.cyperia.challenges.plugin.utils.bukkit.container;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class BukkitSerialization {

  /**
   * Converts the player inventory to a String array of Base64 strings. First string is the content and second string is the armor.
   *
   * @param playerInventory to turn into an array of strings.
   * @return Array of strings: [ main content, armor content ]
   */
  public static String[] playerInventoryToBase64(PlayerInventory playerInventory) throws IllegalStateException {
    //get the main content part, this doesn't return the armor
    String content = toBase64(playerInventory);
    String armor = itemStackArrayToBase64(playerInventory.getArmorContents());

    return new String[]{content, armor};
  }

  /**
   * A method to serialize an {@link ItemStack} array to Base64 String.
   * <p>
   * <p/>
   * <p>
   * Based off of {@link #toBase64(Inventory)}.
   *
   * @param items to turn into a Base64 String.
   * @return Base64 string of the items.
   */
  public static String itemStackArrayToBase64(ItemStack[] items) throws IllegalStateException {
    try {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

      // Write the size of the inventory
      dataOutput.writeInt(items.length);

      // Save every element in the list
      for (int i = 0; i < items.length; i++) {
        dataOutput.writeObject(items[i]);
      }

      // Serialize that array
      dataOutput.close();

      // org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder.encodeLines removed in 26.1
      return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    } catch (Exception e) {
      throw new IllegalStateException("Unable to save item stacks.", e);
    }
  }

  /**
   * A method to serialize an inventory to Base64 string.
   * <p>
   * <p/>
   * <p>
   * Special thanks to Comphenix in the Bukkit forums or also known
   * as aadnk on GitHub.
   *
   * <a href="https://gist.github.com/aadnk/8138186">Original Source</a>
   *
   * @param inventory to serialize
   * @return Base64 string of the provided inventory
   */
  public static String toBase64(Inventory inventory) throws IllegalStateException {
    try {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

      // Save every element in the list
      for (int i = 0; i < inventory.getSize(); i++) {
        dataOutput.writeObject(inventory.getItem(i));
      }

      // Serialize that array
      dataOutput.close();
      // org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder.encodeLines removed in 26.1
      return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    } catch (Exception e) {
      throw new IllegalStateException("Unable to save item stacks.", e);
    }
  }

  /**
   * A method to get an {@link Inventory} from an encoded, Base64, string.
   * <p>
   * <p/>
   * <p>
   * Special thanks to Comphenix in the Bukkit forums or also known
   * as aadnk on GitHub.
   *
   * <a href="https://gist.github.com/aadnk/8138186">Original Source</a>
   *
   * @param data Base64 string of data containing an inventory.
   * @return Inventory created from the Base64 string.
   */
  public static Inventory fromBase64(Inventory inventory, String data) throws IOException {
    try {
      // org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder.decodeLines(data) removed in 26.1
      ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
      BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

      // Read the serialized inventory
      for (int i = 0; i < inventory.getSize(); i++) {
        inventory.setItem(i, (ItemStack) dataInput.readObject());
      }

      dataInput.close();
      return inventory;
    } catch (ClassNotFoundException e) {
      throw new IOException("Unable to decode class type.", e);
    }
  }

  /**
   * Gets an array of ItemStacks from Base64 string.
   * <p>
   * <p/>
   * <p>
   * Base off of .
   *
   * @param data Base64 string to convert to ItemStack array.
   * @return ItemStack array created from the Base64 string.
   */
  public static ItemStack[] itemStackArrayFromBase64(String data) throws IOException {
    try {
      // org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder.decodeLines(data) removed in 26.1
      ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
      BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
      ItemStack[] items = new ItemStack[dataInput.readInt()];

      // Read the serialized inventory
      for (int i = 0; i < items.length; i++) {
        items[i] = (ItemStack) dataInput.readObject();
      }

      dataInput.close();
      return items;
    } catch (ClassNotFoundException e) {
      throw new IOException("Unable to decode class type.", e);
    }
  }

}
