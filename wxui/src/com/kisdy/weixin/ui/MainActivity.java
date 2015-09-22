package com.kisdy.weixin.ui;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.kisdy.weixin.R;
import com.kisdy.weixin.view.ChangeColorIconWithText;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.Window;

public class MainActivity extends FragmentActivity implements OnClickListener, OnPageChangeListener {
	
	private static final String TAG = "MainActivity";
	private ViewPager mViewPager;
	private List<Fragment> mTabs = new ArrayList<Fragment>();
	private String[] mTitles = new String[]
	{ "First Fragment !", "Second Fragment !", "Third Fragment !",
			"Fourth Fragment !" };
	
	private FragmentPagerAdapter mAdapter;
	private List<ChangeColorIconWithText > mTabIndicators=new ArrayList<ChangeColorIconWithText>();
	
	@Override
	protected void onCreate(Bundle bundle) {
		
		super.onCreate(bundle);
		setContentView(R.layout.activity_main);
		ActionBar actionBar = getActionBar();  
		setOverflowButtonAlways();
		actionBar.setDisplayHomeAsUpEnabled(true);  //可点击
		actionBar.setDisplayShowHomeEnabled(false); //去掉ActionBar上面的AppIcon

		initView();
		initDatas();
		initEvent();
		
	}
	
	private void initView()
	{
		mViewPager = (ViewPager) findViewById(R.id.id_viewpager);
		ChangeColorIconWithText one = (com.kisdy.weixin.view.ChangeColorIconWithText) findViewById(R.id.id_indicator_1);
		mTabIndicators.add(one);
		ChangeColorIconWithText two = (ChangeColorIconWithText) findViewById(R.id.id_indicator_2);
		mTabIndicators.add(two);
		ChangeColorIconWithText three = (ChangeColorIconWithText) findViewById(R.id.id_indicator_3);
		mTabIndicators.add(three);
		ChangeColorIconWithText four = (ChangeColorIconWithText) findViewById(R.id.id_indicator_4);
		mTabIndicators.add(four);

		one.setOnClickListener(this);
		two.setOnClickListener(this);
		three.setOnClickListener(this);
		four.setOnClickListener(this);

		one.setmAlpha(1.0f);

	}
	
	void initDatas(){
		for (String title : mTitles)
		{
			TabFragment tabFragment = new TabFragment();
			Bundle bundle = new Bundle();
			bundle.putString(TabFragment.TITLE, title);
			tabFragment.setArguments(bundle);
			mTabs.add(tabFragment);
		}
		
		mAdapter=new FragmentPagerAdapter(getSupportFragmentManager()) {
			
			@Override
			public int getCount() {
				return mTabs.size();
			}
			
			@Override
			public Fragment getItem(int position) {
				return mTabs.get(position);
			}
		};
		mViewPager.setAdapter(mAdapter);
	}
	
	private void initEvent(){
		mViewPager.setOnPageChangeListener(this);
	}


	private void setOverflowButtonAlways() {
		ViewConfiguration config = ViewConfiguration.get(this);
		try {
			Field menuKey=ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			menuKey.setAccessible(true);
			menuKey.setBoolean(config, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();  
		inflater.inflate(R.menu.main, menu);  
		return true;  
		//return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		if(featureId==Window.FEATURE_ACTION_BAR && menu!=null){
			if(menu.getClass().getSimpleName().equals("MenuBuilder")){
				try {
					Method method=	menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
					method.setAccessible(true);
					method.invoke(menu, true);
				} catch (Exception e) {
				}
			}
		}
		return super.onMenuOpened(featureId, menu);
	}

	int mCurrentChoosePageIndex=0;
	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.id_indicator_1:
			changedViewPageIndex(0);
			break;
		case R.id.id_indicator_2:
			changedViewPageIndex(1);
			break;
		case R.id.id_indicator_3:
			changedViewPageIndex(2);
			break;
		case R.id.id_indicator_4:
			changedViewPageIndex(3);
			break;
		}		
	}
	
	
	void changedViewPageIndex(int toIndex){
		Log.d(TAG, "changedViewPageIndex --->mCurrentChoosePageIndex="+mCurrentChoosePageIndex);
		if(mCurrentChoosePageIndex!=toIndex){
			mTabIndicators.get(mCurrentChoosePageIndex).setmAlpha(0.0f);
			mCurrentChoosePageIndex=toIndex;
			mTabIndicators.get(mCurrentChoosePageIndex).setmAlpha(1.0f);
			mViewPager.setCurrentItem(mCurrentChoosePageIndex,false);
		}
	}
	
	void resetOtherTabs(){
		for (int i = 0; i < mTabIndicators.size(); i++) {
			mTabIndicators.get(i).setmAlpha(0.0f);
		}
	}

	
	
	@Override
	public void onPageScrollStateChanged(int state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		// TODO Auto-generated method stub	
		if (positionOffset > 0){
			ChangeColorIconWithText left_view=mTabIndicators.get(position);
			ChangeColorIconWithText right_view=mTabIndicators.get(position+1);
			left_view.setmAlpha(1-positionOffset);
			right_view.setmAlpha(positionOffset);
		}		
	}

	@Override
	public void onPageSelected(int position) {
		mCurrentChoosePageIndex=position;
		Log.d(TAG, "onPageSelected --->mCurrentChoosePageIndex="+mCurrentChoosePageIndex);
		
	}
}
