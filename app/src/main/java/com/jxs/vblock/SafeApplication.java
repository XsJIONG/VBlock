package com.jxs.vblock;

import android.app.*;
import android.content.*;
import android.os.*;
import java.io.*;

public class SafeApplication extends Application {
	private static final String T="VBlock";
	@Override
	protected void attachBaseContext(Context base) {
		CrashHelper.install(new ErrorHolder() {
				@Override
				public void onError(Thread th, Throwable err) {
					Logs.wtf(T, "Uncaught exception", err);
				}
			});
		super.attachBaseContext(base);
		Logs.EnableLog = true;
		Logs.setLogFile(new File(Environment.getExternalStorageDirectory(), "VBlockLog.txt"));
	}
	public static class CrashHelper {
		private static boolean installed=false;
		private static ErrorHolder Holder;
		private static Thread.UncaughtExceptionHandler DEF;
		public static void install(ErrorHolder holder) {
			if (installed) throw new IllegalStateException();
			if (holder == null) throw new IllegalArgumentException();
			Holder = holder;
			new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						while (true) {
							try {
								Looper.loop();
							} catch (Throwable t) {
								if (t instanceof QuitException) return;
								Holder.onError(Looper.getMainLooper().getThread(), t);
							}
						}
					}
				});
			DEF = Thread.getDefaultUncaughtExceptionHandler();
			Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
					@Override
					public void uncaughtException(Thread th, Throwable t) {
						Holder.onError(th, t);
					}
				});
		}
		public static void uninstall() {
			installed = false;
			Holder = null;
			Thread.setDefaultUncaughtExceptionHandler(DEF);
			new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						throw new QuitException();
					}
				});
		}
		public static boolean isInstalled() {
			return installed;
		}
		private static class QuitException extends RuntimeException {}
	}
	public static interface ErrorHolder {
		void onError(Thread th, Throwable err);
	}
}
