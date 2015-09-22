package com.kisdy.imageloader.utils;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;

public class ImageLoader {
	/**
	 * 图片缓存
	 */
	private LruCache<String, Bitmap> mLruCache;

	/**
	 * 线程池
	 */
	private ExecutorService mThreadPool;
	/**
	 * 默认线程池线程数量
	 */
	private final static int DEFAULT_THREAD_COUNT=1;

	private final static int MSG_ID=0x100;

	private static final String TAG = "ImageLoader";

	private int mThreadCount;
	

	private Type defaultType=Type.LIFO;   

	/**
	 * 任务队列 可以从头和尾取到集合中的对象
	 */
	private LinkedList<Runnable> mTaskQueue;

	/**
	 * 后台轮询线程
	 */
	private Thread mPoolThread;
	/**
	 * 处理轮询线程的消息
	 */
	private Handler mPoolThreadHanlder;

	/**
	 * UI线程中的Handler,用于处理UI消息
	 */
	private Handler mUIHandler;

	/**
	 * 信号量，同步轮询线程使用到
	 */
	private Semaphore mSemaophoreThreadPoolHandler=new Semaphore(0);

	/**
	 * 信号量，同步任务队列
	 */
	private volatile Semaphore mSemaophoreforTaskQueue;


	private static ImageLoader imgLoader;
	/**
	 * 单例模式
	 * @return
	 */
	public static ImageLoader getInstance(int threadCount,Type queueType){
		if(imgLoader==null){
			synchronized(ImageLoader.class){
				if(imgLoader==null){
					imgLoader=new ImageLoader(threadCount,queueType);
				}
			}
		}
		return imgLoader;
	}

	public static ImageLoader getInstance(){
		return getInstance(DEFAULT_THREAD_COUNT,Type.LIFO);
	}

	private ImageLoader (int threadCount,Type queueType){	
		init(threadCount,queueType);
	}

	private ImageLoader(){
		new ImageLoader(DEFAULT_THREAD_COUNT,Type.LIFO);
	}

	/**
	 * 数据初始化
	 */
	private void init(int threadCount,Type queueType){

		this.mThreadCount=threadCount;
		this.defaultType=queueType!=null?queueType:Type.LIFO;		

		/**
		 * 后台轮询线程
		 */
		mPoolThread=new Thread(){
			@Override
			public void run() {
				Looper.prepare();
				mPoolThreadHanlder=new Handler(){
					public void handleMessage(Message msg) {
						mThreadPool.execute(getTask());     						//线程池取出一个任务进行执行
						try {
							mSemaophoreforTaskQueue.acquire();  //线程池任务到达一定数量后阻塞线程
						} catch (Exception e) {
						}						
					};
				};
				
				mSemaophoreThreadPoolHandler.release(); //释放信号量，告诉其它线程中使用了mPoolThreadHanlder变量的线程对象已经初始化完成
				Looper.loop();
				
			}
		};
		mPoolThread.start();


		//获取应用最大内存(byte)
		int maxMemory=	(int) Runtime.getRuntime().maxMemory();
		int cacheSize=maxMemory/8;

		/**
		 * 初始化LruCache
		 */
		mLruCache=new LruCache<String, Bitmap>(cacheSize){
			@SuppressLint("NewApi")
			@Override
			protected int sizeOf(String key, Bitmap value) {
				if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN_MR1){
					return value.getByteCount();
				}
				else{
					return value.getHeight()*value.getRowBytes();
				}
			}
		};

		/**
		 * 线程池初始化
		 */
		mThreadPool=Executors.newFixedThreadPool(threadCount);
		/**
		 * 初始化任务队列
		 */
		mTaskQueue=new LinkedList<Runnable>();

