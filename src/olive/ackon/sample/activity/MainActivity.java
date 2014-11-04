package olive.ackon.sample.activity;

import java.util.ArrayList;
import java.util.List;

import olive.ackon.sample.AckonSample;
import olive.ackon.sample.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.olivestory.ackon.AckonDataManager;
import com.olivestory.ackon.AckonInfo;
import com.olivestory.ackon.AckonUpdateListener;
import com.olivestory.ackon.SDK;
import com.olivestory.ackon.cms.AckonDataDel;
import com.olivestory.ackon.cms.OnAckonAgreeResult;
import com.olivestory.ackon.cms.OnAckonDataDelResult;
import com.olivestory.ackon.cms.SetAckonAgree;

public class MainActivity extends Activity {

	private SlidingPaneLayout paneLayout;

	private TextView version;
	private TextView device_id;
	private TextView service_id;
	private TextView domain;

	public AckonSample getAckonSample() {
		return (AckonSample) getApplication();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActionBar().setHomeButtonEnabled(true);

		setContentView(R.layout.activity_main);

		service_id = (TextView) findViewById(R.id.service_id);
		domain = (TextView) findViewById(R.id.domain);
		device_id = (TextView) findViewById(R.id.device_id);
		version = (TextView) findViewById(R.id.version);

		// SDK 정보
		service_id.setText(SDK.SERVICE_ID != null ? SDK.SERVICE_ID : "");
		domain.setText(SDK.DOMAIN != null ? SDK.DOMAIN : "");
		version.setText("Ver." + SDK.VERSION);
		device_id.setText("DEVICE_ID : \n" + SDK.DEVICE_ID);

		paneLayout = (SlidingPaneLayout) findViewById(R.id.paneLayout);
		paneLayout.setParallaxDistance(getResources().getDisplayMetrics().widthPixels / 3);

		paneLayout.openPane();
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				if (paneLayout != null) {
					paneLayout.closePane();
				}
			}
		}, 800);

		final ExpandableAckonAdapter adapter = new ExpandableAckonAdapter();
		ExpandableListView expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
		expandableListView.setAdapter(adapter);

		// CMS 데이터 갱신
		findViewById(R.id.update).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (Patterns.WEB_URL.matcher("http://" + domain.getText().toString().trim()).matches() == false) {
					return;
				}

				if (service_id.getText().toString().trim().length() < 1) {
					return;
				}

				SDK.SERVICE_ID = service_id.getText().toString().trim();
				SDK.DOMAIN = domain.getText().toString().trim();

				// Ackon 데이터 갱신
				AckonDataManager.update(getApplicationContext(), new AckonUpdateListener() {

					@Override
					public void onUpdateResult(boolean result) {
						if (result) {
							if (adapter != null) {
								adapter.update();

								if (paneLayout != null)
									paneLayout.closePane();
							}
						} else {
							Toast.makeText(getApplicationContext(), R.string.http_error, Toast.LENGTH_SHORT).show();
						}
					}
				});
			}
		});

		// 위치정보 사용 동의
		findViewById(R.id.agree).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SetAckonAgree agree = new SetAckonAgree(MainActivity.this);

				agree.setAckonAgree(new OnAckonAgreeResult() {

					@Override
					public void onAckonAgreeResult(boolean result) {
						AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
						builder.setMessage(result ? R.string.data_agree_ok : R.string.data_agree_error);
						builder.setPositiveButton(android.R.string.ok, null);

						builder.show();

					}
				});

			}
		});

		// 서버기록정보 삭제
		findViewById(R.id.delete).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AckonDataDel del = new AckonDataDel(MainActivity.this);
				del.setAckonDataDel(new OnAckonDataDelResult() {
					@Override
					public void onAckonDataDelResult(boolean result) {
						AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
						builder.setMessage(result ? R.string.data_delete_ok : R.string.data_delete_error);
						builder.setPositiveButton(android.R.string.ok, null);

						builder.show();
					}
				});

			}
		});

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem item = menu.add(0, R.drawable.ic_search, 0, "SCAN");
		item.setIcon(R.drawable.ic_search);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.drawable.ic_search:
			startActivity(new Intent(getBaseContext(), ScanActivity.class));
			break;
		case android.R.id.home:

			if (paneLayout == null)
				paneLayout = (SlidingPaneLayout) findViewById(R.id.paneLayout);

			if (paneLayout.isOpen()) {
				paneLayout.closePane();
			} else {
				paneLayout.openPane();
			}
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	class ExpandableAckonAdapter extends BaseExpandableListAdapter {
		private int padding;

		private List<AckonInfo> list;

		@SuppressWarnings("unchecked")
		public ExpandableAckonAdapter() {
			super();
			list = new ArrayList<AckonInfo>();
			List<AckonInfo> data = AckonDataManager.getAckonInfoList();
			if (data != null)
				list.addAll(data);

			padding = getResources().getDimensionPixelSize(R.dimen.dp16);
		}

		@SuppressWarnings("unchecked")
		public void update() {
			List<AckonInfo> data = AckonDataManager.getAckonInfoList();
			if (data != null && data.size() > 0) {
				list.clear();
				list.addAll(data);
				notifyDataSetChanged();
			} else {
				Toast.makeText(getApplicationContext(),
						"AckonInfo Data " + (data == null ? "null." : "size : " + data.size()), Toast.LENGTH_LONG)
						.show();
			}
		}

		@Override
		public int getGroupCount() {
			return list == null ? 0 : list.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return 1;
		}

		@Override
		public AckonInfo getGroup(int groupPosition) {
			return list.get(groupPosition);
		}

		@Override
		public AckonInfo getChild(int groupPosition, int childPosition) {
			return getGroup(groupPosition);
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			Holder holder = null;
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.row_info_item, parent, false);
				holder = new Holder(convertView);

				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}

			holder.setData(getGroup(groupPosition));
			return convertView;
		}

		private class Holder {
			private TextView name, uuid, major, minor;

			public Holder(View view) {
				super();
				this.name = (TextView) view.findViewById(R.id.name);
				this.uuid = (TextView) view.findViewById(R.id.uuid);
				this.major = (TextView) view.findViewById(R.id.major);
				this.minor = (TextView) view.findViewById(R.id.minor);
			}

			void setData(AckonInfo item) {
				if (item != null) {
					name.setText(item.getAlias());
					uuid.setText(item.getUuid());
					major.setText("MAJOR : " + String.valueOf(item.getMajor()));
					minor.setText("MINOR : " + String.valueOf(item.getMinor()));
				}
			}

		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
				ViewGroup parent) {
			TextView textView = null;
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false);
				textView = (TextView) convertView.findViewById(android.R.id.text1);
				textView.setPadding(padding * 3, padding, padding, padding);

				textView.setBackgroundColor(Color.parseColor("#e5e5e5"));

				convertView.setTag(textView);
			} else {
				textView = (TextView) convertView.getTag();
			}

			AckonInfo item = getChild(groupPosition, childPosition);
			if (item != null) {
				String text = null;
				String[] keys = item.getKeys();

				for (int i = 0; i < keys.length; i++) {
					if (i == 0) {
						text = "Action Data\n";
					}
					text += keys[i] + " : " + item.getValue(keys[i]);

					if (i < keys.length - 1) {
						text += "\n";
					}
				}

				if (text != null)
					textView.setText(text);
				else
					textView.setText(R.string.ackoninfo_null);
			} else {
				textView.setText(R.string.ackoninfo_null);
			}

			return convertView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

	}

}
