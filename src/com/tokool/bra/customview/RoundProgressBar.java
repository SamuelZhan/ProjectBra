package com.tokool.bra.customview;

import com.tokool.bra.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class RoundProgressBar extends View {

	private int progressColor;
	private float progress;
	private float max=100;
	private int progressWidth;
	private Paint paint;
	
	public RoundProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
		TypedArray a=context.obtainStyledAttributes(attrs, R.styleable.RoundProgressBar, defStyleAttr, 0);	
		progress=a.getFloat(R.styleable.RoundProgressBar_progress, 0);
		progressColor=a.getInt(R.styleable.RoundProgressBar_progressColor, 0xffeceeda);
		progressWidth=a.getDimensionPixelSize(R.styleable.RoundProgressBar_progressWidth,
				(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics()));		
		a.recycle();
		
		paint=new Paint();
	}

	public RoundProgressBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public RoundProgressBar(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		paint.reset();
		paint.setAntiAlias(true);
		paint.setColor(progressColor);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(progressWidth);
		
		float centre=getWidth()/2;
		float radius=centre-progressWidth/2;
		
		canvas.drawCircle(centre, centre, radius, paint);		
		SweepGradient shader=new SweepGradient(centre, centre, new int[]{0xffF4FF90, 0xffCCFF6F, 0xff98FF43, 0xff66FF19}, null);
		Matrix matrix=new Matrix();
		matrix.setRotate(90, centre, centre);
		shader.setLocalMatrix(matrix);
		paint.setShader(shader);
		RectF oval=new RectF(centre-radius, centre-radius, centre+radius, centre+radius);
		canvas.drawArc(oval, 90, progress/max*360, false, paint);		
	}
	
	public void setProgress(float progress){
		this.progress=progress;
		invalidate();
	}
	
	public void setMax(float max){
		this.max=max;
		invalidate();
	}
	
	public void setProgressColor(int color){
		this.progressColor=color;
		invalidate();
	}

	public void setProgressWidth(int width){
		this.progressWidth=(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, width, getResources().getDisplayMetrics());
		invalidate();
	}

}