		mSemaophoreforTaskQueue=new Semaphore(threadCount);

	}

	/**
	 * 核心方法，加载图片
	 * @param path
	 * @param imageView
	 */
	public void loadImage(final String path,final ImageView imageView){
		imageView.setTag(path);    //防止ImageView在复用错乱问题

		if(mUIHandler==null){
			mUIHandler=new Handler(){
				@Override
				public void handleMessage(Message msg) {
					ImageHolder holder=(ImageHolder) msg.obj;
					ImageView objImageView=holder.mImageView;
					String objPath=holder.mPath;
					Bitmap bmp=holder.mBitmap;
					if(objImageView.getTag().toString().equals(objPath)){
						objImageView.setImageBitmap(bmp);
					}
					//super.handleMessage(msg);
				}
			};
		}


		Bitmap bitmap=getBitmapFromLruCache(path);

		if(bitmap==null){     //缓存中没有图片时，开启线程异步取获取本机存储图片
			addTask(new Runnable(){			
				@Override
				public void run() {
					//获取图片
					ImageSize imgSize= getImageWidth(imageView);
					//压缩图片
					Bitmap bmp=decodeImageFromPath(path,imgSize.mImageWidth,imgSize.mImageHeight);
					//将图片加入到缓存
					addBitmapToLruCache(bmp,path);
					//refreshMessage
					refreshMessage(path,imageView,bmp);

					mSemaophoreforTaskQueue.release();  //当任务完成后释放信号，告诉线程池，当前线程已经执行完成
				}			
			});
		}else{
			refreshMessage(path, imageView, bitmap);
		}
	}



	private void refreshMessage(final String path, final ImageView imageView,final Bitmap bitmap) {	
		Message message=Message.obtain();
		ImageHolder holder=new ImageHolder();
		holder.mImageView=imageView;
		holder.mPath=path;
		holder.mBitmap=bitmap;
		message.obj=holder;
		mUIHandler.sendMessage(message);
	}


	/**
	 * 将图片加入缓存
	 * @param bmp
	 * @param path
	 */
	protected void addBitmapToLruCache(Bitmap bmp, String path) {
		if(mLruCache.get(path)==null )
		{
			if(bmp!=null)
				mLruCache.put(path, bmp);
		}
		Log.d(TAG,"mLruCache已经使用"+ mLruCache.size()+"字节");
	}

	/**
	 * 从缓存拿图片
	 * @param path
	 * @return
	 */
	private Bitmap getBitmapFromLruCache(String path) {
		// TODO Auto-generated method stub
		return  mLruCache.get(path);
	}


	/**
	 * 获取图片
	 * @param path
	 * @param mImageWidth
	 * @param mImageHeight
	 * @return
	 */
	protected Bitmap decodeImageFromPath(String path, int mImageWidth,int mImageHeight) {
		BitmapFactory.Options options=new BitmapFactory.Options();
		options.inJustDecodeBounds=true;//设置为true，在加载图片时仅加载图片头文件信息
		BitmapFactory.decodeFile(path,options);
		options.inSampleSize=caculateSampleSizeFromOption(options,mImageWidth,mImageHeight);  //计算缩放比例,直接加载可能导致OOM
		options.inJustDecodeBounds=false;
		Bitmap bitmap=BitmapFactory.decodeFile(path,options);
		//Log.d(TAG, "-->"+path);
		return bitmap;
	}

	/**
	 * 计算图片压缩比例
	 * @param options
	 * @param mImageWidth 	展示时的宽度
	 * @param mImageHeight  展示时的高度
	 * @return
	 */
	private int caculateSampleSizeFromOption(Options options, int mImageWidth,
			int mImageHeight) {
		int width=options.outWidth;  //实际图片宽
		int height=options.outHeight;
		int inSampleSize=1;

		int scaleX=width/mImageWidth;
		int scaleY=height/mImageHeight;
		

		inSampleSize=Math.max(scaleX, scaleY);
		if(inSampleSize<1)
			inSampleSize=1;
		
		//Log.d(TAG, "inSampleSize="+inSampleSize);
		return inSampleSize;
	}

	/**
	 * 获取ImageView的宽和高
	 * @return 
	 */
	@SuppressLint("NewApi")
	private ImageSize getImageWidth(ImageView imageView){
		LayoutParams lp=imageView.getLayoutParams();
		WindowManager wm= (WindowManager) imageView.getContext().getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics=new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);

		int width=imageView.getWidth();  
		int height=imageView.getHeight();

		if(width<=0){
			width=lp.width;   //ImageView在Xml布局中声明的宽度
		}
		if(width<=0){
			//width=imageView.getMaxWidth();  //Api 16
			width=getImageViewFiledValueByFieldName(imageView, "mMaxWidth"); //此处改成用反射获取
		}
		if(width<=0){
			width=outMetrics.widthPixels/3;   //获取屏幕宽度   因为屏幕每行可以显示三张
		}

		if(height<=0){
			height=lp.height;   //ImageView在Xml布局中声明的宽度
		}
		if(height<=0){
			//height=imageView.getMaxHeight();   //Api 16 
			height=getImageViewFiledValueByFieldName(imageView, "mMaxHeight"); //此处改成用反射获取
		}
		if(height<=0){
			height=outMetrics.heightPixels;     
		}
		return new ImageSize(width,height);
	}

	private synchronized void addTask(Runnable runnable) {



		try {
			if(mPoolThreadHanlder==null){
				mSemaophoreThreadPoolHandler.acquire();   //等到信号通知
			}
		} catch (InterruptedException e) {	
		}
		mTaskQueue.add(runnable);
		mPoolThreadHanlder.sendEmptyMessage(MSG_ID);  //线程池Handler


	}

	private synchronized Runnable getTask(){
		if(defaultType==Type.FIFO){
			return mTaskQueue.removeFirst();
		}else if(defaultType==Type.LIFO){
			return mTaskQueue.removeLast();
		}
		return null;
	}


	class ImageHolder{
		Bitmap mBitmap;
		ImageView mImageView;
		String mPath;
	}

	class ImageSize{
		//存放图片宽高
		public ImageSize(int h,int w){
			mImageHeight=h;
			mImageWidth=w;
		}
		public int mImageHeight;   
		public int mImageWidth;
	}

	/**
	 * 反射获取对象的某个字段值
	 * @param obj
	 * @param fieldName
	 * @return
	 */
	private int getImageViewFiledValueByFieldName(Object obj,String fieldName){
		int val=0;
		try {
			Field field=ImageView.class.getDeclaredField(fieldName);
			field.setAccessible(true);
			int fieldValue=field.getInt(obj);
			if(fieldValue>=0 && fieldValue<Integer.MAX_VALUE){
				val =fieldValue;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return val;
	}

	public interface ImageLoadCallBack {
		public void onImageLoadedListener(Bitmap bitmap, String path);
	}
}
