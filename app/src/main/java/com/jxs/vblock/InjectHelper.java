package com.jxs.vblock;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;

public class InjectHelper {
	public static final String T="VBlock";

	public static ClassLoader CL;
	private static Class<?> BaseDexClassLoaderClass=null;
	private static Field PathListField=null;
	public static void InjectNativeLibraryPath(PathClassLoader cl, File libPath) {
		if (!libPath.exists()) {
			Logs.w(T, "MCPE Lib Dir(" + libPath + ") not found, gave up injecting native library path");
			return;
		}
		if (hasDexLoader())
			try {
				Logs.d(T, "Inject Native Library using V14");
				injectNativeLibraryPathV14(cl, libPath);
			} catch (Throwable t) {
				Logs.d(T, "Inject Native Library V14 failed, using V21 - " + t.toString());
				try {
					injectNativeLibraryPathV21(cl, libPath);
				} catch (Throwable e) {
					Logs.e(T, "Inject Native Library V21 failed", e);
					return;
				}
			}
		else {
			Logs.d(T, "Inject Native Library under V14");
			try {
				injectNativeLibraryUnderV14(cl, libPath);
			} catch (Throwable e) {
				Logs.e(T, "Inject Native Library under V14 failed", e);
				return;
			}
		}
		Logs.v(T, "Injected Native Library - " + libPath.getAbsolutePath());
	}
	private static boolean hasDexLoader() {
		if (BaseDexClassLoaderClass != null) return true;
		try {
			BaseDexClassLoaderClass = Class.forName("dalvik.system.BaseDexClassLoader");
			return true;
		} catch (Throwable t) {
			return false;
		}
	}
	private static void injectNativeLibraryUnderV14(PathClassLoader cl, File libPath) throws Throwable {
		Field mLibPaths = cl.getClass().getDeclaredField("mLibPaths");
		mLibPaths.setAccessible(true);
		String[] libs = (String[]) (mLibPaths).get(cl);
		String[] newPaths = new String[libs.length + 1];
		System.arraycopy(libs, 0, newPaths, 1, libs.length);
		libs[0] = libPath.getAbsolutePath();
		mLibPaths.set(cl, newPaths);
	}
	private static Object getPathList(PathClassLoader loader) throws Throwable {
		if (PathListField != null) return PathListField;
		PathListField = BaseDexClassLoaderClass.getDeclaredField("pathList");
		PathListField.setAccessible(true);
		return PathListField.get(loader);
	}
	private static void injectNativeLibraryPathV21(PathClassLoader cl, File libPath) throws Throwable {
		Object pathList = getPathList(cl);
		Class<?> elementClass = Class.forName("dalvik.system.DexPathList$Element");
		Constructor<?> element = null;
		try {
			element = elementClass.getConstructor(File.class, boolean.class, File.class, DexFile.class);
		} catch (NoSuchMethodException e) {
			Logs.e(T, "DexElement constructor not found", e);
			return;
		}
		Field systemNativeLibraryDirectories = pathList.getClass().getDeclaredField("systemNativeLibraryDirectories");
		Field nativeLibraryDirectories = pathList.getClass().getDeclaredField("nativeLibraryDirectories");
		Field nativeLibraryPathElements = pathList.getClass().getDeclaredField("nativeLibraryPathElements");
		systemNativeLibraryDirectories.setAccessible(true);
		nativeLibraryDirectories.setAccessible(true);
		nativeLibraryPathElements.setAccessible(true);
		List<File> systemFiles = (List<File>) systemNativeLibraryDirectories.get(pathList);
		List<File> nativeFiles = (List<File>) nativeLibraryDirectories.get(pathList);
		Object[] elementFiles = (Object[]) nativeLibraryPathElements.get(pathList);
		Object newElementFiles = Array.newInstance(elementClass, elementFiles.length + 1);
		systemFiles.add(libPath);
		nativeFiles.add(libPath);
		systemNativeLibraryDirectories.set(pathList, systemFiles);
		nativeLibraryDirectories.set(pathList, nativeFiles);
		if (element != null) {
			try {
				Object newInstance = element.newInstance(libPath, true, null, null);
				Array.set(newElementFiles, 0, newInstance);
				System.arraycopy(elementFiles, 0, newElementFiles, 1, elementFiles.length);
				nativeLibraryPathElements.set(pathList, newElementFiles);
			} catch (Throwable t) {
				Logs.e(T, "Error setting library path elements", t);
			}
		}
	}
	private static void injectNativeLibraryPathV14(PathClassLoader cl, File libPath) throws Throwable {
		Class<?> cc=Class.forName("dalvik.system.BaseDexClassLoader");
		Field pf=cc.getDeclaredField("pathList");
		pf.setAccessible(true);
		Object pathList=pf.get(cl);
		Field sf=pathList.getClass().getDeclaredField("nativeLibraryDirectories");
		sf.setAccessible(true);
		File[] lpaths=(File[]) sf.get(pathList);
		File[] ns=new File[lpaths.length + 1];
		System.arraycopy(lpaths, 0, ns, 0, lpaths.length);
		ns[ns.length - 1] = libPath;
		sf.set(pathList, ns);
	}
	private static void delete(File f) {
		if (f.isDirectory())
			for (File one : f.listFiles()) delete(one);
		f.delete();
	}
}
