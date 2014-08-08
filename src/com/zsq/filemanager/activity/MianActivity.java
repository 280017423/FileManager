package com.zsq.filemanager.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zsq.filemanager.R;
import com.zsq.filemanager.adapter.FolderAdapter;
import com.zsq.filemanager.util.FileUtil;
import com.zsq.filemanager.util.OpenFileUtil;

public class MianActivity extends JytActivityBase implements OnClickListener, OnItemClickListener {
	private static final long WAIT_TIME = 2000;
	private static final int REQUEST_CODE = 1;
	private GridView mGvRootFolder;
	private ArrayList<File> mFileList;
	private FolderAdapter mFilAdapter;
	private File mResDir;
	private File mCurrentFile;
	private LinearLayout mLlBack;
	private LinearLayout mLlAbout;
	private long mTouchTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initVariables();
		initView();
		setListener();
		getFileList();
	}

	private void initVariables() {
		mFileList = new ArrayList<File>();
		mFilAdapter = new FolderAdapter(this, mFileList);
		mResDir = FileUtil.getResDir(this);
		mCurrentFile = mResDir;
	}

	private void initView() {
		mGvRootFolder = (GridView) findViewById(R.id.gv_root_folder);
		mGvRootFolder.setAdapter(mFilAdapter);
		TextView titleTextView = (TextView) findViewById(R.id.title_with_back_title_btn_mid);
		titleTextView.setText(R.string.app_name);
		mLlBack = (LinearLayout) findViewById(R.id.title_with_back_title_btn_left);
		mLlAbout = (LinearLayout) findViewById(R.id.title_with_back_title_btn_right);
		TextView mTvBack = (TextView) findViewById(R.id.tv_title_with_back_left);
		mTvBack.setText(R.string.title_back_text);
		mTvBack.setBackgroundResource(R.drawable.btn_back_bg);
		TextView tvRight = (TextView) findViewById(R.id.tv_title_with_right);
		tvRight.setBackgroundResource(R.drawable.tongyong_button_bg);
		tvRight.setText(R.string.about);
	}

	private void setListener() {
		mGvRootFolder.setOnItemClickListener(this);
		mLlBack.setOnClickListener(this);
		mLlAbout.setOnClickListener(this);
	}

	private void getFileList() {
		if (null == mCurrentFile) {
			return;
		}
		if (null != mResDir && mCurrentFile.getAbsolutePath().equals(mResDir.getAbsolutePath())) {
			mLlBack.setVisibility(View.GONE);
		} else {
			mLlBack.setVisibility(View.VISIBLE);
		}
		File[] files = mCurrentFile.listFiles();
		if (files == null) {
			return;
		} else {
			mFileList.clear();
			mFileList.addAll(Arrays.asList(files));
			Collections.sort(mFileList, new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					if (o1.isDirectory() && o2.isFile())
						return -1;
					if (o1.isFile() && o2.isDirectory())
						return 1;
					return o1.getName().compareTo(o2.getName());
				}
			});
		}
		mFilAdapter.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		File tempFile = (File) parent.getAdapter().getItem(position);
		if (null == tempFile || !tempFile.exists()) {
			return;
		}
		if (tempFile.isDirectory()) {
			mCurrentFile = tempFile;
			getFileList();
		} else if (tempFile.isFile()) {
			Intent intent = OpenFileUtil.openFile(tempFile, this);
			if (null != intent) {
				startActivity(intent);
			} else {
				Toast.makeText(this, getString(R.string.no_app_found), Toast.LENGTH_LONG).show();
			}
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.title_with_back_title_btn_left:
				if (null != mCurrentFile) {
					mCurrentFile = mCurrentFile.getParentFile();
					getFileList();
				}
				break;
			case R.id.title_with_back_title_btn_right:
				Intent intent = new Intent(MianActivity.this, AboutActivity.class);
				startActivityForResult(intent, REQUEST_CODE);
				break;
			default:
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (RESULT_OK == resultCode && REQUEST_CODE == requestCode) {
			mResDir = FileUtil.getResDir(this);
			mCurrentFile = mResDir;
			getFileList();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onBackPressed() {
		if (null != mCurrentFile && null != mResDir
				&& !mCurrentFile.getAbsolutePath().equals(mResDir.getAbsolutePath())) {
			mCurrentFile = mCurrentFile.getParentFile();
			getFileList();
			return;
		} else {
			long currentTime = System.currentTimeMillis();
			if ((currentTime - mTouchTime) >= WAIT_TIME) {
				Toast.makeText(this, getString(R.string.once_press_quit), Toast.LENGTH_SHORT).show();
				mTouchTime = currentTime;
				return;
			} else {
				finish();
			}
		}
		super.onBackPressed();
	}

}
