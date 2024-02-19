package com.example.fortest;
public class SDK {
    static {
        System.loadLibrary("fortest");
    }

    public static native void SDKInit(String mode,String ant, double[] pos,double[] enu,double cut, double intv, String targetPath);
    public static native int IOInputObsData(byte data);
    public static native int IOInputEphData(byte data);
    public static native void  SDKSetIntv(int intv);
    public static native String SDKRetrieve(String type,  int len);
    public static native int IOInputSsrData(byte data);
    public static native void SDKTerminate();
    public static native void SDKRestart();
    public static native void SDKSetpath(String path);
    public static native void sendEphData(byte[] receiveByte);
    public static native void sendSsrData(byte[] bytes);
    public static native void sendObsData(byte[] bytes);
}

