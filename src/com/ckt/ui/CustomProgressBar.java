package com.ckt.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.ckt.modle.LogUtil;



/**
 * Created by mertsimsek on 17/07/15.
 */
public class CustomProgressBar extends View {

	private float downX;
	private float downY;
	private float downMinR;  //down点击事件有效的最小半径
	private float downMaxR;  //down点击事件有效的最大半径
	private boolean isDown = false;
	private OnCircleProgressBarDragListener dragListener;  //拖拽进度条时的回调监听
	private boolean isDraging=false;
    /**
     * Rect for get time height and width
     */
    private Rect mRectText;

    /**
     * Paint for drawing left and passed time.
     */
    private Paint mPaintTime;

    /**
     * RectF for draw circle progress.
     */
    private RectF rectF;

    /**
     * Paint for circle progress left
     */
    private Paint mPaintProgressEmpty;

    /**
     * Paint for circle progress loaded
     */
    private Paint mPaintProgressLoaded;

    /**
     * Modified OnClickListener. We do not want all view click.
     * notify onClick() only button area touched.
     */
    private OnClickListener onClickListener;

    /**
     * Button paint for play/pause control button
     */
    private Paint mPaintButton;

    /**
     * Play/Pause button region for handle onTouch
     */
    private Region mButtonRegion;

    /**
     * Play icon will be converted to Bitmap
     */
//    private Bitmap mBitmapPlay;

    /**
     * Pause icon will be converted to Bitmap
     */
//    private Bitmap mBitmapPause;

    /**
     * Paint for drawing play/pause icons to canvas.
     */
    private Paint mPaintPlayPause;

    /**
     * Paint to draw cover photo to canvas
     */
    private Paint mPaintCover;

    /**
     * Bitmap for shader.
     */
    private Bitmap mBitmapCover;

    /**
     * Shader for make drawable circle
     */
    private BitmapShader mShader;

    /**
     * Scale image to view width/height
     */
    private float mCoverScale;

    /**
     * Image Height and Width values.
     */
    private int mHeight;
    private int mWidth;

    /**
     * Center values for cover image.
     */
    private float mCenterX;
    private float mCenterY;

    /**
     * Cover image is rotating. That is why we hold that value.
     */
    private int mRotateDegrees;

    /**
     * Handler for posting runnable object
     */
    private Handler mHandlerRotate;

    /**
     * Runnable for turning image (default velocity is 10)
     */
    private Runnable mRunnableRotate;

    /**
     * Handler for posting runnable object
     */
    private Handler mHandlerProgress;

    /**
     * Runnable for turning image (default velocity is 10)
     */
    private Runnable mRunnableProgress;

    /**
     * isRotating
     */
    private boolean isRotating;

    /**
     * Handler will post runnable object every @ROTATE_DELAY seconds.
     */
    private static int ROTATE_DELAY = 100;

    /**
     * 1 sn = 1000 ms
     */
    private static int PROGRESS_SECOND_MS = 1000;

    /**
     * mRotateDegrees count increase 1 by 1 default.
     * I used that parameter as velocity.
     */
    private static int VELOCITY = 1;

    /**
     * Default color code for cover
     */
    private int mCoverColor = Color.GRAY;

    /**
     * Play/Pause button radius.(default = 120)
     */
    private float mButtonRadius = 120f;

    /**
     * Play/Pause button color(Default = dark gray)
     */
    private int mButtonColor = Color.DKGRAY;

    /**
     * Color code for progress left.
     */
//    private int mProgressEmptyColor = 0x20FFFFFF;
    private int mProgressEmptyColor = 0x6ccc;
    /**
     * Color code for progress loaded.
     */
    private int mProgressLoadedColor = 0xFF00815E;

    /**
     * Time text size
     */
    private int mTextSize = 40;

    /**
     * Default text color
     */
    private int mTextColor = 0xFFFFFFFF;

    /**
     * Current progress value
     */
    private int currentProgress = 0;

    /**
     * Max progress value
     */
//    歌曲总长度
    private int maxProgress = 10;

    /**
     * Auto progress value start progressing when
     * cover image start rotating.
     */
    private boolean isAutoProgress = true;

    /**
     * Progressview and time will be visible/invisible depends on this
     */
    private boolean mProgressVisibility = true;

