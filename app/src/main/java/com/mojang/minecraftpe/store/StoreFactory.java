package com.mojang.minecraftpe.store;

public class StoreFactory {
    public static Store createGooglePlayStore(String something, StoreListener listener) {
        return new Store(listener);
    }

    public static Store createAmazonAppStore(StoreListener listener) {
        return new Store(listener);
    }

    public static Store createAmazonAppStore(StoreListener listener, boolean a) {
        return new Store(listener);
    }

    public static Store createSamsungAppStore(StoreListener listener) {
        return new Store(listener);
    }
}
