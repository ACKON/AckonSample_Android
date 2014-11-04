package olive.ackon.sample;

import java.util.LinkedHashMap;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

import com.olivestory.ackon.Ackon;
import com.olivestory.ackon.AckonDataManager;
import com.olivestory.ackon.AckonInfo;
import com.olivestory.ackon.AckonListener;
import com.olivestory.ackon.AckonService;
import com.olivestory.ackon.AckonUpdateListener;

public class AckonSampleManager implements AckonListener {

	public final static LinkedHashMap<String, Ackon> ACKON_MAP = new LinkedHashMap<String, Ackon>();

	/**
	 * 서버 데이터 갱신 시간(로그 전송 포함), 10초 이하로 설정할 수 없다.
	 */
	public final static int UPDATE_TIME = 60 * 1000;
	
	
	private Context context;
	private NotificationManager manager;
	private AckonListener ackonListener;

	public AckonSampleManager(final Context context) {
		super();
		this.context = context;
		this.manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		// 서버 데이터 갱신 시간
		AckonDataManager.setUpdateTime(UPDATE_TIME);

		// 이벤트 등록
		AckonDataManager.setAckonListener(this);

	}

	/**
	 * 서버 데이터 갱신 후 스캔 서비스 시작
	 */
	public void update() {
		AckonDataManager.update(context, new AckonUpdateListener() {

			@Override
			public void onUpdateResult(boolean result) {
				if (result) {
					AckonService.startService(context);
				}
			}
		});
	}

	/**
	 * 스캔 이벤트 등록
	 * 
	 * @param ackonListener
	 */
	public void setAckonListener(AckonListener ackonListener) {
		this.ackonListener = ackonListener;
	}

	/**
	 * Ackon 진입
	 */
	@Override
	public void onAckonEnter(Ackon ackon) {
		if (ackon != null) {
			// info 정보가 있을 경우 노티 알림 띄우기
			notification(ackon.getAckonInfo());
		}

		if (ackonListener != null)
			ackonListener.onAckonEnter(ackon);
	}

	/**
	 * Ackon 연결 끊김
	 */
	@Override
	public void onAckonExit(Ackon ackon) {
		manager.cancel(ackon.getMajor() + ackon.getMinor());

		synchronized (ACKON_MAP) {
			ACKON_MAP.remove(ackon.getName());

			if (ackonListener != null)
				ackonListener.onAckonExit(ackon);
		}

	}

	@Override
	public void onAckonResult(int arg0, Ackon ackon) {
		if (ackonListener != null)
			ackonListener.onAckonEnter(ackon);
	}

	/**
	 * 데이터 갱신
	 */
	@Override
	public void onAckonUpdate(Ackon ackon) {
		synchronized (ACKON_MAP) {
			Ackon item = ACKON_MAP.get(ackon.getName());
			if (item == null) {
				ACKON_MAP.put(ackon.getName(), ackon);
			} else {
				item.setRssi(ackon.getRssi());
			}
			if (ackonListener != null)
				ackonListener.onAckonUpdate(ackon);
		}

	}

	/**
	 * 알림 메시지 띄우기
	 * 
	 * @param info
	 */
	private void notification(AckonInfo info) {
		// info 정보는 없을 수 있기 때문에 null 체크는 필수
		if (info != null) {
			Notification.Builder builder = new Notification.Builder(context);
			builder.setSmallIcon(R.drawable.ic_launcher);
			builder.setTicker(info.getAlias());
			builder.setContentTitle(context.getString(R.string.app_name));
			builder.setContentText(info.getAlias());
			builder.setVibrate(new long[] { 500, 400, 200, 400 });

			manager.notify(info.getMajor() + info.getMinor(), builder.build());
		}
	}
}
