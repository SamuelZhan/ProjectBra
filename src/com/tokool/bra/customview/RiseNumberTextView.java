package com.tokool.bra.customview;

import java.text.DecimalFormat;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class RiseNumberTextView extends TextView {
	
	private float toNumber;
	private float fromNumber;
	private DecimalFormat df=new DecimalFormat("0");
	
	public interface EndListener{
		public void onEnd();
	}

	public RiseNumberTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public RiseNumberTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public RiseNumberTextView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	private void startRising(){
		
		ValueAnimator valueAnimator=ValueAnimator.ofFloat(fromNumber, toNumber);
		valueAnimator.setDuration(1000);
		
		valueAnimator.addUpdateListener(new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				// TODO Auto-generated method stub
				
				setText(df.format(Float.parseFloat(valueAnimator.getAnimatedValue().toString())));
			}
		});
		
		valueAnimator.start();
	}
	
	public void setNumber(float number){
		toNumber=number;
		fromNumber=number/2;
		startRising();
	}

}
