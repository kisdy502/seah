package com.kisdy.imageloder.activitys;


import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.kisdy.imageloder.R;
import com.kisdy.imageloder.activitys.SelectFilePopupWindow.OnDirSelectChangeListener;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	private  static final int SCAN_OK = 0x01;  //扫描OK
	private static final int CLOSE_POP=0x02;
	/**
	 * 存储文件夹中的图片数量
	 */
	private int mPicsSize;

	//private HashMap<String, ArrayList<String>> mGruopMap = new HashMap<String, ArrayList<String>>();  

	private GridView mGridView;
	private RelativeLayout mBottomLayout;
	private TextView tvFileName;
	private TextView tvFileCount;
	private View loadingView;
	private int mTotalCount;	
	private List<String> mImageList;



	ArrayList<FolderBean> mFolderBeanList;

	File mCurrentDirectory; //目录

	private  ImageAdapter mAdapter;

	private SelectFilePopupWindow popWindow;


	private Handler mHandler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			loadingView.setVisibility(View.GONE);
			if(msg.what==SCAN_OK){
				data2View();
				initPopupWindow();
			}
		};
	};

	private Handler closePopHanlder=new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(msg.what==CLOSE_POP){
				if(	popWindow.isShowing())
				{
					popWindow.dismiss();
				}
			}
		}
	};





	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		initView();
		initData();
		initEvent();
	}





	/**
	 * 控件初始化
	 */
	private void initView() {
		mGridView=	(GridView) findViewById(R.id.gv_photos);
		mBottomLayout=	(RelativeLayout) findViewById(R.id.rl_Bottom);
		tvFileName=	(TextView) findViewById(R.id.tv_pathName);
		tvFileCount=	(TextView) findViewById(R.id.tv_imageCount);
		loadingView = (RelativeLayout) findViewById(R.id.loading);
		mFolderBeanList=new ArrayList<FolderBean>();

	}

	/**
	 * 利用ContentProvider扫描手机中所有的图片
	 */
	private void initData() {
		// TODO Auto-generated method stub
		if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			//存储卡不可用
			Toast.makeText(this, R.string.str_sdcard_unmount, Toast.LENGTH_SHORT).show();
			return;
		}else{
			loadingView.setVisibility(View.VISIBLE);

			new Thread(){
				public void run() {

					FolderBean folderBean=null;
					HashSet<String> dirSet=new HashSet<String>();
					Uri mImageUri=MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
					ContentResolver mContentResolver=getContentResolver();
					Cursor cursor = mContentResolver.query(mImageUri, null,MediaStore.Images.Media.MIME_TYPE + "=? or "+ MediaStore.Images.Media.MIME_TYPE + "=?",new String[] { "image/jpeg", "image/png" },MediaStore.Images.Media.DATE_MODIFIED);

					if(cursor==null) return;

					while(cursor.moveToNext()){
						//图片全路径
						String path=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)); //图片路径
						//图片父路径，这里必定是一个目录
						File parentFile=new File(path).getParentFile(); 
						if(parentFile==null) continue;
						String parentDirectory=parentFile.getAbsolutePath();

						if(!dirSet.contains(parentDirectory)){
							dirSet.add(parentDirectory);
							folderBean=new FolderBean();
							folderBean.setDir(parentDirectory);
							folderBean.setFirstFilePath(path);
						}else{
							continue;
						}

						if(parentFile.list()==null)
							continue;
						else{
							int picLength=parentFile.list(new FilenameFilter() {
								@Override
								public boolean accept(File dir, String fileName) {
									if(fileName.endsWith(".jpg")||fileName.endsWith(".png")||fileName.endsWith(".jpeg"))
										return true;
									else
										return false;
								}
							}).length;
							mTotalCount+=picLength;

							folderBean.setFileCount(picLength);
							mFolderBeanList.add(folderBean);

							if(picLength>mPicsSize){
								mPicsSize=picLength;
								mCurrentDirectory=parentFile;
							}
						}


						/* 
					    String parentFileName=parentFile.getName();
					 	if(mGruopMap.containsKey(parentFileName)){
							ArrayList<String> childFileList=new ArrayList<String>();
							childFileList.add(path);
							mGruopMap.put(parentFileName, childFileList);
						}else{
							mGruopMap.get(parentFileName).add(path);
						}
						 */

					}  
					//扫描结束
					cursor.close();  
					mHandler.sendEmptyMessage(SCAN_OK);  
					//dirSet=null; //释放不用的内存,会自动回收
				};
			}.start();
		}
	}


	private void data2View(){
		if(mCurrentDirectory==null){	
			Toast.makeText(getApplicationContext(), R.string.str_no_photo, Toast.LENGTH_SHORT).show();
			return;
		}else{
			mImageList= Arrays.asList(mCurrentDirectory.list(new FilenameFilter() {	
				@Override
				public boolean accept(File dir, String filename) {
					if(filename.endsWith(".jpg")||filename.endsWith(".png")||filename.endsWith(".jpeg"))
						return true;
					else
						return false;
				}
			}));

			mAdapter=new ImageAdapter(getApplicationContext(), mCurrentDirectory.getAbsolutePath(), mImageList);
			mGridView.setAdapter(mAdapter);

			//tvFileCount.setText("0张");
			//tvFileName.setText(mCurrentDirectory.getAbsolutePath());
			tvFileName.setText(mCurrentDirectory.getAbsolutePath());
			tvFileCount.setText(mImageList.size()+"张");
		}
	}

	private void initPopupWindow(){
		//popWindow=new SelectFilePopupWindow();
		popWindow=new SelectFilePopupWindow(getApplicationContext(), mFolderBeanList,mCurrentDirectory.getAbsolutePath());
		popWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				// TODO Auto-generated method stub
				lightOn();
			}
		});

		popWindow.setOnDirSelectChangeListener(new OnDirSelectChangeListener() {
			@Override
			public void onSelected(FolderBean bean) {
				// TODO Auto-generated method stub
				if(bean.getDir().equals(mCurrentDirectory.getAbsolutePath())){
					popWindow.dismiss();
					return;
				}else{
					mCurrentDirectory=new File(bean.getDir());

					mImageList = Arrays.asList(mCurrentDirectory.list(new FilenameFilter() {	
						@Override
						public boolean accept(File dir, String filename) {
							if(filename.endsWith(".jpg")||filename.endsWith(".png")||filename.endsWith(".jpeg"))
								return true;
							else
								return false;
						}
					}));

					mAdapter=new ImageAdapter(getApplicationContext(), mCurrentDirectory.getAbsolutePath(), mImageList);
					mGridView.setAdapter(mAdapter);

					tvFileName.setText(mCurrentDirectory.getAbsolutePath());
					tvFileCount.setText(mImageList.size()+"张");

					closePopHanlder.sendEmptyMessageDelayed(CLOSE_POP,200);//通过handler发送延时消息来关闭PopWindow
				}

			}
		});
	}

	private void initEvent() {
		mBottomLayout.setOnClickListener(this);
	}


	void lightOff(){
		android.view.WindowManager.LayoutParams lp=	getWindow().getAttributes();
		lp.alpha=0.3f;
		getWindow().setAttributes(lp);
	}

	void lightOn(){
		android.view.WindowManager.LayoutParams lp=	getWindow().getAttributes();
		lp.alpha=1.0f;
		getWindow().setAttributes(lp);
	}



	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.rl_Bottom:
			popWindow.setAnimationStyle(R.style.anim_popup_dir);
			popWindow.showAsDropDown(mBottomLayout, 0, 0);
			lightOff();
			break;
		}
	}
}
