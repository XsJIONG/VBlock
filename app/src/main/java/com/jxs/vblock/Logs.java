package com.jxs.vblock;

import android.util.*;
import java.io.*;
import java.nio.channels.*;

public final class Logs {
	public static boolean EnableLog=true;
	private static final String TAG="Logs";
	private static RandomAccessFile Q;
	private static File Origin=null;
	private static byte[] Writing=new byte[0];
	private static void safeDelete(File f) {
		if (f.isDirectory())
			for (File one : f.listFiles()) safeDelete(one);
		f.delete();
	}
	public static void setLogFile(File f) {
		if (!EnableLog) return;
		synchronized (Writing) {
			if (f == null) return;
			if (f.exists()) safeDelete(f);
			try {
				Q = new RandomAccessFile(f, "rw");
			} catch (IOException e) {
				throw new RuntimeException("Can't create RandomAccessFile for:" + f.getAbsolutePath(), e);
			}
			if (Origin != null && Origin.exists()) {
				try {
					f.createNewFile();
					FileInputStream in=new FileInputStream(Origin);
					FileChannel inc=in.getChannel();
					WritableByteChannel outc=Q.getChannel();
					inc.transferTo(0, inc.size(), outc);
					inc.close();
					in.close();
				} catch (IOException e) {
					throw new RuntimeException(join("Can't copy origin log file(", Origin.getAbsolutePath(), ") to new one(", f.getAbsolutePath(), ")"), e);
				}
			}
		}
	}
	public static String getLogText() {
		if (!EnableLog) return null;
		if (!Origin.exists()) return null;
		try {
			InputStream in=new FileInputStream(Origin);
			byte[] data=new byte[in.available()];
			in.read(data);
			in.close();
			String s=new String(data);
			data = null;
			System.gc();
			return s;
		} catch (IOException e) {
			Log.e(TAG, join("Can't get log text from file(", Origin.getAbsolutePath(), ")"), e);
			return null;
		}
	}
	public static void clearLog() {
		if (!EnableLog) return;
		if (Origin == null) return;
		synchronized (Writing) {
			try {
				Q.close();
				Origin.delete();
				Q = new RandomAccessFile(Origin, "rw");
			} catch (IOException e) {
				Log.e(TAG, join("Can't recreate FileOutputStream for log file(", Origin.getAbsolutePath(), ")"), e);
				return;
			}
		}
	}
	public static void closeLog(boolean keepFile) {
		if (!EnableLog) return;
		synchronized (Writing) {
			try {
				Q.close();
			} catch (IOException e) {
				throw new RuntimeException("Can't close RandomAccessFile", e);
			}
			Q = null;
			if (!keepFile) Origin.delete();
			Origin = null;
		}
	}
	public static File getLogFile() {
		if (!EnableLog) return null;
		return Origin;
	}
	public static String join(Object...args) {
		StringBuffer b=new StringBuffer();
		for (Object one : args) b.append(one);
		return b.toString();
	}
	private static void print(String tag, char level, String msg) {
		try {
			Q.writeBytes(join(level, " [", tag, "]: ", msg));
			Q.write('\n');
		} catch (IOException e) {
			throw new RuntimeException("Can't write log", e);
		}
	}
	private static void printMsg(String tag, char level, String msg) {
		if (!EnableLog) return;
		synchronized (Writing) {
			print(tag, level, msg);
		}
	}
	public static String getStackTraceString(Throwable t) {
		return Log.getStackTraceString(t);
	}
	private static void printErr(String tag, char level, String msg, Throwable err) {
		if (!EnableLog) return;
		synchronized (Writing) {
			print(tag, level, msg);
			print(tag, level, getStackTraceString(err));
		}
	}
	public static void v(String tag, String msg) {
		printMsg(tag, 'V', msg);
	}
	public static void v(String tag, String msg, Throwable err) {
		printErr(tag, 'V', msg, err);
	}
	public static void d(String tag, String msg) {
		printMsg(tag, 'D', msg);
	}
	public static void d(String tag, String msg, Throwable err) {
		printErr(tag, 'D', msg, err);
	}
	public static void i(String tag, String msg) {
		printMsg(tag, 'I', msg);
	}
	public static void i(String tag, String msg, Throwable err) {
		printErr(tag, 'I', msg, err);
	}
	public static void w(String tag, String msg) {
		printMsg(tag, 'W', msg);
	}
	public static void w(String tag, String msg, Throwable err) {
		printErr(tag, 'W', msg, err);
	}
	public static void e(String tag, String msg) {
		printMsg(tag, 'E', msg);
	}
	public static void e(String tag, String msg, Throwable err) {
		printErr(tag, 'E', msg, err);
	}
	public static void wtf(String tag, String msg) {
		printMsg(tag, 'F', msg);
	}
	public static void wtf(String tag, String msg, Throwable err) {
		printErr(tag, 'F', msg, err);
	}
}