    /**
     * Constructor
     *
     * @param context
     */
    private boolean state = false;
    public CustomProgressBar(Context context) {
        super(context);
        init(context, null);
    }

    /**
     * Constructor
     *
     * @param context
     * @param attrs
     */
    public CustomProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    /**
     * Constructor
     *
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public CustomProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * Constructor
     *
     * @param context
     * @param attrs
     * @param defStyleAttr
     * @param defStyleRes
     */
 

    /**
     * Initializes resource values, create objects which we need them later.
     * Object creation must not called onDraw() method, otherwise it won't be
     * smooth.
     *
     * @param context
     * @param attrs
     */
    private void init(Context context, AttributeSet attrs) {

        //Get Image resource from xml
//        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.playerview);
//        Drawable mDrawableCover = a.getDrawable(R.styleable.playerview_cover);
//        if (mDrawableCover != null)
//            mBitmapCover = drawableToBitmap(mDrawableCover);
//
//        mButtonColor = a.getColor(R.styleable.playerview_buttonColor, mButtonColor);
//        mProgressEmptyColor = a.getColor(R.styleable.playerview_progressEmptyColor, mProgressEmptyColor);
//        mProgressLoadedColor = a.getColor(R.styleable.playerview_progressLoadedColor, mProgressLoadedColor);
//        mTextColor = a.getColor(R.styleable.playerview_textColor, mTextColor);
//        mTextSize = a.getDimensionPixelSize(R.styleable.playerview_textSize, mTextSize);
//        a.recycle();

        mRotateDegrees = 0;

        //Handler and Runnable object for turn cover image by updating rotation degrees
        mHandlerRotate = new Handler();

        mRunnableRotate = new Runnable() {
            @Override
            public void run() {
                if (isRotating) {

                    if(currentProgress > maxProgress){
                        currentProgress = 0;
                        setProgress(currentProgress);
                        stop();
                    }

                    updateCoverRotate();
                    mHandlerRotate.postDelayed(mRunnableRotate, ROTATE_DELAY);
                }
            }
        };

        //Handler and Runnable object for progressing.
        mHandlerProgress = new Handler();

        mRunnableProgress = new Runnable() {
            @Override
            public void run() {
                if(isRotating){
                    currentProgress += 1;
                    mHandlerProgress.postDelayed(mRunnableProgress, PROGRESS_SECOND_MS);
                }
            }
        };

        //Play/Pause button circle paint
        mPaintButton = new Paint();
        mPaintButton.setAntiAlias(true);
        mPaintButton.setStyle(Paint.Style.FILL);
        mPaintButton.setColor(mButtonColor);

        //Play/Pause button icons paint and bitmaps
        mPaintPlayPause = new Paint();
        mPaintPlayPause.setAntiAlias(true);
//        mBitmapPlay = BitmapFactory.decodeResource(getResources(), R.drawable.icon_play);
//        mBitmapPause = BitmapFactory.decodeResource(getResources(), R.drawable.icon_pause);

        //Progress paint object creation
        mPaintProgressEmpty = new Paint();
        mPaintProgressEmpty.setAntiAlias(true);
        mPaintProgressEmpty.setColor(mProgressEmptyColor);
        mPaintProgressEmpty.setStyle(Paint.Style.STROKE);
        mPaintProgressEmpty.setStrokeWidth(12.0f);

        mPaintProgressLoaded = new Paint();
        mPaintProgressEmpty.setAntiAlias(true);
        mPaintProgressLoaded.setColor(mProgressLoadedColor);
        mPaintProgressLoaded.setStyle(Paint.Style.STROKE);
        mPaintProgressLoaded.setStrokeWidth(12.0f);

        mPaintTime = new Paint();
        mPaintTime.setColor(mTextColor);
        mPaintTime.setAntiAlias(true);
        mPaintTime.setTextSize(mTextSize);

        //rectF and rect initializes
        rectF = new RectF();
        mRectText = new Rect();

    }

