package com.jxs.vblock;

public class MinecraftManager {
	public static native void setTmpOptionFilePath(String path);
	public static native String getTmpOptionFilePath();
	public static native void setGamesDir(String path);
	public static native String getGamesDir();
}
