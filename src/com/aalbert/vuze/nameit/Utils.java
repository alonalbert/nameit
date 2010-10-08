package com.aalbert.vuze.nameit;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by IntelliJ IDEA. User: al Date: Oct 1, 2010 Time: 9:53:13 AM To change this template use
 * File | Settings | File Templates.
 */
public class Utils {
  private static final int IO_BUFFER_SIZE = 4 * 1024;

  /**
   * Copy the content of the input stream into the output stream, using a temporary byte array buffer
   * whose size is defined by {@link #IO_BUFFER_SIZE}.
   *
   * @param in The input stream to copy from.
   * @param out The output stream to copy to.
   * @throws IOException If any error occurs during the copy.
   */
  public static void copy(InputStream in, OutputStream out) throws IOException {
    byte[] b = new byte[IO_BUFFER_SIZE];
    int read;
    while ((read = in.read(b)) != -1) {
      out.write(b, 0, read);
    }
  }

  public static void close(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (IOException e) {
        // ignore
      }
    }
  }
}
