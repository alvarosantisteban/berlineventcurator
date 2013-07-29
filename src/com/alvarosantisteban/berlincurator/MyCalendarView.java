package com.alvarosantisteban.berlincurator;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Map.Entry;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;

public class MyCalendarView extends CalendarView{
	
	private int numOfEvents = 69;
	Context context;
	
	private int numOfWeeks = 6;
	private int distanceBetweenWeeks = 0;
	
	
	private final Paint mDrawPaint = new Paint();
	private final Paint mMonthNumDrawPaint = new Paint();
	
	// The height this view should draw at in pixels, set by height param
	private int mHeight; // = (mListView.getHeight() - mListView.getPaddingTop() - mListView.getPaddingBottom()) / mShownWeekCount;
	// Quick reference to the width of this view, matches parent
	private int mWidth; // onSizeChanged
	private int mWeekSeperatorLineWidth; // = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, UNSCALED_WEEK_SEPARATOR_LINE_WIDTH, displayMetrics);
	
	private static final int UNSCALED_WEEK_SEPARATOR_LINE_WIDTH = 1;
	// The number of days + a spot for week number if it is displayed
	private int mNumCells = 8;
	private int mDateTextSize; // = dateTextAppearance.getDimensionPixelSize(R.styleable.TextAppearance_textSize, DEFAULT_DATE_TEXT_SIZE);
	// Quick lookup for checking which days are in the focus month
	private boolean[] mFocusDay;
	private int mFocusedMonthDateColor; // = attributesArray.getColor(R.styleable.CalendarView_focusedMonthDateColor, 0);
	private int mUnfocusedMonthDateColor; // = attributesArray.getColor(R.styleable.CalendarView_unfocusedMonthDateColor, 0);
	// Cache the number strings so we don't have to recompute them each time
	private String[] mDayNumbers; // = String.format(Locale.getDefault(), "%d", mTempDate.get(Calendar.DAY_OF_MONTH));
	private boolean mShowWeekNumber; // show the week number or not
	private int mWeekNumberColor; // = attributesArray.getColor(R.styleable.CalendarView_weekNumberColor, 0);
	
	public MyCalendarView(Context context) {	
		super(context);
		System.out.println("solo context");
	}
	
	public MyCalendarView(Context context, AttributeSet attrs){
		super(context, attrs);
		System.out.println("context y atributos");
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MyCalendarView);
		try{
			numOfEvents = a.getInt(R.styleable.MyCalendarView_eventsNumber, 0);
			initializeThings();
			initializePaints();
		} finally{
			a.recycle();
		}
	}
	
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		System.out.println("onSizeChanged()");
		mWidth = w;
		mHeight = h/numOfWeeks;
		System.out.println("oldwidth:"+oldw +" - oldheight:"+oldh);
		System.out.println("width:"+w +" - height:"+h);
	}
	
	private void initializeThings(){
		System.out.println("initializeThings()");
		//ListView mListView = (ListView) findViewById(R.id.list);
		//mHeight = (mListView.getHeight() - mListView.getPaddingTop() - mListView.getPaddingBottom()) / getShownWeekCount();
		mHeight = 59;
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		mWeekSeperatorLineWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, UNSCALED_WEEK_SEPARATOR_LINE_WIDTH, displayMetrics);
	}
	
	/*
	 * 
	 * 
	 */
	private void initializePaints() {
		System.out.println("initializePaints()");
		mDrawPaint.setFakeBoldText(false);
    	mDrawPaint.setAntiAlias(true);
    	mDrawPaint.setStyle(Style.FILL);
    	
    	mMonthNumDrawPaint.setColor(Color.BLUE);
        mMonthNumDrawPaint.setTextSize(10);
    	mMonthNumDrawPaint.setFakeBoldText(true);
        mMonthNumDrawPaint.setAntiAlias(true);
        mMonthNumDrawPaint.setStyle(Style.FILL);
        mMonthNumDrawPaint.setTextAlign(Align.CENTER);
        //mMonthNumDrawPaint.setTextSize(mDateTextSize);
        }
	

	
	@Override
    protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		System.out.println("dispatchDraw");
		//drawEventsNumbers(canvas);
		/*
		int x = 0;
		int y = 0;
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		
		// draw a solid blue circle
		paint.setColor(Color.BLUE);
		canvas.drawCircle(20, 20, 15, paint);
		
		// draw some text using FILL style
		paint.setStyle(Paint.Style.FILL);
		//turn antialiasing on
		paint.setAntiAlias(true);
		paint.setTextSize(10);
		canvas.drawText("12", 358, 139, paint);
		
		View v = getChildAt(0);
		int numView = getChildCount();
		System.out.println("number of views:" +numView);
		System.out.println("View.tag:"+ this.getTag());
		System.out.println("View.className:"+ getClass().getName());
		if (v instanceof MyCalendarView) {
			System.out.println("Es mycalendarview");
		}else if (v instanceof CalendarView){
			System.out.println("Es calendar view");
		}
		v.getLayoutParams();
		*/
	}
	
	/**
     * Draws the week and month day numbers for this week.
     *
     * @param canvas The canvas to draw on
     */
    private void drawEventsNumbers(Canvas canvas) {		
    	System.out.println("drawEventsNumbers()");
        final float textHeight = mDrawPaint.getTextSize();
        final int y = (int) ((mHeight + textHeight) / 2) - mWeekSeperatorLineWidth;
        final int nDays = mNumCells;
        final int divisor = 2 * nDays;

        //mDrawPaint.setTextAlign(Align.CENTER);
        //mDrawPaint.setTextSize(mDateTextSize);

        int i = 0;
        //boolean a = getLayoutDirection() == LAYOUT_DIRECTION_RTL;
        boolean a = true;
        if (a) {
        	for(int j = 0; j < numOfWeeks; j++){
        		//System.out.println("------------------- j:"+j);
	            for (; i < nDays - 1; i++) {
	                mMonthNumDrawPaint.setColor(Color.BLUE);
	                mMonthNumDrawPaint.setTextSize(13);
	                int x = (2 * i + 1) * mWidth / divisor;
	                canvas.drawText(Integer.valueOf(numOfEvents).toString(), x+60, y+140+distanceBetweenWeeks, mMonthNumDrawPaint);
	            }
	            distanceBetweenWeeks += 135;
	            i=0;
        	}
            /*
            if (mShowWeekNumber) {
                mDrawPaint.setColor(mWeekNumberColor);
                int x = mWidth - mWidth / divisor;
                canvas.drawText(mDayNumbers[0], x, y, mDrawPaint);
            }
            */
        } else {
            if (mShowWeekNumber) {
                mDrawPaint.setColor(mWeekNumberColor);
                int x = mWidth / divisor;
                canvas.drawText(mDayNumbers[0], x, y, mDrawPaint);
                i++;
            }
            for (; i < nDays; i++) {
                mMonthNumDrawPaint.setColor(mFocusDay[i] ? mFocusedMonthDateColor
                        : mUnfocusedMonthDateColor);
                int x = (2 * i + 1) * mWidth / divisor;
                canvas.drawText(mDayNumbers[i], x, y, mMonthNumDrawPaint);
            }
        }
    }
	
}
