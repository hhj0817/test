package com.example.test.test.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.test.test.BaseViewHolder;
import com.example.test.test.R;
import com.example.test.test.bean.AppLockSettingBean;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by hhj on 2017/08/25.
 */

public class AppLockSettingAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private List<AppLockSettingBean> mDatas;
    private OnItemClickListener mOnItemClickListener;

    public AppLockSettingAdapter(List<AppLockSettingBean> datas) {
        mDatas = datas;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, final int viewType) {
        final BaseViewHolder holder;
        if (viewType == AppLockSettingBean.TYPE_TITLE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_lock_setting_title, parent, false);
            holder = new TitleViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_lock_setting, parent, false);
            holder = new NormalViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onClick(holder.getLayoutPosition());
                    }
                }
            });
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        holder.bindView(mDatas.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        AppLockSettingBean appLockSettingBean = mDatas.get(position);
        return appLockSettingBean.type;
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    class TitleViewHolder extends BaseViewHolder<AppLockSettingBean> {
        @BindView(R.id.title_text)
        TextView title;

        public TitleViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bindView(AppLockSettingBean bean) {
            title.setText(bean.text);
        }
    }

    class NormalViewHolder extends BaseViewHolder<AppLockSettingBean> {
        @BindView(R.id.text)
        TextView text;
        @BindView(R.id.sub_text)
        TextView subText;
        @BindView(R.id.btn_switch)
        ImageView btnSwitch;

        public NormalViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bindView(AppLockSettingBean bean) {
            if(bean.enable){
                itemView.setBackgroundResource(R.drawable.item_app_lock_list_selector);
                itemView.setEnabled(true);
            }else{
                itemView.setBackgroundColor(Color.parseColor("#33999999"));
                itemView.setEnabled(false);
            }
            text.setText(bean.text);
            if (bean.type == AppLockSettingBean.TYPE_NORMAL) {
                subText.setVisibility(View.GONE);
                btnSwitch.setVisibility(View.GONE);
            } else if (bean.type == AppLockSettingBean.TYPE_OPTION) {
                subText.setVisibility(View.VISIBLE);
                btnSwitch.setVisibility(View.GONE);
                subText.setText(bean.subText);
            } else if (bean.type == AppLockSettingBean.TYPE_SWITCH) {
                subText.setVisibility(View.GONE);
                btnSwitch.setVisibility(View.VISIBLE);
                if (bean.switchOn) {
                    btnSwitch.setSelected(true);
                } else {
                    btnSwitch.setSelected(false);
                }
            }
        }
    }

    public interface OnItemClickListener {
        void onClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

}
