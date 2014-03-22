package com.ayushhgoyal.oauth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class Home extends Activity implements OnClickListener {
	Button login, logout, sendmail;
	Context context;
	public static final String TOKEN_SHAREDPREF = "token_sharedpref";

	AccountManager accountManager;
	Account account;

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		setContext(this);

		login = (Button) findViewById(R.id.login);
		logout = (Button) findViewById(R.id.logout);
		sendmail = (Button) findViewById(R.id.send);

		login.setOnClickListener(this);
		logout.setOnClickListener(this);
		sendmail.setOnClickListener(this);

		accountManager = AccountManager.get(getContext());
		// by default we are doing stuff with first account configured
		account = accountManager.getAccountsByType("com.google")[0];

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	@Override
	public void onClick(View arg0) {

		switch (arg0.getId()) {
			case R.id.login :

				try {
					accountManager.invalidateAuthToken(
							"com.google",
							getContext()
									.getSharedPreferences(TOKEN_SHAREDPREF,
											MODE_WORLD_READABLE)
									.getString(TOKEN_SHAREDPREF, null)
									.toString());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				accountManager.getAuthToken(account,
						"oauth2:https://mail.google.com/", null, this,
						new OnTokenAcquired(), null);

				break;

			case R.id.send :
				accountManager.getAuthToken(account,
						"oauth2:https://mail.google.com/", null, this,
						new OnTokenAcquired(), null);
				new SendEmailTask().execute(getContext().getSharedPreferences(
						TOKEN_SHAREDPREF, getContext().MODE_WORLD_READABLE)
						.getString(TOKEN_SHAREDPREF, null));

				break;

			case R.id.logout :
				getContext()
						.getSharedPreferences(TOKEN_SHAREDPREF,
								MODE_WORLD_WRITEABLE).edit()
						.putString(TOKEN_SHAREDPREF, null).commit();
				break;

			default :
				break;
		}

	}
	class OnTokenAcquired implements AccountManagerCallback<Bundle> {
		@Override
		public void run(AccountManagerFuture<Bundle> result) {
			try {
				Bundle bundle = result.getResult();
				String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);

				// save token in sharedpref for future usage
				SharedPreferences pref = getContext().getSharedPreferences(
						TOKEN_SHAREDPREF, getContext().MODE_WORLD_WRITEABLE);
				pref.edit().putString(TOKEN_SHAREDPREF, token).commit();

			} catch (Exception e) {
				Log.d("test", e.getMessage());
			}
		}
	}

	private class SendEmailTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			if ((params[0]) != null) {

				new GMailOauthSender().sendMail("CHeck subject", // subject
						"body here : Winter is coming",// body
						accountManager.getAccountsByType("com.google")[0].name
								.toString().trim(), // user
						params[0], // oAuth token
						"ayushhgoyal@gmail.com, ayushhgoyal@live.in") // recepients

				;

				Log.e("User: ",
						accountManager.getAccountsByType("com.google")[0].name
								.toString().trim());

				return "done";
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result == null) {
				Toast.makeText(getContext(), "Not logged in",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(
						getContext(),
						"Mail sent to "
								+ accountManager
										.getAccountsByType("com.google")[0].name
										.toString().trim(), Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

}
