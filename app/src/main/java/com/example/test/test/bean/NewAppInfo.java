package com.example.test.test.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by hhj on 2017/08/17.
 */
@Entity
public class NewAppInfo  {

    private String pkgName;

    @Generated(hash = 1326605599)
    public NewAppInfo(String pkgName) {
        this.pkgName = pkgName;
    }

    @Generated(hash = 1229693366)
    public NewAppInfo() {
    }

    public String getPkgName() {
        return this.pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

}
