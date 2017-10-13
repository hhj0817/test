package com.example.test.test.bean;

import android.graphics.drawable.Drawable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

/**
 * Created by hhj on 2017/08/23.
 */
@Entity
public class AppLockInfo{

    @Id(autoincrement = true)
    private Long id;
    private String pkgName;
    private String appName;
    @Transient
    private Drawable icon;

    private boolean isLock;
    private boolean isBrightnessLock;
    private boolean isRotateLock;

    @Generated(hash = 184551681)
    public AppLockInfo(Long id, String pkgName, String appName, boolean isLock,
                       boolean isBrightnessLock, boolean isRotateLock) {
        this.id = id;
        this.pkgName = pkgName;
        this.appName = appName;
        this.isLock = isLock;
        this.isBrightnessLock = isBrightnessLock;
        this.isRotateLock = isRotateLock;
    }

    @Generated(hash = 1089439453)
    public AppLockInfo() {

    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPkgName() {
        return this.pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public String getAppName() {
        return this.appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public boolean getIsLock() {
        return this.isLock;
    }

    public void setIsLock(boolean isLock) {
        this.isLock = isLock;
    }

    public boolean getIsBrightnessLock() {
        return this.isBrightnessLock;
    }

    public void setIsBrightnessLock(boolean isBrightnessLock) {
        this.isBrightnessLock = isBrightnessLock;
    }

    public boolean getIsRotateLock() {
        return this.isRotateLock;
    }

    public void setIsRotateLock(boolean isRotateLock) {
        this.isRotateLock = isRotateLock;
    }

    public Drawable getIcon() {
        return this.icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }
}
