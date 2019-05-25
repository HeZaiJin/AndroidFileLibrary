package com.hand.document.util;

import android.annotation.SuppressLint;
import android.util.Log;
import com.hand.document.BuildConfig;

@SuppressLint("LogTagMismatch")
public class LogUtil {

    private static final String GLOBAL_TAG = BuildConfig.APPLICATION_ID;

    private static final boolean DEBUG = true;

    private static boolean isLoggable(String tag, int level) {
        return DEBUG || Log.isLoggable(tag, level);
    }

    public static void v(String tag, String msg) {
        if (isLoggable(GLOBAL_TAG, Log.VERBOSE)) {
            Log.v(GLOBAL_TAG + ":" + tag, checkMsg(msg));
        }
    }

    public static void d(String tag, String msg) {
        if (isLoggable(GLOBAL_TAG, Log.DEBUG)) {
            Log.d(GLOBAL_TAG + ":" + tag, checkMsg(msg));
        }
    }

    public static void i(String tag, String msg) {
        if (isLoggable(GLOBAL_TAG, Log.INFO)) {
            Log.i(GLOBAL_TAG + ":" + tag, checkMsg(msg));
        }
    }

    public static void w(String tag, String msg) {
        if (isLoggable(GLOBAL_TAG, Log.WARN)) {
            Log.w(GLOBAL_TAG + ":" + tag, checkMsg(msg));
        }
    }

    public static void e(String tag, String msg) {
        if (isLoggable(GLOBAL_TAG, Log.ERROR)) {
            Log.e(GLOBAL_TAG + ":" + tag, checkMsg(msg));
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (isLoggable(GLOBAL_TAG, Log.ERROR)) {
            Log.e(GLOBAL_TAG + ":" + tag, checkMsg(msg), tr);
        }
    }

    private static String checkMsg(String msg) {
        return msg == null ? "null" : msg;
    }

    /**
     * 分段打印全部字符，解决日志打印不全问题
     *
     * @param tag 日志级别
     * @param msg 日志信息
     */
    public static void iPrintfALLStr(String tag, String msg) {
        if (isLoggable(GLOBAL_TAG, Log.ERROR)) {
            //因为String的length是字符数量不是字节数量所以为了防止中文字符过多，
            //  把4*1024的MAX字节打印长度改为2001字符数
            int max_str_length = 2001 - tag.length();
            //大于4000时
            while (msg.length() > max_str_length) {
                Log.i(GLOBAL_TAG + ":" + tag, checkMsg(msg.substring(0, max_str_length)));
                msg = msg.substring(max_str_length);
            }
            //剩余部分
            Log.i(GLOBAL_TAG + ":" + tag, checkMsg(msg));
        }

    }
}
