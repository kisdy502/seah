package com.kisdy.news.activitys;


import java.util.ArrayList;

import org.apache.http.client.methods.AbortableHttpRequest;

import com.kisdy.news.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class NewsAdapter extends BaseAdapter implements OnScrollListener{

	private ArrayList<NewBean> mList;
	private LayoutInflater mInflater;
	private Context mContext;
	/**
	 * 开始与结束位置
	 */
	private int statIndex,endIndex; 
	/**
	 * 图片URL
	 */
	public static String [] mUrls;
	
	private ListView mListView;
	/**
	 * 是否是首次加载
	 */
	private boolean isFirstLoading=true;

	public NewsAdapter(Context ctx,ArrayList<NewBean> list,ListView listView){
		mListView=listView;
		mContext=ctx;
		mList=list;
		mInflater=LayoutInflater.from(ctx);
		int len=mList.size();
		mUrls=new String[len];
		for(int i=0;i<len;i++){
			mUrls[i]=mList.get(i).getNewImageUrl();
		}
		mListView.setOnScrollListener(this);
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder=null;
		if(convertView==null){
			viewHolder=new ViewHolder();
			convertView =mInflater.inflate(R.layout.item_news, null);
			viewHolder.ivIcon=(ImageView) convertView.findViewById(R.id.iv_newsIcon);
			viewHolder.tvTitle=(TextView) convertView.findViewById(R.id.tv_newsTitle);
			viewHolder.tvContent=(TextView) convertView.findViewById(R.id.tv_newsContent);
			convertView.setTag(viewHolder);
		}else{
			viewHolder=(ViewHolder)convertView.getTag();		
		}
		
		//getView时不再加载图片
		viewHolder.ivIcon.setImageResource(R.drawable.ic_launcher);
		viewHolder.ivIcon.setTag(mList.get(position).getNewImageUrl());

		//new ImageLoader().loadImgFromUrlByThread(mList.get(position).getNewImageUrl(),viewHolder.ivIcon);
		ImageLoader.getInstance(mListView).loadImgFromUrlByAsyncTask(mList.get(position).getNewImageUrl(),viewHolder.ivIcon);
		viewHolder.tvTitle.setText(mList.get(position).getNewTile());
		viewHolder.tvContent.setText(mList.get(position).getNewContent());
		return convertView;
	}

	class ViewHolder{
		public TextView tvTitle;
		public TextView tvContent;
		public ImageView ivIcon;
	}


	@Override
	public void onScroll(AbsListView arg0, int firstVisiableItem, int visableItemCount, int totalItemCount) {
	   statIndex=firstVisiableItem;
	   endIndex=firstVisiableItem+visableItemCount;
	   if(isFirstLoading&&visableItemCount>0){
		   ImageLoader.getInstance(mListView).downloadImages(statIndex, endIndex);
		   isFirstLoading=false;
	   }
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int state) {
		if(state==SCROLL_STATE_IDLE){
            //停止滚时才加载图片
			ImageLoader.getInstance(mListView).downloadImages(statIndex,endIndex);
		}else{
			//滚动时取消所有下载任务
			ImageLoader.cancelAllTask();
		}

	}

}
