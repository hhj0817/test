package com.example.test.test.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.test.test.R;
import com.example.test.test.bean.AppLockInfo;
import com.example.test.test.dao.AppLockDao;
import com.example.test.test.ui.AppLockActivity;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by hhj on 2017/08/23.
 */

public class AppLockAdapter extends RecyclerView.Adapter<AppLockAdapter.AppLockViewHolder> {

    private List<AppLockInfo> mDatas;
    private OnItemClickListener mOnItemClickListener;
    private int mLockType;

    public AppLockAdapter(List<AppLockInfo> datas, int lockType) {
        mDatas = datas;
        mLockType = lockType;
    }

    @Override
    public AppLockViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_lock_list, parent, false);
        final AppLockViewHolder holder = new AppLockViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    int position = holder.getLayoutPosition();
                    AppLockInfo appLockInfo = mDatas.get(position);
                    if (mLockType == AppLockActivity.NORMAL) {
                        appLockInfo.setIsLock(!appLockInfo.getIsLock());
                    } else if (mLockType == AppLockActivity.BRIGHTNESS) {
                        appLockInfo.setIsBrightnessLock(!appLockInfo.getIsBrightnessLock());
                    } else if (mLockType == AppLockActivity.ROTATE) {
                        appLockInfo.setIsRotateLock(!appLockInfo.getIsRotateLock());
                    }
                    mOnItemClickListener.onItemClick();
                    AppLockDao.updateAppLockInfo(appLockInfo);
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(AppLockViewHolder holder, int position) {
        AppLockInfo appLockInfo = mDatas.get(position);
        holder.icon.setImageDrawable(appLockInfo.getIcon());
        holder.appName.setText(appLockInfo.getAppName());
        boolean lock = false;
        if (mLockType == AppLockActivity.NORMAL) {
            lock = appLockInfo.getIsLock();
        } else if (mLockType == AppLockActivity.BRIGHTNESS) {
            lock = appLockInfo.getIsBrightnessLock();
        } else if (mLockType == AppLockActivity.ROTATE) {
            lock = appLockInfo.getIsRotateLock();
        }
        if (lock) {
            holder.lock.setImageResource(R.mipmap.app_lock);
        } else {
            holder.lock.setImageResource(R.mipmap.app_unlock);
        }
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    class AppLockViewHolder extends RecyclerView.ViewHolder {
        private ImageView icon;
        private TextView appName;
        private ImageView lock;

        public AppLockViewHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            appName = (TextView) itemView.findViewById(R.id.appName);
            lock = (ImageView) itemView.findViewById(R.id.btn_lock);
        }
    }

    public interface OnItemClickListener {
        void onItemClick();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    /**
     * 数据排序
     */
    public void sort() {
        Collections.sort(mDatas, createComparator());
    }

    /**
     * 根据锁类型不同，创建不同的Comparator进行排序
     *
     * @return
     */
    private Comparator<AppLockInfo> createComparator() {
        Comparator<AppLockInfo> comparator = null;
        if (mLockType == AppLockActivity.NORMAL) {
            comparator = new Comparator<AppLockInfo>() {
                @Override
                public int compare(AppLockInfo o1, AppLockInfo o2) {
                    if (o1.getIsLock() && !o2.getIsLock()) {
                        return -1;
                    } else if (!o1.getIsLock() && o2.getIsLock()) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            };
        } else if (mLockType == AppLockActivity.BRIGHTNESS) {
            comparator = new Comparator<AppLockInfo>() {
                @Override
                public int compare(AppLockInfo o1, AppLockInfo o2) {
                    if (o1.getIsBrightnessLock() && !o2.getIsBrightnessLock())
                        return -1;
                    else if (!o1.getIsBrightnessLock() && o2.getIsBrightnessLock())
                        return 1;
                    else
                        return 0;
                }
            };
        } else if (mLockType == AppLockActivity.ROTATE) {
            comparator = new Comparator<AppLockInfo>() {
                @Override
                public int compare(AppLockInfo o1, AppLockInfo o2) {
                    if (o1.getIsRotateLock() && !o2.getIsRotateLock())
                        return -1;
                    else if (!o1.getIsRotateLock() && o2.getIsRotateLock())
                        return 1;
                    else
                        return 0;
                }
            };
        }
        return comparator;
    }
}
