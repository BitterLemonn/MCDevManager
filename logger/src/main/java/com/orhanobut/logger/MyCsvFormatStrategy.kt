package com.orhanobut.logger;

import static com.orhanobut.logger.Utils.checkNotNull;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * CSV formatted file logging for Android.
 * Writes to CSV the following data:
 * epoch timestamp, ISO8601 timestamp (human-readable), log level, tag, log message.
 */
public class MyCsvFormatStrategy implements FormatStrategy {

  private static final String NEW_LINE = System.getProperty("line.separator");
  private static final String NEW_LINE_REPLACEMENT = " <br> ";
  private static final String SEPARATOR = ",";

  private static final char TOP_LEFT_CORNER = '┌';
  private static final String BOTTOM_LEFT_CORNER = "\n└";
  private static final char MIDDLE_CORNER = '├';
  private static final String HORIZONTAL_LINE = "│\n";
  private static final String DOUBLE_DIVIDER = "──────────────────────────\n";
  private static final String SINGLE_DIVIDER = "┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄\n";
  private static final String TOP_BORDER = TOP_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
  private static final String BOTTOM_BORDER = BOTTOM_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
  private static final String MIDDLE_BORDER = MIDDLE_CORNER + SINGLE_DIVIDER + SINGLE_DIVIDER;

  private final int methodCount = 4;

  private final int methodOffset = 0;

  private static final int CHUNK_SIZE = 4000;

  private final boolean showThreadInfo = true;

  @NonNull private final Date date;
  @NonNull private final SimpleDateFormat dateFormat;
  @NonNull private final LogStrategy logStrategy;
  @Nullable private final String tag;

  /**
   * The minimum stack trace index, starts at this class after two native calls.
   */
  private static final int MIN_STACK_OFFSET = 5;

  private MyCsvFormatStrategy(@NonNull Builder builder) {
    checkNotNull(builder);

    date = builder.date;
    dateFormat = builder.dateFormat;
    logStrategy = builder.logStrategy;
    tag = builder.tag;
  }

  @NonNull public static Builder newBuilder() {
    return new Builder();
  }

  @Override public void log(int priority, @Nullable String onceOnlyTag, @NonNull String message) {
    checkNotNull(message);

    String tag = formatTag(onceOnlyTag);

    logTopBorder(priority, tag);
    logHeaderContent(priority, tag, methodCount);

    //get bytes of message with system's default charset (which is UTF-8 for Android)
    byte[] bytes = message.getBytes();
    int length = bytes.length;
    if (length <= CHUNK_SIZE) {
      if (methodCount > 0) {
        logDivider(priority, tag);
      }
      logContent(priority, tag, message);
      logBottomBorder(priority, tag);
      return;
    }
    if (methodCount > 0) {
      logDivider(priority, tag);
    }
    for (int i = 0; i < length; i += CHUNK_SIZE) {
      int count = Math.min(length - i, CHUNK_SIZE);
      //create a new String with system's default charset (which is UTF-8 for Android)
      logContent(priority, tag, new String(bytes, i, count));
    }
    logBottomBorder(priority, tag);
  }


  private void logBottomBorder(int logType, @Nullable String tag) {
    logChunk(logType, tag, BOTTOM_BORDER);
  }


  private void logContent(int logType, @Nullable String tag, @NonNull String chunk) {
    checkNotNull(chunk);

    String[] lines = chunk.split(System.getProperty("line.separator"));
    for (String line : lines) {
      logChunk(logType, tag, HORIZONTAL_LINE + " " + line);
    }
  }

  private void logTopBorder(int logType, @Nullable String tag) {
    logChunk(logType, tag, TOP_BORDER);
  }

  private void logChunk(int priority, @Nullable String tag, @NonNull String chunk) {
    checkNotNull(chunk);

    logStrategy.log(priority, tag, chunk);
  }

  private void logDivider(int logType, @Nullable String tag) {
    logChunk(logType, tag, MIDDLE_BORDER);
  }


  /**
   * Determines the starting index of the stack trace, after method calls made by this class.
   *
   * @param trace the stack trace
   * @return the stack offset
   */
  private int getStackOffset(@NonNull StackTraceElement[] trace) {
    checkNotNull(trace);

    for (int i = MIN_STACK_OFFSET; i < trace.length; i++) {
      StackTraceElement e = trace[i];
      String name = e.getClassName();
      if (!name.equals(LoggerPrinter.class.getName()) && !name.equals(Logger.class.getName())) {
        return --i;
      }
    }
    return -1;
  }

  @SuppressWarnings("StringBufferReplaceableByString")
  private void logHeaderContent(int logType, @Nullable String tag, int methodCount) {
    StackTraceElement[] trace = Thread.currentThread().getStackTrace();
    if (showThreadInfo) {
      logChunk(logType, tag, HORIZONTAL_LINE + " Thread: " + Thread.currentThread().getName());
      logDivider(logType, tag);
    }
    String level = "";

    int stackOffset = getStackOffset(trace) + methodOffset;

    //corresponding method count with the current stack may exceeds the stack trace. Trims the count
    if (methodCount + stackOffset > trace.length) {
      methodCount = trace.length - stackOffset - 1;
    }

    for (int i = methodCount; i > 0; i--) {
      int stackIndex = i + stackOffset;
      if (stackIndex >= trace.length) {
        continue;
      }
      StringBuilder builder = new StringBuilder();
      builder.append(HORIZONTAL_LINE)
              .append(' ')
              .append(level)
              .append(getSimpleClassName(trace[stackIndex].getClassName()))
              .append(".")
              .append(trace[stackIndex].getMethodName())
              .append(" ")
              .append(" (")
              .append(trace[stackIndex].getFileName())
              .append(":")
              .append(trace[stackIndex].getLineNumber())
              .append(")");
      level += "   ";
      logChunk(logType, tag, builder.toString());
    }
  }

  private String getSimpleClassName(@NonNull String name) {
    checkNotNull(name);

    int lastIndex = name.lastIndexOf(".");
    return name.substring(lastIndex + 1);
  }



  @Nullable private String formatTag(@Nullable String tag) {
    if (!Utils.isEmpty(tag) && !Utils.equals(this.tag, tag)) {
      return this.tag + "-" + tag;
    }
    return this.tag;
  }

  public static final class Builder {
    private static final int MAX_BYTES = 5000 * 1024; // 500K averages to a 4000 lines per file

    Date date;
    SimpleDateFormat dateFormat;
    LogStrategy logStrategy;
    String tag = "PRETTY_LOGGER";

    private Builder() {
    }

    @NonNull public Builder date(@Nullable Date val) {
      date = val;
      return this;
    }

    @NonNull public Builder dateFormat(@Nullable SimpleDateFormat val) {
      dateFormat = val;
      return this;
    }

    @NonNull public Builder logStrategy(@Nullable LogStrategy val) {
      logStrategy = val;
      return this;
    }

    @NonNull public Builder tag(@Nullable String tag) {
      this.tag = tag;
      return this;
    }

    @NonNull public MyCsvFormatStrategy build(String fileName) {
      if (date == null) {
        date = new Date();
      }
      if (dateFormat == null) {
        dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS", Locale.UK);
      }
      if (logStrategy == null) {
        String diskPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String folder = diskPath + File.separatorChar + "logger";

        HandlerThread ht = new HandlerThread("AndroidFileLogger." + folder);
        ht.start();
        Handler handler = new DiskLogStrategy.WriteHandler(ht.getLooper(), folder , fileName, MAX_BYTES);
        logStrategy = new DiskLogStrategy(handler);
      }
      return new MyCsvFormatStrategy(this);
    }
  }
}
