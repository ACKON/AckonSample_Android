package olive.ackon.sample;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;

import com.olivestory.ackon.SDK;

public class AckonSample extends Application {

	{
		// CMS에서 ETC1~5 까지 파라미터 적용시킬 수 있다.
		SDK.ETC = new String[] { SDK.OS_CODE_RELEASE };
	}

	public static final String TAG = AckonSample.class.getSimpleName();

	private AckonSampleManager ackonSampleManager;

	public AckonSampleManager getAckonSampleManager() {
		return ackonSampleManager;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		// 블루투스 켜기
		if (BluetoothAdapter.getDefaultAdapter().isEnabled() == false)
			BluetoothAdapter.getDefaultAdapter().enable();

		// 매니저를 통해 실행
		ackonSampleManager = new AckonSampleManager(this);
		ackonSampleManager.update();
	}

}
