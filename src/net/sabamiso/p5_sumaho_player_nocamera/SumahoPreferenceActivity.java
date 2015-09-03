package net.sabamiso.p5_sumaho_player_nocamera;

import net.sabamiso.p5_sumaho_player_nocamera.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SumahoPreferenceActivity extends PreferenceActivity {
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		finish();
	}
}
