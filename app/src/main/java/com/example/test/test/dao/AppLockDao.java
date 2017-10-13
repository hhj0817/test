package com.example.test.test.dao;

import com.example.test.test.MyApplication;
import com.example.test.test.bean.AppLockInfo;
import com.example.test.test.bean.AppLockInfoDao;
import com.example.test.test.bean.NewAppInfo;

import java.util.List;

/**
 * Created by hhj on 2017/08/24.
 */

public class AppLockDao {

    public static void insertAppLockInfo(AppLockInfo appLockInfo) {
        MyApplication.getDaoSession().getAppLockInfoDao().insertOrReplace(appLockInfo);
    }

    public static AppLockInfo queryAppLockInfo(String pkgName) {
        List<AppLockInfo> appLockInfos = MyApplication.getDaoSession().getAppLockInfoDao()
                .queryBuilder()
                .where(AppLockInfoDao.Properties.PkgName.eq(pkgName))
                .build()
                .list();
        if (appLockInfos == null || appLockInfos.size() == 0)
            return null;
        else
            return appLockInfos.get(0);
    }

    public static void updateAppLockInfo(AppLockInfo appLockInfo){
        MyApplication.getDaoSession().getAppLockInfoDao().update(appLockInfo);
    }

    public static void updateNewAppDetectInfos(List<NewAppInfo> appInfos){
        MyApplication.getDaoSession().getNewAppInfoDao().deleteAll();
        MyApplication.getDaoSession().getNewAppInfoDao().insertInTx(appInfos);
    }

    public static List<NewAppInfo> getNewAppDetectInfos(){
        return MyApplication.getDaoSession().getNewAppInfoDao().loadAll();
    }
}
