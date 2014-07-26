package android.util;

import java.util.Arrays;

/** Mocking Android Log class for testing. */
public class Log {

    public static int v(String tag, String msg) { return log(tag, msg, null); }

    public static int v(String tag, String msg, java.lang.Throwable tr) { return log(tag, msg, tr); }

    public static int d(String tag, String msg) { return log(tag, msg, null); }

    public static int d(String tag, String msg, java.lang.Throwable tr) { return log(tag, msg, tr); }

    public static int i(String tag, String msg) { return log(tag, msg, null); }

    public static int i(String tag, String msg, java.lang.Throwable tr) { return log(tag, msg, tr); }

    public static int w(String tag, String msg) { return log(tag, msg, null); }

    public static int w(String tag, String msg, java.lang.Throwable tr) { return log(tag, msg, tr); }

    public static boolean isLoggable(String s, int i) { return true; }

    public static int w(String tag, java.lang.Throwable tr) { return log(tag, null, tr); }

    public static int e(String tag, String msg) { return err(tag, msg, null); }

    public static int e(String tag, String msg, java.lang.Throwable tr) { return err(tag, msg, tr); }

    public static int wtf(String tag, String msg) { return err(tag, msg, null); }

    public static int wtf(String tag, java.lang.Throwable tr) { return err(tag, null, tr); }

    public static int wtf(String tag, String msg, java.lang.Throwable tr) { return err(tag, msg, tr); }

    public static String getStackTraceString(java.lang.Throwable tr) { return Arrays.toString(tr.getStackTrace()); }

    public static int println(int priority, String tag, String msg) { return log(tag, msg, null); }

    private static int log(String tag, String msg, Throwable t) {
        if (tag != null) System.out.print("(" + tag + ") ");
        if (msg != null) System.out.print(msg);
        if (tag != null || msg != null) System.out.println();
        if (t != null) t.printStackTrace(System.out);
        return 0;
    }

    private static int err(String tag, String msg, Throwable t) {
        if (tag != null) System.err.print("(" + tag + ") ");
        if (msg != null) System.err.print(msg);
        if (tag != null || msg != null) System.err.println();
        if (t != null) t.printStackTrace(System.err);
        return 0;
    }

}
