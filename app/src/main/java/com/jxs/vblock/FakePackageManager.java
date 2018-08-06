package com.jxs.vblock;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class FakePackageManager {
	public static final String T="VBlock";
	private static Class<?> PMClass=null,APMClass=null,CImplClass=null,CWClass=null;
	private static Constructor<?> Cons=null;
	private static Field PMField=null;
	public static final PackageManager getInstance(final Context cx, final PackageManager origin, final String dir) {
		if (!checkClass()) return null;
		try {
			final Object pm=PMField.get(origin);
			return (PackageManager) Cons.newInstance(cx, Proxy.newProxyInstance(PMClass.getClassLoader(), new Class<?>[]{PMClass}, new InvocationHandler() {
				@Override
				public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
					String s = method.getName();
					Object res = method.invoke(pm, objects);
					if (s.equals("getActivityInfo") || s.equals("getReceiverInfo"))
						((ActivityInfo) res).applicationInfo.nativeLibraryDir = dir;
					Logs.i(T,s);
					return res;
				}
			}));
		} catch (Throwable t) {
			Logs.e(T, "Can't Find Generate a ApplicationPackageManager", t);
			return null;
		}
	}
	private static boolean checkClass() {
		if (BaseField==null) {
			try {
				PMClass = Class.forName("android.content.pm.IPackageManager");
				APMClass = Class.forName("android.app.ApplicationPackageManager");
				CImplClass = Class.forName("android.app.ContextImpl");
				Cons=APMClass.getDeclaredConstructor(CImplClass,PMClass);
				Cons.setAccessible(true);
				PMField=APMClass.getDeclaredField("mPM");
				PMField.setAccessible(true);
				CWClass = ContextWrapper.class;
				BaseField = CWClass.getDeclaredField("mBase");
			} catch (Throwable t) {
				Logs.e(T, "Init Classes, Fields and Constructors Failed", t);
				return false;
			}
			BaseField.setAccessible(true);
		}
		return true;
	}
	private static Field BaseField=null;
	public static final Context getContextImpl(Context cx) {
		if (!checkClass()) return null;
		if (CImplClass.isInstance(cx)) return cx;
		try {
			return (Context) BaseField.get(cx);
		} catch (Throwable t) {
			Logs.e(T, "Can't Get ContextImpl From Context", t);
			return null;
		}
	}
}
