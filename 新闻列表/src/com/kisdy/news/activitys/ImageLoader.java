package com.kisdy.news.activitys;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

import com.kisdy.news.R;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.widget.ImageView;
import android.widget.ListView;

public class ImageLoader {

	private ListView mListView;

	public static HashSet<MyTask> mTask;

	ImageView mImageView;

	String mUrl;

	ArrayList<NewBean> beanList;

	private LruCache<String,Bitmap> mCache;

	private static ImageLoader imageLoader;

	public static ImageLoader getInstance(ListView mListView){
		if(imageLoader==null)
			imageLoader=new ImageLoader(mListView);
		return imageLoader;
	}

	private  ImageLoader(ListView listview){

		int maxMemory=(int) Runtime.getRuntime().maxMemory(); //最大可用内存
		int cacheSize=maxMemory/4;  						  //作为图片缓存大小
		mCache=new LruCache<String,Bitmap>(cacheSize){
			@SuppressLint("NewApi") @Override
			protected int sizeOf(String key, Bitmap value) {
				// TODO Auto-generated method stub
				if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB_MR1){
					return value.getByteCount();
				}else{
					return value.getRowBytes()*value.getHeight();
				}
			}
		};

		mTask=new HashSet<MyTask>();
		mListView=listview;
	}

	void  addBitmapToCache(String key,Bitmap bitmap){
		if(mCache.get(key)==null){
			mCache.put(key,bitmap);
		}
	}

	Bitmap getBitmapFromCache(String key){
		return mCache.get(key);
	}

	/**
	 * 使用多线程异步加载图片
	 * @param url
	 * @param imageView
	 */
	public void loadImgFromUrlByThread(final String url,final ImageView imageView){
		mImageView=imageView;
		mUrl=url;
		new Thread(new Runnable() {
			public void run() {
				Bitmap bmp=	getImagefromUrl(url,imageView);
				Message message=Message.obtain();	
				message.obj=bmp;
				handler.sendMessage(message);
			}
		}).start();
	}


	private Handler handler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(mImageView.getTag().equals(mUrl)){
				mImageView.setImageBitmap((Bitmap)msg.obj);
			}
		};
	};


	private Bitmap getImagefromUrl(String url,ImageView imageView){
		Bitmap bmp = null;
		try {
			URL mUrl=new URL(url);	
			HttpURLConnection connection = (HttpURLConnection) mUrl.openConnection();
			InputStream is=connection.getInputStream();
			bmp=BitmapFactory.decodeStream(is);
			is.close();
			connection.disconnect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bmp;


	}

	/**
	 * 使用AsyncTask异步加载图片
	 * @param url
	 * @param iv
	 */
	public void loadImgFromUrlByAsyncTask(String url,ImageView iv){
		Bitmap bitmap=getBitmapFromCache(url);
		if(bitmap==null){
			//new MyTask(iv).execute(url);
			iv.setImageResource(R.drawable.ic_launcher);
		}else{
			iv.setImageBitmap(bitmap);
		}
	}

	private class MyTask extends AsyncTask<String, Void, Bitmap>{

		//private ImageView mInnerImageView;
		private String imUrl;
		public MyTask(String url)
		{
			//mInnerImageView=ivicon;
			imUrl=url;
		}

		@Override
		protected Bitmap doInBackground(String... parms) {
			//mImageView=mInnerImageView;
			ImageView imgView=	(ImageView) mListView.findViewWithTag(imUrl);
			//imUrl=parms[0];
			Bitmap bitmap= getImagefromUrl(imUrl,imgView);
			addBitmapToCache(imUrl, bitmap);	
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {			
			ImageView imgView=	(ImageView) mListView.findViewWithTag(imUrl);
			if(imgView!=null&&bitmap!=null){
				imgView.setImageBitmap(bitmap);
			}
			mTask.remove(this);
		}

	}


	/**
	 * 下载图片任务
	 * @param start
	 * @param end
	 * @param urls
	 */
	public void  downloadImages(int start ,int end){
		for(int i=start;i<end;i++){
			String strUrl=NewsAdapter.mUrls[i];
			Bitmap bmp=getBitmapFromCache(strUrl);
			if(bmp==null){                          //缓存中没有,前往下载
				MyTask task=new MyTask(strUrl);
				mTask.add(task);
				task.execute(strUrl);		
			}else{                                  //缓存中有，取缓存的图片
				ImageView imgView=(ImageView) mListView.findViewWithTag(strUrl);	
				if(imgView!=null){
					imgView.setImageBitmap(bmp);
				}

			}
		}
	}

	public static void cancelAllTask() {
		if(mTask!=null){
			for(MyTask task :mTask){
				task.cancel(true);
			}
		}
	}

}
