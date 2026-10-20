package net.cyperia.commons.database;

import net.cyperia.commons.common.misc.ReflectionUtils;
import net.cyperia.commons.common.misc.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public final class SQLColumn {

  public enum Type {
    /**
     * A FIXED length string (can contain letters, numbers, and special characters). The size parameter specifies the column length in characters - can be from 0 to 255. Default is 1
     */
    CHAR,
    /**
     * A VARIABLE length string (can contain letters, numbers, and special characters). The size parameter specifies the maximum column length in characters - can be from 0 to 65535
     */
    VARCHAR,
    /**
     * Equal to {@link #CHAR}, but stores binary byte strings. The size parameter specifies the column length in bytes. Default is 1
     */
    BINARY,
    /**
     * Equal to {@link #VARCHAR}, but stores binary byte strings. The size parameter specifies the maximum column length in bytes.
     */
    VARBINARY,
    /**
     * For BLOBs (Binary Large OBjects). Max length: 255 bytes
     */
    TINYBLOB,
    /**
     * Holds a string with a maximum length of 255 characters
     */
    TINYTEXT,
    /**
     * Holds a string with a maximum length of 65,535 bytes
     */
    TEXT,
    /**
     * For BLOBs (Binary Large OBjects). Holds up to 65,535 bytes of data
     */
    BLOB,
    /**
     * For BLOBs (Binary Large OBjects). Holds up to 65,535 bytes of data
     */
    MEDIUMTEXT,
    /**
     * For BLOBs (Binary Large OBjects). Holds up to 16,777,215 bytes of data
     */
    MEDIUMBLOB,
    /**
     * Holds a string with a maximum length of 4,294,967,295 characters
     */
    LONGTEXT,
    /**
     * For BLOBs (Binary Large OBjects). Holds up to 4,294,967,295 bytes of data
     */
    LONGBLOB,
    /**
     * A string object that can have only one value, chosen from a list of possible values. You can list up to 65535 values in an ENUM list. If a value is inserted that is not in the list, a blank value will be inserted. The values are sorted in the order you enter them
     */
    ENUM,
    /**
     * A string object that can have 0 or more values, chosen from a list of possible values. You can list up to 64 values in a SET list
     */
    SET,
    /**
     * A bit-value type. The number of bits per value is specified in size. The size parameter can hold a value from 1 to 64. The default value for size is 1.
     */
    BIT,
    /**
     * A very small integer. Signed range is from -128 to 127. Unsigned range is from 0 to 255. The size parameter specifies the maximum display width (which is 255)
     */
    TINYINT,
    /**
     * Zero is considered as false, nonzero values are considered as true.
     */
    BOOLEAN,
    /**
     * A small integer. Signed range is from -32768 to 32767. Unsigned range is from 0 to 65535. The size parameter specifies the maximum display width (which is 255)
     */
    SMALLINT,
    /**
     * A medium integer. Signed range is from -8388608 to 8388607. Unsigned range is from 0 to 16777215. The size parameter specifies the maximum display width (which is 255)
     */
    MEDIUMINT,
    /**
     * A medium integer. Signed range is from -2147483648 to 2147483647. Unsigned range is from 0 to 4294967295. The size parameter specifies the maximum display width (which is 255)
     */
    INT,
    /**
     * A large integer. Signed range is from -9223372036854775808 to 9223372036854775807. Unsigned range is from 0 to 18446744073709551615. The size parameter specifies the maximum display width (which is 255)
     */
    BIGINT,
    /**
     * A floating point number. MySQL uses the p value to determine whether to use FLOAT or DOUBLE for the resulting data type. If p is from 0 to 24, the data type becomes FLOAT(). If p is from 25 to 53, the data type becomes DOUBLE()
     */
    FLOAT,
    /**
     * A normal-size floating point number. The total number of digits is specified in size. The number of digits after the decimal point is specified in the d parameter
     */
    DOUBLE,
    /**
     * An exact fixed-point number. The total number of digits is specified in size. The number of digits after the decimal point is specified in the d parameter. The maximum number for size is 65. The maximum number for d is 30. The default value for size is 10. The default value for d is 0.
     */
    DECIMAL
  }

  private final String name;
  private final String type;
  private final String param;

  public SQLColumn(@NotNull String name, @NotNull String type, @Nullable String param) {
    if (name.contains(" ")) throw new IllegalArgumentException("Column name cannot contain spaces");
    if (type.contains(" ")) throw new IllegalArgumentException("Column type cannot contain spaces");

    this.name = name;
    this.type = type;
    this.param = param;
  }

  public SQLColumn(@NotNull String name, @NotNull String type, int size) {
    this(name, type, String.valueOf(size));
  }

  public SQLColumn(@NotNull String name, @NotNull String type, int size, int d) {
    this(name, type, String.valueOf(size), String.valueOf(d));
  }

  public SQLColumn(@NotNull String name, @NotNull String type, @NotNull String... params) {
    this(name, type, StringUtils.getArrayAsString(params, ", "));
  }

  public SQLColumn(@NotNull String name, @NotNull Type type, @Nullable String param) {
    this(name, type.name(), param);
  }

  public SQLColumn(@NotNull String name, @NotNull Type type, int size) {
    this(name, type.name(), size);
  }

  public SQLColumn(@NotNull String name, @NotNull Type type, int size, int d) {
    this(name, type.name(), size, d);
  }

  public SQLColumn(@NotNull String name, @NotNull Type type, @NotNull String... params) {
    this(name, type.name(), params);
  }

  public SQLColumn(@NotNull String name, @NotNull Type type, @NotNull Type... types) {
    this(name, type, Arrays.stream(types).map(Type::name).toArray(String[]::new));
  }

  @NotNull
  public String getName() {
    return name;
  }

  @NotNull
  public String getType() {
    return type;
  }

  @Nullable
  public Type findType() {
    return ReflectionUtils.getEnumOrNull(type.toUpperCase(), Type.class);
  }

  @Nullable
  public String getParam() {
    return param;
  }

  @Override
  public String toString() {
    return name + " " + type + (param == null ? "" : "(" + param + ")");
  }

}