    /**
     * Calculate mWidth, mHeight, mCenterX, mCenterY values and
     * scale resource bitmap. Create shader. This is not called multiple times.
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    
    public void setMaxProgress(int time)
    {
    	setMaxProgress(time);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);

        int minSide = Math.min(mWidth,mHeight);
        mWidth = minSide;
        mHeight = minSide;

        this.setMeasuredDimension(mWidth, mHeight);

        mCenterX = mWidth / 2f;
        mCenterY = mHeight / 2f;

        //set RectF left, top, right, bottom coordiantes
        rectF.set(20.0f, 20.0f, mWidth - 20.0f, mHeight - 20.0f);

        //button size is about to 1/4 of image size then we divide it to 8.
        mButtonRadius = mWidth / 8.0f;

        //We resize icons with button radius. icons need to be inside circle.
//        mBitmapPlay = getResizedBitmap(mBitmapPlay, mButtonRadius - 20.0f, mButtonRadius - 20.0f);
//        mBitmapPause = getResizedBitmap(mBitmapPause, mButtonRadius - 20.0f, mButtonRadius - 20.0f);

        mButtonRegion = new Region((int) (mCenterX - mButtonRadius),
                (int) (mCenterY - mButtonRadius),
                (int) (mCenterX + mButtonRadius),
                (int) (mCenterY + mButtonRadius));

        createShader();
        
        //计算一下点击事件有效的范围
        downMaxR = rectF.width()/2+mPaintProgressEmpty.getStrokeWidth()+15;
        downMinR = rectF.width()/2-30;

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * This is where magic happens as you know.
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        LogUtil.v("MusicPlayerService", "circleUI:onDraw-->max:"+maxProgress+" current:"+currentProgress);
        if (mShader == null)
            return;
        if(mProgressVisibility){
            //Draw empty progress
            canvas.drawArc(rectF, 145, calculatePastProgressDegree(), false, mPaintProgressEmpty);

            //Draw loaded progress
            canvas.drawArc(rectF, 145, calculatePastProgressDegree(), false, mPaintProgressLoaded);
            if(isRotating == true)
            {
            //Draw left time text
            	String leftTime = secondsToTime(calculateLeftSeconds());
            	mPaintTime.getTextBounds(leftTime, 0, leftTime.length(), mRectText);
            
            	canvas.drawText(leftTime,
                    (float) (mCenterX * Math.cos(Math.toRadians(35.0))) + mWidth / 2.0f - mRectText.width() / 1.5f,
                    (float) (mCenterX * Math.sin(Math.toRadians(35.0))) + mHeight / 2.0f + mRectText.height() + 15.0f,
                    mPaintTime);

            //Draw passed time text
            	String passedTime = secondsToTime(calculatePassedSeconds());
            	mPaintTime.getTextBounds(passedTime, 0, passedTime.length(), mRectText);

            	canvas.drawText(passedTime,
                    (float) (mCenterX * -Math.cos(Math.toRadians(35.0))) + mWidth / 2.0f - mRectText.width() / 3.0f,
                    (float) (mCenterX * Math.sin(Math.toRadians(35.0))) + mHeight / 2.0f + mRectText.height() + 15.0f,
                    mPaintTime);
            }

        }
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    /**
     * Create shader and set shader to mPaintCover
     */
    private void createShader() {

        if (mWidth == 0)
            return;

        //if mBitmapCover is null then create default colored cover
        if (mBitmapCover == null) {
            mBitmapCover = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            mBitmapCover.eraseColor(mCoverColor);
        }

        mCoverScale = ((float) mWidth) / (float) mBitmapCover.getWidth();

        mBitmapCover = Bitmap.createScaledBitmap(mBitmapCover,
                (int) (mBitmapCover.getWidth() * mCoverScale),
                (int) (mBitmapCover.getHeight() * mCoverScale),
                true);

        mShader = new BitmapShader(mBitmapCover, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        mPaintCover = new Paint();
        mPaintCover.setAntiAlias(true);
        mPaintCover.setShader(mShader);

    }

    /**
     * Update rotate degree of cover and invalide onDraw();
     */
    public void updateCoverRotate() {
        mRotateDegrees += VELOCITY;
        mRotateDegrees = mRotateDegrees % 360;
        postInvalidate();
    }

    /**
     * Checks is rotating
     *
     * @return
     */
    public boolean isRotating() {
        return isRotating;
    }

    /**
     * Start turning image
     */
    public void start() {
        isRotating = true;
        state = true;
        mHandlerRotate.removeCallbacksAndMessages(null);
        mHandlerRotate.postDelayed(mRunnableRotate, ROTATE_DELAY);
        if(isAutoProgress){
            mHandlerProgress.removeCallbacksAndMessages(null);
            mHandlerProgress.postDelayed(mRunnableProgress, PROGRESS_SECOND_MS);
        }
    }

    /**
     * Stop turning image
     */
    public void stop() {
        isRotating = false;
    }

    /**
     * Set velocity.When updateCoverRotate() method called,
     * increase degree by velocity value.
     *
     * @param velocity
     */
    public void setVelocity(int velocity) {
        if (velocity > 0)
            VELOCITY = velocity;
    }

    /**
     * set cover image resource
     *
     * @param coverDrawable
     */
//    public void setCoverDrawable(int coverDrawable) {
//        Drawable drawable = getContext().getDrawable(coverDrawable);
//        mBitmapCover = drawableToBitmap(drawable);
//        createShader();
//        postInvalidate();
//    }

    /**
     * gets image URL and load it to cover image.It uses Picasso Library.
     *
     * @param imageUrl
     */
   
    /**
     * onClickListener.onClick will be called when button clicked.
     * We dont want all view click. We only want button area click.
     * That is why we override it.
     *
     * @param l
     */
    @Override
    public void setOnClickListener(OnClickListener l) {
        onClickListener = l;
    }

    /**
     * Resize bitmap with @newHeight and @newWidth parameters
     *
     * @param bm
     * @param newHeight
     * @param newWidth
     * @return
     */
    private Bitmap getResizedBitmap(Bitmap bm, float newHeight, float newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    /**
     * Sets button color
     *
     * @param color
     */
    public void setButtonColor(int color) {
        mButtonColor = color;
        mPaintButton.setColor(mButtonColor);
        postInvalidate();
    }

    /**
     * sets progress empty color
     * @param color
     */
    public void setProgressEmptyColor(int color){
        mProgressEmptyColor = color;
        mPaintProgressEmpty.setColor(mProgressEmptyColor);
        postInvalidate();
    }

    /**
     * sets progress loaded color
     * @param color
     */
    public void setProgressLoadedColor(int color){
        mProgressLoadedColor = color;
        mPaintProgressLoaded.setColor(mProgressLoadedColor);
        postInvalidate();
    }

    /**
     * Sets total seconds of music
     * @param maxProgress
     */
    public void setMax(int maxProgress){
        this.maxProgress = maxProgress;
        postInvalidate();
    }

    /**
     * Sets current seconds of music
     * @param currentProgress
     */
    public void setProgress(int currentProgress){
        if(isDraging) return;
        if(currentProgress<0) {
        	this.currentProgress = 0;
        }else if(currentProgress>maxProgress) {
        	this.currentProgress = maxProgress;
        }else {
        	this.currentProgress = currentProgress;
        }
        postInvalidate();
    }

    /**
     * Get current progress seconds
     * @return
     */
    public int getProgress(){
        return currentProgress;
    }

    /**
     * Calculate left seconds
     * @return
     */
    private int calculateLeftSeconds(){
        return maxProgress - currentProgress;
    }

    /**
     * Return passed seconds
     * @return
     */
    private int calculatePassedSeconds(){
        return currentProgress;

    }

    /**
     * Convert seconds to time
     * @param seconds
     * @return
     */
    private String secondsToTime(int seconds){
        String time = "";

        String minutesText = String.valueOf(seconds / 60);
        if(minutesText.length() == 1)
            minutesText = "0" + minutesText;

        String secondsText = String.valueOf(seconds % 60);
        if(secondsText.length() == 1)
            secondsText = "0" + secondsText;

        time = minutesText + ":" + secondsText;

        return time;

    }
    /**
     * Calculate passed progress degree
     * @return
     */
    private int calculatePastProgressDegree(){
        return (360*currentProgress/maxProgress);
    }

    /**
     * If you do not want to automatic progress, you can disable it
     * and implement your own handler by using setProgress method repeatedly.
     * @param isAutoProgress
     */
    public void setAutoProgress(boolean isAutoProgress){
        this.isAutoProgress = isAutoProgress;
    }

    /**
     * Sets time text color
     * @param color
     */
    public void setTimeColor(int color){
        mTextColor = color;
        mPaintTime.setColor(mTextColor);
        postInvalidate();
    }

    public void setProgressVisibility(boolean mProgressVisibility){
        this.mProgressVisibility = mProgressVisibility;
        postInvalidate();
    }
    

    /**
     * This is detect when mButtonRegion is clicked. Which means
     * play/pause action happened.
     *
     * @param event
     * @return
     */
    /**根据点击的点,计算当前的进度--->
     * @param x
     * @param y
     * @return
     */
    public int calculateNowProgress(float x, float y) {
    	double dy = y-rectF.centerY();
    	double dx = x-rectF.centerX();
    	double temp2 = (double)Math.abs(dy) / Math.abs(dx);
    	double tan = Math.atan(temp2); //获取弧度角(第一象限)
    	float jiaodu = (float) (180*tan/3.14);
    	//下面的所有象限都是基于手机坐标系的(y轴向下)
    	if(dx>0) { //android系统的1,4象限
    		if(dy>0) {  //1象限
    			
    		}else { //4象限
    			jiaodu = 360-jiaodu;
    		}
    	}else {//2,3
    		if(dy>0) { //2象限
    			jiaodu = 180 - jiaodu;
    		}else { //3象限
    			jiaodu+=180;
    		}
    	}
 
    	jiaodu -= 145;  //因为起始角度为145度,所以这里做一个变化
    	if(jiaodu < 0) {
    		jiaodu = 360 - Math.abs(jiaodu);
    	}
    	LogUtil.v("MusicPlayerService", "CircleProgressBar--->角度:"+jiaodu);
    	//计算当前progress:
    	return (int) (jiaodu * maxProgress / 360);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	LogUtil.v("MusicPlayerService", "CircleProgressBar--->onTouchEvent"+event.getAction());
        float x = event.getX();
        float y = event.getY();
        
        
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: 
            	downX = x;
            	downY = y;
            	//平方和
                float temp = (x-rectF.centerX())*(x-rectF.centerX());
            	temp += ((y-rectF.centerY())*(y-rectF.centerY())); //temp = dx*dx+dy*dy
            	double tempR = Math.sqrt(temp);
            	if(tempR <= downMaxR && tempR >= downMinR) {//按下的位置在有效范围内
            		LogUtil.v("MusicPlayerService", "CircleProgressBar--->valid down----->");
            		isDown = true;
            		mPaintProgressLoaded.setColor(Color.parseColor("#993333"));
            		postInvalidate();
            	}
            	break;
            case MotionEvent.ACTION_MOVE:
            	if(!isDown) break;  //down的位置无效,不处理move事件了
            	if(!isDraging) {
            		if(Math.abs(downX-x)>20 || Math.abs(downY-y)>20) {
                		isDraging = true;
                	}else {
                		break;
                	}
            	}
            		
            	//处理drag事件
            	currentProgress = calculateNowProgress(x, y);
            	postInvalidate();
            	if(this.dragListener != null) {
            		dragListener.onDrag(currentProgress);  //回调
            		LogUtil.v("MusicPlayerService", "CircleProgressBar---->onDrag()");
            	}
            	
            	break;
            case MotionEvent.ACTION_UP:
            	mPaintProgressLoaded.setColor(mProgressLoadedColor); //恢复进度条的颜色
            	if(!isDown) break;  //down的位置不对,不处理up事件了
            	if(!isDraging) { //没有拖动--->触发onClick点击事件
            		currentProgress = calculateNowProgress(x, y);
            		if(this.dragListener != null) {
            			dragListener.onClick(currentProgress); //回调
            			LogUtil.v("MusicPlayerService", "CircleProgressBar---->onClick()");
            		}
            	}
            	isDown = false;
            	isDraging = false;
            	postInvalidate();
            	break;
            case MotionEvent.ACTION_CANCEL:
            	mPaintProgressLoaded.setColor(mProgressLoadedColor); //恢复进度条的颜色
            	postInvalidate();
            	break;
        }

        return super.onTouchEvent(event);
    }
    
    /**拖动进度条时的回调监听
	 * @author JonsonMarxy
	 *
	 */
	public interface OnCircleProgressBarDragListener {
		/**
		 * @param progress 拖拽的位置
		 */
		public abstract void onDrag(int progress);
		public abstract void onClick(int progress);
	}
	
	public void setOnDragListener(OnCircleProgressBarDragListener dragListener) {
		this.dragListener = dragListener;
	}
    
}
