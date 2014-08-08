package com.zsq.filemanager.activity;

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zsq.filemanager.R;
import com.zsq.filemanager.util.ConstantSet;
import com.zsq.filemanager.util.SharedPreferenceUtil;
import com.zsq.filemanager.util.StringUtil;

public class AboutActivity extends ActivityBase implements OnClickListener {

	private static final int CLICK_COUNT_FIVE = 5;
	private static int CLICK_COUNT;
	private LinearLayout mLlBack;
	private LinearLayout mLlPath;
	private EditText mEdtPath;
	private long mSystemCurrentTime;
	private String mVersionName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		initVariables();
		initView();
		setListener();
	}

	private void initVariables() {
		mSystemCurrentTime = System.currentTimeMillis();
		mVersionName = getString(R.string.unknow_version_name);
		try {
			mVersionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void setListener() {
		mLlBack.setOnClickListener(this);
	}

	private void initView() {

		TextView titleTextView = (TextView) findViewById(R.id.title_with_back_title_btn_mid);
		titleTextView.setText(R.string.about);
		mLlBack = (LinearLayout) findViewById(R.id.title_with_back_title_btn_left);
		mLlPath = (LinearLayout) findViewById(R.id.ll_path_layout);
		mEdtPath = (EditText) findViewById(R.id.edt_path_name);
		TextView mTvBack = (TextView) findViewById(R.id.tv_title_with_back_left);
		mTvBack.setText(R.string.title_back_text);
		mTvBack.setBackgroundResource(R.drawable.btn_back_bg);
		TextView tvVersion = (TextView) findViewById(R.id.tv_version_code);
		tvVersion.setText(getString(R.string.text_version_code, mVersionName));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.title_with_back_title_btn_left:
				back();
				break;
			case R.id.iv_icon:
				changeDir();
				break;
			case R.id.btn_path_name:
				String path = mEdtPath.getText().toString().trim();
				if (StringUtil.isNullOrEmpty(path)) {
					Toast.makeText(this, getString(R.string.input_custom_dir), Toast.LENGTH_SHORT).show();
				} else {
					SharedPreferenceUtil.saveValue(AboutActivity.this, ConstantSet.CONFIG_FILE, ConstantSet.CUSTOM_DIR,
							path);
					setResult(RESULT_OK);
					finish();
				}

				break;
			default:
				break;
		}
	}

	private void changeDir() {
		long currentTime = System.currentTimeMillis();
		if (Math.abs(currentTime - mSystemCurrentTime) <= 2 * ConstantSet.INTERVAL_TIME) {
			CLICK_COUNT += 1;
			mSystemCurrentTime = currentTime;
		} else {
			CLICK_COUNT = 0;
			mSystemCurrentTime = currentTime;
		}
		if (CLICK_COUNT == CLICK_COUNT_FIVE) {
			CLICK_COUNT = 0;
			mLlPath.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onBackPressed() {
		back();
		super.onBackPressed();
	}

	private void back() {
		setResult(RESULT_CANCELED);
		finish();
	}
}
