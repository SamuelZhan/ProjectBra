package com.tokool.bra.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;

public class ShopActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		MyWebView webView=new MyWebView(this);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.loadUrl("https://item.taobao.com/item.htm?id=528381252910");
		
		setContentView(webView);
	}
	
	private class MyWebView extends WebView{

		public MyWebView(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public boolean onKeyUp(int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub
			if(keyCode==KeyEvent.KEYCODE_BACK){
				finish();
			}
			return super.onKeyUp(keyCode, event);
			
		}
	}
}
