package com.jxs.vblock;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;

import com.sun.jna.NativeLibrary;

import java.io.File;
import java.lang.ref.WeakReference;

import dalvik.system.PathClassLoader;

public class MCActivity extends com.mojang.minecraftpe.MainActivity implements View.OnClickListener {
	public static final String T="MCActivity";

	public static WeakReference<MCActivity> Current;
	private FloatButton FB;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Current = new WeakReference<MCActivity>(this);
		Logs.i(T, "Before onCreate");
		try {
			File LIBDIR=Global.MCPE_LIBDIR;
			String path;
			if (LIBDIR==null) {
				path=super.getSharedPreferences(Global.SP, Context.MODE_PRIVATE).getString(Global.TAG_LIBDIR,null);
				InjectHelper.InjectNativeLibraryPath((PathClassLoader) getClassLoader(), new File(path));
			} else path=LIBDIR.getAbsolutePath();
			Replaced=FakePackageManager.getInstance(FakePackageManager.getContextImpl(this), super.getPackageManager(), path);
			super.onCreate(savedInstanceState);
			Replaced=null;
		} catch (Throwable t) {
			Logs.e(T, "onCreate failed", t);
			NativeLibrary libdl=NativeLibrary.getInstance("dl");
			Logs.e(T, "dlerror:"+libdl.getFunction("dlerror").invokeString(new Object[]{},false));
			Logs.i(T, getClassLoader().toString());
		}
		Logs.i(T, "After onCreate");
	}
	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		InitializeFB();
	}
	private PackageManager Replaced;
	@Override
	public PackageManager getPackageManager() {
		Logs.i(T,"Get PackageManager");
		if (Replaced!=null) {
			Logs.i(T, "Get Replaced PackageManager");
			return Replaced;
		}
		return super.getPackageManager();
	}
	/*@Override
	public Resources getResources() {
		return Global.MinecraftContext.getResources();
	}*/
	@Override
	public AssetManager getAssets() {
		return Global.MinecraftAssets;
	}
	private DebugWrapper Wrapper;
	@Override
	public Context getApplicationContext() {
		//return super.getApplicationContext();
		if (Wrapper == null) Wrapper = new DebugWrapper(super.getApplicationContext());
		return Wrapper;
	}
	@Override
	public SharedPreferences getSharedPreferences(String name, int mode) {
		Logs.i(T, "MCActivity getSharedPreferences:" + name, new Throwable());
		return super.getSharedPreferences(name, mode);
	}
	@Override
	public File getFilesDir() {
		Logs.i(T, "Get files dir:" + super.getFilesDir().getAbsolutePath());
		return super.getFilesDir();
	}
	private static class DebugWrapper extends ContextWrapper {
		public DebugWrapper(Context cx) {
			super(cx);
			Logs.i(T, "Get DebugWrapper");
		}

		@Override
		public SharedPreferences getSharedPreferences(String name, int mode) {
			return getSharedPreferences(name, mode);
		}

		@Override
		public File getDir(String name, int mode) {
			Logs.i(T, "getDir:" + name);
			return super.getDir(name, mode);
		}
	}
	private void InitializeFB() {
		FB=new FloatButton(this);
		FB.showAt(getWindow().getDecorView());
		FB.setOnClickListener(this);
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		FB.dismiss();
		Global.MinecraftLibrary.dispose();
	}
	@Override
	public void onClick(View v) {
		Global.MinecraftLibrary.getFunction("_ZN5Level7setTimeEi").invoke(new Object[]{500});
	}
}