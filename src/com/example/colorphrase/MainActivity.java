package com.example.colorphrase;

import com.medusa.lib.ColorPhrase;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
	private EditText editText;
	private TextView textView;
	private Button button;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		editText = (EditText) findViewById(R.id.editText1);
		textView = (TextView) findViewById(R.id.textView1);
		button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String pattern = editText.getText().toString();
				CharSequence chars = ColorPhrase.from(pattern).withSeparator("<>").innerColor(0xFFE6454A).outerColor(0xFF666666).format();
				textView.setText(chars);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
