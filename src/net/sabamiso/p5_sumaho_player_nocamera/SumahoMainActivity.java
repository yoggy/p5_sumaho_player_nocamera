package net.sabamiso.p5_sumaho_player_nocamera;

import java.io.IOException;

import net.sabamiso.utils.Config;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

@SuppressWarnings("unused")
public class SumahoMainActivity extends Activity {

	SumahoView sumaho_view;
	Config cf;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		
		Config.init(this);
		cf = Config.getInstance();

		super.onCreate(savedInstanceState);

		RelativeLayout layout = new RelativeLayout(this);
		setContentView(layout);

		@SuppressWarnings("deprecation")
		int fp = ViewGroup.LayoutParams.FILL_PARENT;

		//
		sumaho_view = new SumahoView(this);
		layout.addView(sumaho_view, new RelativeLayout.LayoutParams(fp, fp));
	}

	@Override
	protected void onResume() {
		super.onResume();
		sumaho_view.start();
	}

	@Override
	protected void onPause() {
		sumaho_view.stop();
		super.onPause();
		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode != KeyEvent.KEYCODE_BACK) {
			return super.onKeyDown(keyCode, event);
		} else {
			return false;
		}
	}
}
