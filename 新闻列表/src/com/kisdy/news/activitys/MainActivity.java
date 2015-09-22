package com.kisdy.news.activitys;

/***
 * 一个简单的ListView加载数据时优化用户体验的程序
 * 1，使用LruCache缓存了图片信息
 * 2.在滚动时不加载图片，停止滚动时加载图片
 * 
 * 
 * 进一步优化 ，使用磁盘缓存缓存图片
 * 进入主界面时，请求网络时显示ProgrossBar
 * 
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kisdy.news.R;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";

	private ListView mListView;

	NewsAdapter mAdapter;
	ArrayList<NewBean> beanList;

	String serverurl="http://www.imooc.com/api/teacher?type=4&num=30"; //服务器资源URL

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		beanList=new ArrayList<NewBean>();
		mListView=(ListView) findViewById(R.id.lv_NewsList);
		new MyAsyncTask().execute(serverurl);
	}

	class MyAsyncTask extends AsyncTask<String,Void,ArrayList<NewBean>>{

		@Override
		protected ArrayList<NewBean> doInBackground(String... url) {
			ArrayList<NewBean> data=getJsonData(url[0]);
			return data;
		}	

		@Override
		protected void onPostExecute(ArrayList<NewBean> result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			mAdapter=new NewsAdapter(getApplicationContext(),result,mListView);
			mListView.setAdapter(mAdapter);
		}
	}

	private String readStream(InputStream is){
		StringBuffer sb=new StringBuffer();
		String line="";
		try {
			InputStreamReader isReader=new InputStreamReader(is,"UTF-8");
			BufferedReader bufferReader=new BufferedReader(isReader);
			while((line=bufferReader.readLine())!=null){
				sb.append(line);	
			}		     
			bufferReader.close();
			isReader.close();
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Log.d(TAG, sb.toString());
		return sb.toString();
	}


	private ArrayList<NewBean> getJsonData(String url){
		try {
			String jsonString=readStream(new URL(url).openStream());
			JSONObject jobj;
			NewBean bean;
			int jArrayLen=0;

			jobj=new JSONObject(jsonString);
			JSONArray jarray=jobj.getJSONArray("data");
			jArrayLen=jarray.length();
			for (int i = 0; i < jArrayLen; i++) {
				bean=new NewBean();
				JSONObject obj=	jarray.getJSONObject(i);
				bean.setNewImageUrl(obj.getString("picSmall"));
				bean.setNewTile(obj.getString("name"));
				bean.setNewContent(obj.getString("description"));
				beanList.add(bean);
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return beanList;
	}


}
