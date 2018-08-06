package com.jxs.vblock;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.ScrollView;

import com.sun.jna.NativeLibrary;

import java.io.File;

import dalvik.system.PathClassLoader;

public class LauncherActivity extends AppCompatActivity {
	public static final String T="VBlock";
	public static final String[] PERMISSIONS={Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.VIBRATE,Manifest.permission.ACCESS_NETWORK_STATE,Manifest.permission.GET_ACCOUNTS,Manifest.permission.INTERNET};
	public static final String[] EXPLANATIONS={"VBlock需要存储权限来存储数据","我的世界中需要振动来反馈方块被破坏等事件","我的世界中需要访问网络来登陆X-Box以及连接到商店","我的世界需要访问你的Google账户","VBlock需要网络权限来访问网络"};
	private static int ConsoleErrColor;
	private ScrollView Container;
	private AppCompatTextView Content;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ConsoleErrColor= ContextCompat.getColor(this, R.color.ConsoleErrColor);
		BuildView();
		CheckPermissions();
	}
	private void PostLaunchMC() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				LaunchMC();
			}
		}).start();
	}
	private final void SendMsg(int what, Object obj) {
		Message msg=new Message();
		msg.what=what;
		msg.obj=obj;
		H.sendMessage(msg);
	}
	private void WriteConsole(CharSequence cs) {
		SendMsg(MSG_APPEND,cs);
	}
	private void LaunchMC() {
		WriteConsole("检测Minecraft中...");
		try {
			PackageManager pm=getPackageManager();
			PackageInfo pinfo=pm.getPackageInfo(Global.MCPE_PKG, 0);
			WriteConsole("Minecraft版本 - "+pinfo.versionName+"("+pinfo.versionCode+")");
			ApplicationInfo ai=pm.getApplicationInfo(Global.MCPE_PKG, 0);
			Global.MCPE_LIBDIR = new File(ai.nativeLibraryDir);
		} catch (Throwable t) {
			WriteConsole("没有检测到Minecraft！");
			return;
		}
		WriteConsole("注入本地库路径...");
		InjectHelper.InjectNativeLibraryPath((PathClassLoader) getClassLoader(), Global.MCPE_LIBDIR);
		try {
			WriteConsole("正在加载FMod...");
			//System.load(new File(Global.MCPE_LIBDIR, Global.MCLIB_FMOD).getAbsolutePath());
			System.loadLibrary("fmod");
		} catch (Throwable t) {
			WriteConsole("加载FMod失败！");
			ShowErr(t);
			return;
		}
		try {
			WriteConsole("正在加载Minecraft...");
			Global.MCPE_LIB=new File(Global.MCPE_LIBDIR, Global.MCLIB_CORE);
			//System.load(Global.MCPE_LIB.getAbsolutePath());
			System.loadLibrary("minecraftpe");
		} catch (Throwable t) {
			WriteConsole("加载Minecraft失败！");
			ShowErr(t);
			return;
		}
		try {
			WriteConsole("正在加载Substrate...");
			System.loadLibrary("substrate");
		} catch (Throwable t) {
			WriteConsole("加载Substrate失败！");
			ShowErr(t);
			return;
		}
		try {
			WriteConsole("正在加载VBlock...");
			System.loadLibrary("vblock");
		} catch (Throwable t) {
			WriteConsole("加载VBlock失败！");
			ShowErr(t);
			return;
		}
		WriteConsole("正在加载全局上下文...");
		if (getPackageName().equals(Global.MCPE_PKG))
			Global.MinecraftContext = this;
		else
			try {
				Global.MinecraftContext = createPackageContext(Global.MCPE_PKG, CONTEXT_IGNORE_SECURITY);
				Global.MinecraftAssets = Global.MinecraftContext.getAssets();
			} catch (Throwable t) {
				WriteConsole("加载跨程序应用上下文失败！");
				ShowErr(t);
				return;
			}
		WriteConsole("正在加载JNA库...");
		try {
			Global.MinecraftLibrary = NativeLibrary.getInstance(Global.MCPE_LIB.getAbsolutePath());
		} catch (Throwable t) {
			WriteConsole("加载JNA库失败！");
			ShowErr(t);
			return;
		}
		getSharedPreferences(Global.SP, Context.MODE_PRIVATE).edit().putString(Global.TAG_LIBDIR,Global.MCPE_LIBDIR.getAbsolutePath()).commit();
		WriteConsole("加载完成！");
		LaunchMCActivity();
	}
	private static final int MSG_APPEND=1,MSG_START=2;
	private void ShowErr(Throwable t) {
		SpannableString str=new SpannableString(Log.getStackTraceString(t));
		str.setSpan(new ForegroundColorSpan(ConsoleErrColor), 0, str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		WriteConsole(str);
	}
	private void LaunchMCActivity() {
		SendMsg(MSG_START,null);
	}
	private void HandleMessage(Message msg) {
		switch (msg.what) {
			case MSG_APPEND:{
				Content.append((CharSequence) msg.obj);
				Content.append("\n");
				break;
			}
			case MSG_START:{
				startActivity(new Intent(LauncherActivity.this, MCActivity.class));
				finish();
				break;
			}
		}
	}
	private Handler H=new Handler() {
		@Override
		public void handleMessage(Message msg) {
			HandleMessage(msg);
		}
	};
	private void BuildView() {
		Container=new ScrollView(this);
		Content=new AppCompatTextView(this);
		Content.setTextColor(ContextCompat.getColor(this, R.color.ConsoleTextColor));
		Container.setFillViewport(true);
		Container.addView(Content,-1,-1);
		setContentView(Container);
	}
	public static final int REQUEST_PERMISSIONS_CODE=1926;
	private void CheckPermissions() {
		if (PermissionsOK())
			PostLaunchMC();
		else
			ActivityCompat.requestPermissions(this,PERMISSIONS,REQUEST_PERMISSIONS_CODE);
	}
	private boolean PermissionsOK() {
		for (String one : PERMISSIONS)
			if (ContextCompat.checkSelfPermission(this,one)==PackageManager.PERMISSION_DENIED) return false;
		return true;
	}
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode!=REQUEST_PERMISSIONS_CODE) return;
		for (int i=0;i<grantResults.length;i++)
			if (grantResults[i]==PackageManager.PERMISSION_DENIED) {
				new AlertDialog.Builder(this).setTitle("错误").setMessage(EXPLANATIONS[i]).setCancelable(false).setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						ActivityCompat.requestPermissions(LauncherActivity.this,PERMISSIONS,REQUEST_PERMISSIONS_CODE);
					}
				}).show();
				return;
			}
		PostLaunchMC();
	}
}