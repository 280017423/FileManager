package com.zsq.filemanager.app;

import android.app.Application;
import android.util.Log;

/**
 * 
 * Description the class 全局应用程序
 * 
 * @version 1.0
 * @author zou.sq
 * 
 */
public class FileApplication extends Application {
	public static final String TAG = "QianJiangApplication";

	@Override
	// 建议在您app的退出之前调用mapadpi的destroy()函数，避免重复初始化带来的时间消耗
	public void onTerminate() {
		super.onTerminate();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "FileApplication, onCreate");
	}

}
