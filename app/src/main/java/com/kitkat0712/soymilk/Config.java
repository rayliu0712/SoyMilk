package com.kitkat0712.soymilk;

public class Config {
    public static final String[] switchItems = {"隱藏狀態列", "隱藏導覽列", "啟用flag_secure", "啟用AdBlock", "啟用遮罩", "使用數字鍵盤"};
    public static final String[] buttonItems = {"[ 設定主頁面背景圖片 ]", "[ 設定遮罩圖片 ]", "[ 關於AdBlock ]", "[ 關於... ]"};
    public static final int boolLength = 6;
    public static final int length = 8;

    public boolean hideStatusBar;
    public boolean hideNavigationBar;
    public boolean enableFlagSecure;
    public boolean enableAdBlock;
    public boolean enableMask;
    public boolean useNumPad;

    public boolean[] toBoolArray() {
        return new boolean[]{
                hideStatusBar,
                hideNavigationBar,
                enableFlagSecure,
                enableAdBlock,
                enableMask,
                useNumPad
        };
    }

    public void updateBool(final boolean[] arr) {
        hideStatusBar = arr[0];
        hideNavigationBar = arr[1];
        enableFlagSecure = arr[2];
        enableAdBlock = arr[3];
        enableMask = arr[4];
        useNumPad = arr[5];
    }
}
