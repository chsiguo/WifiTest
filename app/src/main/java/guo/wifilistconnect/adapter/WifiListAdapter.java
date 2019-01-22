package guo.wifilistconnect.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import guo.wifilistconnect.R;
import guo.wifilistconnect.app.AppContants;
import guo.wifilistconnect.bean.WifiBean;


/**
 * Created by ${GuoZhaoHui} on 2017/11/7.
 * Email:guozhaohui628@gmail.com
 */

public class WifiListAdapter extends RecyclerView.Adapter<WifiListAdapter.MyViewHolder> {

    private Context mContext;
    private List<WifiBean> resultList;
    private onItemClickListener onItemClickListener;
    private onItemLongClickListener onItemLongClickListener;

    //新建两个私有变量用于保存用户设置的监听器及其set方法：
    public void setOnItemClickListener(WifiListAdapter.onItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
    public void setOnItemLongClickListener(WifiListAdapter.onItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }


    public WifiListAdapter(Context mContext, List<WifiBean> resultList) {
        this.mContext = mContext;
        this.resultList = resultList;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_wifi_list, parent, false);
        MyViewHolder vh = new MyViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final WifiBean bean = resultList.get(position);
        holder.tvItemWifiName.setText(bean.getWifiName());
        holder.tvItemWifiStatus.setText(bean.getState());
        holder.imageView.setImageResource(bean.getImageId());
        holder.lock.setVisibility(bean.getIfLock());

        //可以传递给adapter的数据都是经过处理的，已连接或者正在连接状态的wifi都是处于集合中的首位，所以可以写出如下判断
        if(position == 0  && (AppContants.WIFI_STATE_ON_CONNECTING.equals(bean.getState()) || AppContants.WIFI_STATE_CONNECT.equals(bean.getState()))){
            holder.tvItemWifiName.setTextColor(mContext.getResources().getColor(R.color.homecolor1));
            holder.tvItemWifiStatus.setTextColor(mContext.getResources().getColor(R.color.homecolor1));
        }else{
            holder.tvItemWifiName.setTextColor(mContext.getResources().getColor(R.color.black_home));
            holder.tvItemWifiStatus.setTextColor(mContext.getResources().getColor(R.color.gray_home));
        }
        //在onBindViewHolder方法内，实现回调：
        //为itemview设置监听器
        holder.itemview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClickListener.onItemClick(view,position,bean);
            }
        });

        holder.itemview.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onItemLongClickListener.onItemLongClick(v,position,bean);
              //返回true 表示消耗了事件 事件不会继续传递
                return true;
            }
        });
    }

    public void replaceAll(List<WifiBean> datas) {
        if (resultList.size() > 0) {
            resultList.clear();
        }
        resultList.addAll(datas);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return resultList.size();
    }


    static class MyViewHolder extends RecyclerView.ViewHolder{

        View itemview;
        TextView tvItemWifiName, tvItemWifiStatus;
        ImageView imageView, lock;

        public MyViewHolder(View itemView) {
            super(itemView);
            itemview = itemView;
            tvItemWifiName = (TextView) itemView.findViewById(R.id.tv_item_wifi_name);
            tvItemWifiStatus = (TextView) itemView.findViewById(R.id.tv_item_wifi_status);
            imageView = itemView.findViewById(R.id.info);
            lock=itemView.findViewById(R.id.lock_if);
        }

    }
    //①新建两个内部接口：
    public interface onItemClickListener{
        void onItemClick(View view, int postion, Object o);
    }

    public interface onItemLongClickListener{
        void onItemLongClick(View view,int position, Object o);
    }


}
