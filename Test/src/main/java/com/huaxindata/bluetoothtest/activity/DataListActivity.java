package com.huaxindata.bluetoothtest.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huaxindata.bluetoothtest.R;
import com.huaxindata.bluetoothtest.util.Configuration;
import com.huaxindata.bluetoothtest.util.DBManager;
import com.huaxindata.bluetoothtest.util.TestInfoDTO;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class DataListActivity extends Activity {

	private DBManager mgr;
	private List<TestInfoDTO> infoDTOs;
	private DataListAdapter listAdapter;
	private ListView listView;
	private List<Integer> list;

	private TextView typechoose;

	private ImageView back;
	private EditText editText;

	private RelativeLayout vinSearchLayout;
	private LinearLayout timeSearchLayout;
	private LinearLayout checkreSearchLayout;
	private LinearLayout checkuploadSearchLayout;

	private LinearLayout checkone, checktwo;
	private LinearLayout uploadcheckone, uploadchecktwo;
	private boolean isCheckone = true, isChecktwo = true;

	private ImageButton oneImage, twoImage;
	private ImageButton uploadoneImage, uploadtwoImage;

	private TextView timestrat, timeend;

	private Button upload;

	private SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
	private SimpleDateFormat df_check = new SimpleDateFormat(
			"yyyy/MM/dd HH:mm:ss");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_datalist);
		mgr = new DBManager(this);
		list = new ArrayList<Integer>();
		listView = (ListView) findViewById(R.id.datalist_listview);
		query();

		upload = (Button) findViewById(R.id.datalist_uploadbutton);
		upload.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				int temp = 0;
				for (int i = 0; i < infoDTOs.size(); i++) {
					if (infoDTOs.get(i).isChoose()) {
						temp++;
						socketClientSendMSGToService(infoDTOs.get(i).getVin()
								+ "[" + getInfo(infoDTOs.get(i)) + "]");

					}
				}
				if (temp == 0) {
					Toast.makeText(DataListActivity.this, "请选择要上传的项",
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		// --
		typechoose = (TextView) findViewById(R.id.search_type);
		typechoose.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				popShow();
			}
		});
		back = (ImageView) findViewById(R.id.back);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		//
		vinSearchLayout = (RelativeLayout) findViewById(R.id.datalist_vinsearch);
		timeSearchLayout = (LinearLayout) findViewById(R.id.datalist_timesearch);
		checkreSearchLayout = (LinearLayout) findViewById(R.id.datalist_checkresearch);
		checkuploadSearchLayout = (LinearLayout) findViewById(R.id.datalist_checkupload);

		// ----------条件搜索 按时间 UI
		timestrat = (TextView) findViewById(R.id.dataList_timestrat);
		timeend = (TextView) findViewById(R.id.dataList_timeend);
		timestrat.setText(df.format(new Date(new Date().getTime() - 3 * 24 * 60
				* 60 * 1000)));
		timeend.setText(df.format(new Date()));
		timestrat.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				showDateChoose(timestrat.getText() + "", timestrat);
			}
		});
		timeend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				showDateChoose(timeend.getText() + "", timeend);
			}
		});

		// --------结果条件搜索
		checkone = (LinearLayout) findViewById(R.id.datalist_one);
		checktwo = (LinearLayout) findViewById(R.id.datalist_two);
		oneImage = (ImageButton) findViewById(R.id.datalist_oneimage);
		twoImage = (ImageButton) findViewById(R.id.datalist_twoimage);
		checkone.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (isCheckone) {
					isCheckone = false;
					oneImage.setImageResource(R.drawable.transparent_color);
				} else {
					isCheckone = true;
					oneImage.setImageResource(R.drawable.checkbox_choose);
				}
				searchState();
			}
		});
		checktwo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (isChecktwo) {
					isChecktwo = false;
					twoImage.setImageResource(R.drawable.transparent_color);
				} else {
					isChecktwo = true;
					twoImage.setImageResource(R.drawable.checkbox_choose);
				}
				searchState();
			}
		});
		// ---
		uploadcheckone = (LinearLayout) findViewById(R.id.datalist_upload_one);
		uploadchecktwo = (LinearLayout) findViewById(R.id.datalist_upload_two);
		uploadoneImage = (ImageButton) findViewById(R.id.datalist_upload_oneimage);
		uploadtwoImage = (ImageButton) findViewById(R.id.datalist_upload_twoimage);
		uploadcheckone.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (isCheckone) {
					isCheckone = false;
					uploadoneImage
							.setImageResource(R.drawable.transparent_color);
				} else {
					isCheckone = true;
					uploadoneImage.setImageResource(R.drawable.checkbox_choose);
				}
				searchUpload();
			}
		});
		uploadchecktwo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (isChecktwo) {
					isChecktwo = false;
					uploadtwoImage
							.setImageResource(R.drawable.transparent_color);
				} else {
					isChecktwo = true;
					uploadtwoImage.setImageResource(R.drawable.checkbox_choose);
				}
				searchUpload();
			}
		});
		// --------VIN码模糊搜索
		editText = (EditText) findViewById(R.id.datalist_edit);
		editText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
									  int arg3) {
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
										  int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				searchVin(arg0.toString());
			}
		});
	}

	public String getInfo(TestInfoDTO infoDTO) {
		if (infoDTO.getStatelisten() == Configuration.CHECK_TRUE
				&& infoDTO.getStatespeack() ==Configuration.CHECK_TRUE) {
			return "BLOK";
		} else {
			return "BLNG";
		}
	}

	public void query() {
		infoDTOs = mgr.query();
		Collections.reverse(infoDTOs);
		listAdapter = new DataListAdapter(this, infoDTOs, list);
		listView.setAdapter(listAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
									long arg3) {
				System.out.println("adad");
				if (infoDTOs.get(arg2).isChoose()) {
					infoDTOs.get(arg2).setChoose(false);
				} else {
					infoDTOs.get(arg2).setChoose(true);
				}
				list.add(arg2);
				listAdapter.notifyDataSetChanged();
			}
		});
	}

	public void queryAll() {
		infoDTOs = mgr.query();
		Collections.reverse(infoDTOs);
		listAdapter = new DataListAdapter(this, infoDTOs, list);
		listView.setAdapter(listAdapter);
	}

	public void searchVin(String vin) {
		infoDTOs = mgr.queryForVin(vin);
		Collections.reverse(infoDTOs);
		listAdapter = new DataListAdapter(this, infoDTOs, list);
		listView.setAdapter(listAdapter);
	}

	public void searchTime() {
		try {
			infoDTOs = mgr
					.queryForTime(
							df_check.parse(timestrat.getText() + " 00:00:01")
									.getTime(),
							df_check.parse(timeend.getText() + " 23:59:59")
									.getTime());
			Collections.reverse(infoDTOs);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		listAdapter = new DataListAdapter(this, infoDTOs, list);
		listView.setAdapter(listAdapter);
	}

	public void searchState() {
		if (isCheckone & isChecktwo) {
			infoDTOs = mgr.query();
		} else {
			if (isCheckone) {
				infoDTOs = mgr.queryForState(true);
			}
			if (isChecktwo) {
				infoDTOs = mgr.queryForState(false);
			}
		}
		Collections.reverse(infoDTOs);
		listAdapter = new DataListAdapter(this, infoDTOs, list);
		listView.setAdapter(listAdapter);
	}

	public void searchUpload() {
		if (isCheckone & isChecktwo) {
			infoDTOs = mgr.query();
		} else {
			if (isCheckone) {
				infoDTOs = mgr.queryForUpload(true);
			}
			if (isChecktwo) {
				infoDTOs = mgr.queryForUpload(false);
			}
		}
		Collections.reverse(infoDTOs);
		listAdapter = new DataListAdapter(this, infoDTOs, list);
		listView.setAdapter(listAdapter);
	}

	public void uploaddata(View view) {
		StringBuffer texttemp = new StringBuffer();
		for (int i = 0; i < infoDTOs.size(); i++) {
			if (infoDTOs.get(i).isChoose()) {
				// texttemp.append("")
			}
		}
	}

	private void showDateChoose(String time, final TextView textView) {
		Date date = new Date(time);
		String[] temp = df.format(date).split("/");
		DatePickerDialog datePickerDialog = new DatePickerDialog(this,
				new DatePickerDialog.OnDateSetListener() {

					@Override
					public void onDateSet(DatePicker view, int year,
										  int monthOfYear, int dayOfMonth) {
						textView.setText(year + "/" + (monthOfYear + 1) + "/"
								+ dayOfMonth);
						searchTime();
					}
				}, Integer.parseInt(temp[0]), (Integer.parseInt(temp[1]) - 1),
				Integer.parseInt(temp[2]));
		datePickerDialog.show();
	}

	// 厂站切换弹出的pp
	private PopupWindow popupWindow;

	public void popShow() {
		if (popupWindow != null) {
			popupWindow.showAsDropDown(typechoose);
			return;
		}
		View popupView = LayoutInflater.from(this).inflate(R.layout.ppwindow,
				null);
		RelativeLayout vin = (RelativeLayout) popupView
				.findViewById(R.id.ppwindow_vin);
		vin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				typechoose.setText(getString(R.string.ppwindow_vin));
				vinSearchLayout.setVisibility(View.VISIBLE);
				timeSearchLayout.setVisibility(View.GONE);
				checkreSearchLayout.setVisibility(View.GONE);
				checkuploadSearchLayout.setVisibility(View.GONE);
				popupWindow.dismiss();
				queryAll();
			}
		});
		RelativeLayout checkre = (RelativeLayout) popupView
				.findViewById(R.id.ppwindow_checkre);
		checkre.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				vinSearchLayout.setVisibility(View.GONE);
				timeSearchLayout.setVisibility(View.GONE);
				checkreSearchLayout.setVisibility(View.VISIBLE);
				checkuploadSearchLayout.setVisibility(View.GONE);
				typechoose.setText(getString(R.string.pp_checkre));
				popupWindow.dismiss();
				uploadoneImage.setImageResource(R.drawable.checkbox_choose);
				uploadtwoImage.setImageResource(R.drawable.checkbox_choose);
				isCheckone = true;
				isChecktwo = true;
				queryAll();
			}
		});
		RelativeLayout time = (RelativeLayout) popupView
				.findViewById(R.id.ppwindow_time);
		time.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				vinSearchLayout.setVisibility(View.GONE);
				timeSearchLayout.setVisibility(View.VISIBLE);
				checkreSearchLayout.setVisibility(View.GONE);
				checkuploadSearchLayout.setVisibility(View.GONE);
				typechoose.setText(getString(R.string.pp_checktime));
				popupWindow.dismiss();
				queryAll();
			}
		});

		RelativeLayout upload = (RelativeLayout) popupView
				.findViewById(R.id.ppwindow_upload);
		upload.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				vinSearchLayout.setVisibility(View.GONE);
				timeSearchLayout.setVisibility(View.GONE);
				checkreSearchLayout.setVisibility(View.GONE);
				checkuploadSearchLayout.setVisibility(View.VISIBLE);
				typechoose.setText(getString(R.string.pp_checkupload));
				popupWindow.dismiss();
				uploadoneImage.setImageResource(R.drawable.checkbox_choose);
				uploadtwoImage.setImageResource(R.drawable.checkbox_choose);
				isCheckone = true;
				isChecktwo = true;
				queryAll();
			}
		});

		popupWindow = new PopupWindow(popupView, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, true);
		popupWindow.setTouchable(true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(),
				(Bitmap) null));

		popShow();
	}

	/**
	 * 发送消息给对接设备服务（开启socket客户端发送消息）
	 *
	 * @param msg 消息内容
	 */
	private void socketClientSendMSGToService(String msg) {
		Log.e("xxx", "onClick:=========上传" + msg);
		upload(msg);
	}

	private void upload(String msg) {
		MainHomeActivtiy.sConnectServerThread.isUploaded= true;
		MainHomeActivtiy.sConnectServerThread.send1(msg, "BL");
	}


	class DataListAdapter extends BaseAdapter {

		private Context mContext;
		private List<TestInfoDTO> mInfoDTOs;
		private List<Integer> list;

		public DataListAdapter(Context context, List<TestInfoDTO> infoDTOs,
							   List<Integer> list) {
			mContext = context;
			mInfoDTOs = infoDTOs;
			this.list = list;
		}

		@Override
		public int getCount() {
			return mInfoDTOs.size();
		}

		@Override
		public Object getItem(int arg0) {
			return mInfoDTOs.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		private SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy.MM.dd/HH:mm");

		@Override
		public View getView(final int arg0, View view, ViewGroup arg2) {
			ViewHold hold;
			if (view == null) {
				hold = new ViewHold();
				view = LayoutInflater.from(mContext).inflate(
						R.layout.activity_datalist_adapter, null);
				hold.checkbox = (ImageButton) view
						.findViewById(R.id.listadapter_choose_bg);
				hold.stateicon = (ImageView) view
						.findViewById(R.id.datalist_adapter_stateicon);
				hold.time = (TextView) view.findViewById(R.id.listadapter_text);
				hold.uploadicon = (ImageView) view
						.findViewById(R.id.listadapter_upload_image);
				hold.vinNumber = (TextView) view
						.findViewById(R.id.datalist_adapter_vinnumber);
				view.setTag(hold);
			}
			hold = (ViewHold) view.getTag();
			if (mInfoDTOs.get(arg0).getStatelisten() ==Configuration.CHECK_TRUE
					&& mInfoDTOs.get(arg0).getStatespeack() == Configuration.CHECK_TRUE) {
				hold.stateicon.setImageResource(R.drawable.icon_ok);
			} else {
				hold.stateicon.setImageResource(R.drawable.icon_ng);
			}
			if (mInfoDTOs.get(arg0).getUpload() == 1) {
				hold.uploadicon.setVisibility(View.VISIBLE);
			} else {
				hold.uploadicon.setVisibility(View.INVISIBLE);
			}
			hold.time.setText(dateFormat.format(new Date(mInfoDTOs.get(arg0)
					.getTime())));
			hold.vinNumber.setText(mInfoDTOs.get(arg0).getVin());
			System.out.println("mInfoDTOs.get(arg0).isChoose()-->"
					+ mInfoDTOs.get(arg0).isChoose());
			if (mInfoDTOs.get(arg0).isChoose()) {
				hold.checkbox.setImageResource(R.drawable.checkbox_choose);
			} else {
				hold.checkbox.setImageResource(R.drawable.transparent_color);
			}
			return view;
		}

	}
	class ViewHold {
		ImageView stateicon;
		ImageView uploadicon;
		TextView vinNumber;
		TextView time;
		ImageButton checkbox;
	}

}