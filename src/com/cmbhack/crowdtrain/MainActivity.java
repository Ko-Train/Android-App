package com.cmbhack.crowdtrain;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.cmbhack.crowdtrain.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.os.Build;

public class MainActivity extends Activity {
	static final String TAG = "GCMDemo";

	private ContainerFragment mContainerFragment;
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";

	String SENDER_ID = "74505910852";

	TextView mDisplay;
	GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();
	SharedPreferences prefs;
	Context context;

	String regid;

	@Override
	protected void onResume() {
		super.onResume();
		if (mContainerFragment != null) {
			Bundle extras = getIntent().getExtras();
			if (extras != null) {
				String trainId = extras.getString("trainId", null);
				if (trainId != null) {
					mContainerFragment
							.setUrl("http://ko-train.meteor.com/train/"
									+ trainId);
					return;
				}
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = getApplicationContext();

		if (savedInstanceState == null) {
			mContainerFragment = new ContainerFragment();
			getFragmentManager().beginTransaction()
					.add(R.id.container, mContainerFragment).commit();
			mContainerFragment.setUrl("https://ko-train.meteor.com");
		}
		gcm = GoogleCloudMessaging.getInstance(this);
		regid = getRegistrationId(context);

		if (regid.isEmpty()) {
			registerInBackground();
		} else {
			Log.d("CrowdTrain", regid);
		}
	}

	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			return "";
		}
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			return "";
		}
		return registrationId;
	}

	private SharedPreferences getGCMPreferences(Context context) {
		return getSharedPreferences(MainActivity.class.getSimpleName(),
				Context.MODE_PRIVATE);
	}

	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public static class ContainerFragment extends Fragment {
		private WebView mMainWV;
		private String mUrl;

		public ContainerFragment() {

		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			mMainWV = (WebView) rootView.findViewById(R.id.MainWV);
			mMainWV.setWebViewClient(new WebViewClient());
			WebSettings settings = mMainWV.getSettings();
			settings.setJavaScriptEnabled(true);
			settings.setDomStorageEnabled(true);
			mMainWV.loadUrl(mUrl);
			return rootView;
		}

		public void setUrl(String url) {
			mUrl = url;
			if (mMainWV != null)
				mMainWV.loadUrl(url);
		}
	}

	private void registerInBackground() {
		RegisterAsyncTask task = new RegisterAsyncTask();
		task.execute(null, null, null);
	}

	public class RegisterAsyncTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			String msg = "";
			try {
				if (gcm == null) {
					gcm = GoogleCloudMessaging.getInstance(context);
				}
				String regidNew = gcm.register(SENDER_ID);
				sendRegistrationIdToBackend(regidNew);
				storeRegistrationId(context, regidNew);
				Log.d("CrowdTrain", regidNew);
			} catch (IOException ex) {
				msg = "Error :" + ex.getMessage();
			}
			return msg;
		}

		private void storeRegistrationId(Context context, String regId) {
			regid = regId;
			final SharedPreferences prefs = getGCMPreferences(context);
			int appVersion = getAppVersion(context); 
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(PROPERTY_REG_ID, regId);
			editor.putInt(PROPERTY_APP_VERSION, appVersion);
			editor.commit();
		}

		private boolean sendRegistrationIdToBackend(String regId) {
			String server = "http://ko-train.meteor.com/collectionapi/androidsubs?registrationId=" +regId ;
			//String server = "http://192.168.123.105:3000/collectionapi/androidsubs?registrationId=" +regId ;

			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpGet httppost = new HttpGet(server);

			 
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
						1);
				//nameValuePairs.add(new BasicNameValuePair("registrationId",						regId));
				//httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,						"UTF-8"));
				HttpResponse response = httpclient.execute(httppost); 
				String str= response.getStatusLine().toString();

			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}

	}
}
