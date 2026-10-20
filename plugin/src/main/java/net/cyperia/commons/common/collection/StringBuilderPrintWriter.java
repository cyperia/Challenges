package net.cyperia.commons.common.collection;

import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;

/**
 * @author org.apache.commons.io
 */
public class StringBuilderPrintWriter extends PrintWriter {

  protected final StringBuilderWriter writer;

  public StringBuilderPrintWriter() {
    super(new StringBuilderWriter());
    writer = (StringBuilderWriter) out;
  }

  @NotNull
  public StringBuilder getBuilder() {
    return writer.getBuilder();
  }

  @Override
  public String toString() {
    return getBuilder().toString();
  }

}
