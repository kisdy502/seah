package com.kisdy.weixin.view;

import com.kisdy.weixin.R;





import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * 自定义View实现滑动界面时底部图标和文件颜色渐变效果
 * View 的图片使用Canvas绘制，从而实现滑动颜色渐变
 * @author Administrator
 */
public class ChangeColorIconWithText extends View {
	
	private static final String INSTANCE_STATUS = "instance_status";
	private static final String STATUS_ALPHA = "status_alpha";


	/**
	 * 绘制图片的背景色(设置一个默认值，防止不传时发生异常,其它属性也是如此)
	 */
	private int mViewColor = 0xFF45C01A;

	/**
	 * 要绘制的文本
	 */
	private String mViewText = "微信";

	/**
	 * 绘制文本的字体大小
	 */
	private int mViewTextSize=(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics());


	/**
	 * 可以改变透明度的图片
	 */
	private Bitmap mCanvasBitmap;  
	/**
	 * 底部导航图片
	 */
	private Bitmap mIconBitmap;  

	/**
	 * 绘制图片要用到的画笔
	 */
	private Paint mImagePaint; 

	/**
	 * 计算文本要占据的位置
	 */
	private Rect mTextRect;

	/**
	 * 绘制文字要用到的画笔
	 */
	private Paint mTextPaint;

	/**
	 * 绘制图标的矩形区域
	 */
	private Rect mIconRect;

	/**
	 * 绘制图片要用到的画笔
	 */
	private Paint mIconPaint;
	/**
	 * 绘制可变色图层用到的画布
	 */
	private Canvas mIconCanvas;

	private float mAlpha;



	public void setmAlpha(float mAlpha) {
		if(mAlpha>1.0f)
			mAlpha=1;
		else if(mAlpha<0)
			mAlpha=0.0f;
		this.mAlpha = mAlpha;
		invalidateView();
	}

	private void invalidateView(){
			if(Looper.getMainLooper()==Looper.myLooper()){
				invalidate();
			}else{
				postInvalidate();
			}
	}

	public ChangeColorIconWithText(Context context) {
		this(context, null);
	}

	public ChangeColorIconWithText(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ChangeColorIconWithText(Context context, AttributeSet attrs,int defStyle) {	
		super(context, attrs,defStyle);

		initAttributes(context, attrs);		
	}

	/**
	 * 获取自定义属性
	 * @param context
	 * @param attrs
	 */
	private void initAttributes(Context context, AttributeSet attrs) {
		TypedArray array=context.obtainStyledAttributes(attrs,R.styleable.ChangeColorIconWithText);

		int arrayCount=array.getIndexCount();

		for (int i = 0; i < arrayCount; i++) {
			int attrid=array.getIndex(i);
			switch(attrid){
			case R.styleable.ChangeColorIconWithText_color:
				mViewColor=array.getColor(attrid, 0xFF45C01A);
				break;
			case R.styleable.ChangeColorIconWithText_icon:
				BitmapDrawable drawable =(BitmapDrawable) array.getDrawable(attrid);
				mIconBitmap=drawable.getBitmap();
				break;
			case R.styleable.ChangeColorIconWithText_text:
				mViewText=array.getString(attrid);
				break;
			case R.styleable.ChangeColorIconWithText_textsize:
				mViewTextSize=(int) array.getDimension(attrid, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
				break;
			}
		}
		array.recycle();      //释放所用内存

		caclateTextRect();
	}

	private void caclateTextRect(){
		mTextRect=new Rect();
		mTextPaint=new Paint();
		mTextPaint.setTextSize(mViewTextSize);
		mTextPaint.setColor(0Xff555555);
		mTextPaint.getTextBounds(mViewText, 0, mViewText.length(), mTextRect);
	}

	private void caclateDrawIconRect(){
		int	textHeight=mTextRect.height();
		int measureMaxWidth=getMeasuredWidth()-getPaddingLeft()-getPaddingRight(); 					//测量出来的可绘制图片的最大宽度
		int measureMaxHeight=getMeasuredHeight()-getPaddingTop()-getPaddingBottom()-textHeight;     //测量出来的可绘制图片的最大高
		int realIconWidth=Math.min(measureMaxWidth, measureMaxHeight);  //实际绘图的宽度和高度，图片是正方形的，去较小的值
		int left=getMeasuredWidth()/2-realIconWidth/2;
		int top=getMeasuredHeight()/2-realIconWidth/2-textHeight/2;

		mIconRect=new Rect(left, top, left+realIconWidth, top+realIconWidth);
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		caclateDrawIconRect();
	}


	/**
	 * 先绘制原图片，然后绘制纯色图片，然后设置Xfermode模式
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawBitmap(mIconBitmap, null, mIconRect, null);  //绘制底部图片，四个图标
		
		int alpha = (int) Math.ceil(255 * mAlpha);
		drawAlterableAlphaIcon(alpha);
		canvas.drawBitmap(mCanvasBitmap, 0, 0, null);
		
		drawSourceText(canvas,alpha);
		drawAlterableAlphaText(canvas,alpha);
	}

	/**
	 * 绘制透明度可变的图片
	 */
	void drawAlterableAlphaIcon(int alpha){
		mCanvasBitmap=Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Config.ARGB_8888);
		mIconCanvas=new Canvas(mCanvasBitmap);
		mIconPaint=new Paint();
		mIconPaint.setColor(mViewColor);
		mIconPaint.setAntiAlias(true);
		mIconPaint.setDither(true);
		mIconPaint.setAlpha(alpha);
		mIconCanvas.drawRect(mIconRect,mIconPaint);
		mIconPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		mIconPaint.setAlpha(255);
		mIconCanvas.drawBitmap(mIconBitmap,null,mIconRect,mIconPaint);
	}
	
	/**
	 * 绘制原来的文本
	 * @param canvas
	 * @param alpha
	 */
	void drawSourceText(Canvas canvas,int alpha){
		mTextPaint.setColor(0Xff555555);
		mTextPaint.setAlpha(255-alpha);
		int x=getMeasuredWidth()/2- mTextRect.width()/2;
		int y=mIconRect.bottom+mTextRect.height();
		canvas.drawText(mViewText, x, y, mTextPaint);
	}
	
	/**
	 * 绘制可变色文本
	 * @param canvas
	 * @param alpha
	 */
	void drawAlterableAlphaText(Canvas canvas,int alpha){
		mTextPaint.setColor(mViewColor);
		mTextPaint.setAlpha(alpha);
		int x = getMeasuredWidth() / 2 - mTextRect.width() / 2;
		int y = mIconRect.bottom + mTextRect.height();
		canvas.drawText(mViewText, x, y, mTextPaint);
	}
	
	
	@Override
	protected Parcelable onSaveInstanceState() {
		Bundle bundle = new Bundle();
		bundle.putParcelable(INSTANCE_STATUS, super.onSaveInstanceState());
		bundle.putFloat(STATUS_ALPHA, mAlpha);
		return bundle;
		//return super.onSaveInstanceState();
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state instanceof Bundle){
			Bundle bundle = (Bundle) state;
			mAlpha = bundle.getFloat(STATUS_ALPHA);
			super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATUS));
			return;
		}
		super.onRestoreInstanceState(state);
	}
	
}
