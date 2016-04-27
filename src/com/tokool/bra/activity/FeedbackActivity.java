package com.tokool.bra.activity;

import com.tokool.bra.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class FeedbackActivity extends Activity {
	
	private ImageView btnBack;
	private Button btnSend;
	private EditText etContact, etFeedback;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feedback);
		
		btnBack=(ImageView)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		etContact=(EditText)findViewById(R.id.et_contact);
		
		etFeedback=(EditText)findViewById(R.id.et_feedback);
		
		btnSend=(Button)findViewById(R.id.btn_send);
		btnSend.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(etContact.getText()==null || etContact.getText().toString().trim().equals("")){
					Toast.makeText(FeedbackActivity.this, getString(R.string.contact_can_not_be_null), Toast.LENGTH_SHORT).show();
					return;
				}
				if(etFeedback.getText()==null || etFeedback.getText().toString().trim().equals("")){
					Toast.makeText(FeedbackActivity.this, getString(R.string.feedback_can_not_be_null), Toast.LENGTH_SHORT).show();
					return;
				}
				etContact.setText("");
				etFeedback.setText("");
				Toast.makeText(FeedbackActivity.this, getString(R.string.sent), Toast.LENGTH_SHORT).show();
			}
		});
	}
}
