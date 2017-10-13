package com.example.test.test.bean;

/**
 * Created by hhj on 2017/08/25.
 */

public class AppLockSettingBean {
    public static final int TYPE_TITLE = 1;
    public static final int TYPE_NORMAL = 2;
    public static final int TYPE_SWITCH = 3;
    public static final int TYPE_OPTION = 4;

    public int type;
    public String text;
    //option
    public String subText;
    //switch
    public boolean switchOn;
    public boolean enable = true;

    //title
    public AppLockSettingBean(int type, String text) {
        this.type = type;
        this.text = text;
    }

    public AppLockSettingBean(int type, String text, String subText) {
        this.type = type;
        this.text = text;
        this.subText = subText;
    }

    //switch
    public AppLockSettingBean(int type, String text, boolean switchOn) {
        this.type = type;
        this.text = text;
        this.switchOn = switchOn;
    }

}
