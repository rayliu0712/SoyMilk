package com.kitkat0712.soymilk;

public class SwitchConfig {
    public static final int length = 6;
    public boolean hideStatusBar;
    public boolean hideNavigationBar;
    public boolean enableFlagSecure;
    public boolean enableAdBlock;
    public boolean enableMask;
    public boolean useNumPad;

    public boolean[] toArray()
    {
        return new boolean[]{
                hideStatusBar,
                hideNavigationBar,
                enableFlagSecure,
                enableAdBlock,
                enableMask,
                useNumPad,
        };
    }

    public void update(final boolean[] arr) {
        hideStatusBar = arr[0];
        hideNavigationBar = arr[1];
        enableFlagSecure = arr[2];
        enableAdBlock = arr[3];
        enableMask = arr[4];
        useNumPad = arr[5];
    }
}
