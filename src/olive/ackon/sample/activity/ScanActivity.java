package olive.ackon.sample.activity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import olive.ackon.sample.AckonSample;
import olive.ackon.sample.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.olivestory.ackon.Ackon;
import com.olivestory.ackon.AckonInfo;
import com.olivestory.ackon.AckonListener;

public class ScanActivity extends Activity {

	private LinkedHashMap<String, Ackon> map = new LinkedHashMap<String, Ackon>();
	private List<Ackon> list = new ArrayList<Ackon>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActionBar().setHomeButtonEnabled(true);

		setContentView(R.layout.activity_scan);

		final AckonAdapter adapter = new AckonAdapter();
		ListView listView = (ListView) findViewById(R.id.listView);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Object item = parent.getItemAtPosition(position);
				if (item != null && item instanceof Ackon) {
					AckonInfo info = ((Ackon) item).getAckonInfo();

					if (info != null) {

						AlertDialog.Builder builder = new AlertDialog.Builder(ScanActivity.this);

						builder.setTitle(info.getAlias());

						String[] keys = info.getKeys();

						String value = "\n";
						for (int i = 0; i < keys.length; i++) {
							value += keys[i] + " : " + info.getValue(keys[i]) + "\n";
						}

						if (value.trim().length() > 0) {
							builder.setMessage(value);
							builder.setPositiveButton(android.R.string.ok, null);
							builder.show();

							return;
						}
					}

					Toast.makeText(getApplicationContext(), R.string.ackoninfo_null, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(), R.string.ackoninfo_null, Toast.LENGTH_SHORT).show();
				}
			}

		});

		// 스캔 리스너
		AckonListener ackonListener = new AckonListener() {

			@Override
			public void onAckonEnter(Ackon ackon) throws NullPointerException {

			}

			// Map을 활용해서 Ackon을 리스트에 표시한다.
			@Override
			public void onAckonUpdate(Ackon ackon) throws NullPointerException {
				notifyDataSetChanged(ackon);
			}

			@Override
			public void onAckonResult(int arg0, Ackon arg1) throws NullPointerException {

			}

			@Override
			public void onAckonExit(Ackon ackon) throws NullPointerException {
			}

			/**
			 * 업데이트 된 데이터 반영
			 */
			private void notifyDataSetChanged(Ackon ackon) {
				if (ackon != null)
					synchronized (map) {

						if (map.get(ackon.getName()) == null) {

							map.put(ackon.getName(), ackon);
							list.add(ackon);
						} else {
							map.get(ackon.getName()).setRssi(ackon.getRssi());
						}

						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (adapter != null)
									adapter.notifyDataSetChanged();
							}
						});

					}
			}
		};

		getAckonSample().getAckonSampleManager().setAckonListener(ackonListener);
	}

	public AckonSample getAckonSample() {
		return (AckonSample) getApplication();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem item = menu.add(0, R.drawable.ic_exit, 0, "EXIT");

		item.setIcon(R.drawable.ic_exit);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.drawable.ic_exit:
		case android.R.id.home:
			finish();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	class AckonAdapter extends BaseAdapter {

		private int padding;

		public AckonAdapter() {
			super();
			padding = getResources().getDimensionPixelSize(R.dimen.dp16);
		}

		@Override
		public int getCount() {
			return list == null ? 0 : list.size();

		}

		@Override
		public Ackon getItem(int position) {
			return list.size() > position ? list.get(position) : null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView textView = null;
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false);
				textView = (TextView) convertView.findViewById(android.R.id.text1);
				textView.setPadding(padding, padding, padding, padding);

				convertView.setTag(textView);
			} else {
				textView = (TextView) convertView.getTag();
			}

			Ackon item = getItem(position);
			if (item != null) {
				String text = "UUID : " + item.getUuid() + "\n";
				text += "MAJOR : " + item.getMajor() + "\n";
				text += "MINOR : " + item.getMinor() + "\n";
				text += "Proximity : " + item.getRssi();
				textView.setText(text);
			}
			return convertView;
		}

	}

}
