package com.jxs.vblock;

import android.content.Context;
import android.content.res.AssetManager;

import com.sun.jna.NativeLibrary;

import java.io.File;

public class Global {
	public static final String SP="vblock_config";
	public static final String TAG_LIBDIR="libdir";
	public static final String MCLIB_FMOD="libfmod.so",MCLIB_CORE="libminecraftpe.so";
	public static Context MinecraftContext;
	public static AssetManager MinecraftAssets;
	public static final String MCPE_PKG="com.mojang.minecraftpe";
	public static File MCPE_LIBDIR=null;
	public static File MCPE_LIB=null;
	public static NativeLibrary MinecraftLibrary;
}
