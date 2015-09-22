package com.kisdy.imageloder.activitys;

import java.util.ArrayList;
import java.util.List;

import com.kisdy.imageloader.utils.ImageLoader;
import com.kisdy.imageloder.R;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class SelectFilePopupWindow extends PopupWindow {

	private String mlastSelectedFile="";
	private static final String TAG = "SelectFilePopupWindow";
	private int width,height;
	private View mConverView;
	private ListView mListView;
	private List<FolderBean> beanList;
	ListDirAdapter mdirAdapter;
	private String mCurrentSelectFileName;

	/*public SelectFilePopupWindow(){
		super(mConverView,width,height);
	}*/

	public SelectFilePopupWindow(Context context,List<FolderBean> mdata,String currentDir){
		super(context);

		//计算popupWindow宽度和高度
		calate(context);
		mConverView=LayoutInflater.from(context).inflate(R.layout.pop_window_main, null);
		setContentView(mConverView);
		if(mdata!=null)
			beanList=mdata;
		else
			beanList=new ArrayList<FolderBean>();
		
		mlastSelectedFile=currentDir.substring(currentDir.lastIndexOf("/")+1);
		
		Log.d(TAG, "mlastSelectedFile="+mlastSelectedFile);
		setHeight(height);
		setWidth(width);	

		setFocusable(true);
		setTouchable(true);
		setOutsideTouchable(true);
		setBackgroundDrawable(new BitmapDrawable());

		setTouchInterceptor(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
					dismiss();
					return true;
				}
				return false;
			}
		});

		mdirAdapter=new ListDirAdapter(context, beanList);
		initView();
	}

	/**
	 * 计算PopupWindow宽和高
	 * @param context
	 */
	void calate(Context context){
		WindowManager wm=(WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics=new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);
		width=outMetrics.widthPixels;
		height=(int) (outMetrics.heightPixels*0.7);


	}

	void initView(){
		mListView =(ListView) mConverView.findViewById(R.id.id_lvdir);
		mListView.setAdapter(mdirAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(onDirSelectChangeListener!=null){
					mlastSelectedFile=beanList.get(position).getFileName();
					mdirAdapter.notifyDataSetChanged();
					onDirSelectChangeListener.onSelected(beanList.get(position));
				}

			}
		});
	}

	OnDirSelectChangeListener onDirSelectChangeListener;

	public void setOnDirSelectChangeListener(OnDirSelectChangeListener onDirSelectChangeListener) {
		this.onDirSelectChangeListener = onDirSelectChangeListener;
	}


	interface OnDirSelectChangeListener{
		void onSelected(FolderBean bean);
	}


	private class ListDirAdapter extends ArrayAdapter<FolderBean>{
		private Context context;

		private boolean isChecked;
		public boolean isChecked() {
			return isChecked;
		}

		public void setChecked(boolean isChecked) {
			this.isChecked = isChecked;
		}

		public ListDirAdapter(Context context,List<FolderBean> list) {
			super(context,0, list);
			this.context=context;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewholder=null;
			if(convertView==null){
				convertView=LayoutInflater.from(context).inflate(R.layout.item_pop_window,null);
				viewholder=new ViewHolder();
				viewholder.mImg=(ImageView) convertView.findViewById(R.id.iv_firstImage);
				viewholder.tvDicName=(TextView) convertView.findViewById(R.id.id_tv_itemname);
				viewholder.tvDirCount=(TextView) convertView.findViewById(R.id.id_tv_itemcount);
				viewholder.iv_IsSelected=(ImageView) convertView.findViewById(R.id.id_iv_selectedimg);
				convertView.setTag(viewholder);
			}else{
				viewholder=	(ViewHolder) convertView.getTag();
			}
			viewholder.mImg.setImageResource(R.drawable.friends_sends_pictures_no); //重置图片
			FolderBean bean=getItem(position);
			ImageLoader.getInstance(3, com.kisdy.imageloader.utils.Type.LIFO).loadImage(bean.getFirstFilePath(), viewholder.mImg);
			viewholder.tvDicName.setText(bean.getFileName());
			viewholder.tvDirCount.setText(bean.getFileCount()+"张");

			if(bean.getFileName().equals(mlastSelectedFile))
				viewholder.iv_IsSelected.setVisibility(View.VISIBLE);
			else
				viewholder.iv_IsSelected.setVisibility(View.GONE);
			return convertView;
		}

		class ViewHolder{
			ImageView mImg;
			TextView tvDicName;
			TextView tvDirCount;
			ImageView iv_IsSelected;
		}

	}
}
