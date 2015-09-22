package com.kisdy.imageloder.activitys;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.kisdy.imageloader.utils.ImageLoader;
import com.kisdy.imageloader.utils.ImageLoader2;
import com.kisdy.imageloder.R;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;

/**
 * ImageAdapter 图片展示适配器
 * @author Administrator
 * @param <MyImageView>
 *
 */
public class ImageAdapter extends BaseAdapter {
	private static final String TAG = "ImageAdapter";

	private Point mPoint = new Point(0, 0);

	private static Set<String> selectedImages=new HashSet<String>();  //在不同对象间共享数据

	private Context mContext;
	private String mImagePath; 	//文件所在路径
	LayoutInflater infalter;
	List<String> mFileList; 	//文件集合

	public ImageAdapter(Context context,String path,List<String> mdata) {
		super();
		mContext=context;
		mImagePath=path;
		infalter=LayoutInflater.from(mContext);
		mFileList=mdata;	
	}

	@Override
	public int getCount() {
		return mFileList.size();
	}

	@Override
	public Object getItem(int position) {
		return mFileList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}


	@Override
	public View getView(int position, View converterView, ViewGroup parentView) {		
		final ViewHolder viewHolder;
		if(converterView==null){
			viewHolder=new ViewHolder();	
			converterView =infalter.inflate(R.layout.item_gridview, null);
			viewHolder.imageview=(ImageView) converterView.findViewById(R.id.id_item_image);
			viewHolder.imagebutton=(ImageButton) converterView.findViewById(R.id.id_item_imgbutton);
			converterView.setTag(viewHolder);
		}else{
			viewHolder =(ViewHolder) converterView.getTag();

		}
		viewHolder.imageview.setColorFilter(null);	
		viewHolder.imageview.setImageResource(R.drawable.friends_sends_pictures_no);
		viewHolder.imagebutton.setImageResource(R.drawable.friends_sends_pictures_select_icon_unselected);

		final String fileFullPath=mImagePath+"/"+mFileList.get(position);
		viewHolder.imageview.setTag(fileFullPath);

		/*viewHolder.imageview.setOnMeasureListener(new OnMeasureListener() {

			@Override
			public void onMeasureSize(int width, int height) {
				// TODO Auto-generated method stub
				mPoint.set(width, height);
			}
		});*/

		Log.d(TAG, "position="+position);


		//ImageLoader2.getInstance(3, com.kisdy.imageloader.utils.Type.LIFO).loadImage(fileFullPath, viewHolder.imageview);
		ImageLoader.getInstance(3, com.kisdy.imageloader.utils.Type.LIFO).loadImage(fileFullPath, viewHolder.imageview);



		viewHolder.imageview.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				Log.d("ImageAdapter", fileFullPath);
				if(selectedImages.contains(fileFullPath)){
					selectedImages.remove(fileFullPath);
					viewHolder.imageview.setColorFilter(null);
					viewHolder.imagebutton.setImageResource(R.drawable.friends_sends_pictures_select_icon_unselected);
				}else{
					selectedImages.add(fileFullPath);
					viewHolder.imageview.setColorFilter(Color.parseColor("#77000000"));
					viewHolder.imagebutton.setImageResource(R.drawable.friends_sends_pictures_select_icon_selected);

				}
				//notifyDataSetChanged();
			}
		});
		
		if(selectedImages.contains(fileFullPath)){
			viewHolder.imageview.setColorFilter(Color.parseColor("#77000000"));
			viewHolder.imagebutton.setImageResource(R.drawable.friends_sends_pictures_select_icon_selected);
		}else{
			viewHolder.imageview.setColorFilter(null);
			viewHolder.imagebutton.setImageResource(R.drawable.friends_sends_pictures_select_icon_unselected);
		}

		return converterView;
	}

	class ViewHolder{
		ImageView imageview;
		ImageButton imagebutton;
	}

}
