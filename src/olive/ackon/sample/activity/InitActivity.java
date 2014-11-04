package olive.ackon.sample.activity;

import olive.ackon.sample.R;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class InitActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (BluetoothAdapter.getDefaultAdapter().isEnabled() == false)
			BluetoothAdapter.getDefaultAdapter().enable();

		setContentView(R.layout.init);

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				startActivity(new Intent(getBaseContext(), MainActivity.class));
			}
		}, 1200);
	}
}
