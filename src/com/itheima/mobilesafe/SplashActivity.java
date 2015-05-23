package com.itheima.mobilesafe;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;
import android.widget.Toast;

import com.itheima.mobilesafe.utils.StreamTools;

public class SplashActivity extends Activity {


	protected static final String TAG = "SplashActivity";
	protected static final int SHOW_UPATE_DIALOG = 0;
	protected static final int ENTER_HOME = 1;
	protected static final int URL_ERROR = 2;
	protected static final int NETWORK_ERROR = 3;
	protected static final int JSON_ERROR = 4;
	private TextView tv_splash_version;
	private String description;
	/**
	 * 新版本的下载地址
	 */
	private String apkurl;
		
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		tv_splash_version = (TextView) findViewById(R.id.tv_splash_version);
		tv_splash_version.setText("版本号" + getVersionName());
		
		//检查升级
		checkUpdata();
		AlphaAnimation aa = new AlphaAnimation(1.0f, 0.2f);
		aa.setDuration(1000);
		findViewById(R.id.rl_root_splash).startAnimation(aa);
	}
	
	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case SHOW_UPATE_DIALOG: // 显示升级的对话框
				Log.i(TAG, "显示升级的对话框");
				break;
			case ENTER_HOME: // 进入主页面
				enterHome();
				break;
			case URL_ERROR: // URL错误
				enterHome();
				Toast.makeText(getApplicationContext(), "URL错误", 0).show();
				break;
			case NETWORK_ERROR: // 网络异常
				enterHome();
				Toast.makeText(getApplicationContext(), "网络异常", 0).show();
				break;
			case JSON_ERROR: // JSON解析出错
				enterHome();
				Toast.makeText(SplashActivity.this, "JSON解析出错", 0).show();
				break;
			default:
				break;
			}
		}

		
		
	};
	private void enterHome() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, HomeActivity.class);
		startActivity(intent);
		//关闭当前页面
		finish();
	}
	/**
	 * 检查是否有新版本，如果有就升级
	 */
	private void checkUpdata() {
		new Thread(){
			public void run() {
				// URL http://10.10.117.203:8080/updata.json

				Message mes = Message.obtain();
				long startTime = System.currentTimeMillis();
				try {
					URL url = new URL(getString(R.string.serverurl));
					//联网
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(4000);
					int code = conn.getResponseCode();
					if(code == 200){
						//联网成功
						InputStream is = conn.getInputStream();
						//把流转换为String
						String result = StreamTools.readFromStream(is);
						Log.i(TAG,"联网成功了"+result);
						// json解析
						JSONObject obj = new JSONObject(result);
						// 得到服务器版本的信息
						String version = (String) obj.get("version");
						
						String description = (String) obj.get("description");
						String apkurl = (String) obj.get("apkurl");
						
						// 校验是否有新版本
						if (getVersionName().equals(version)){
							//版本一致，没有新版本，进入主页面
							mes.what = ENTER_HOME;						
						} else {
							//有新版本，弹出一升级对话框
							mes.what = SHOW_UPATE_DIALOG;
						}
					}
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					mes.what = URL_ERROR;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					mes.what = NETWORK_ERROR;
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					mes.what = JSON_ERROR;
				} finally{
					long endTime = System.currentTimeMillis();
					long dTime = endTime - startTime;
					if(dTime < 2000){
						try {
							Thread.sleep(2000 -dTime);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					handler.sendMessage(mes);
				}
			};
		}.start();
	}

	
	
	
	
	
	/**
	 * 得到应用程序的版本名称
	 */

	private String getVersionName() {
		// 用来管理手机的APK
		PackageManager pm = getPackageManager();

		
			try {
				// 得到知道APK的功能清单文件
				PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
				return info.versionName;
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "";
			}
		

	}

}
