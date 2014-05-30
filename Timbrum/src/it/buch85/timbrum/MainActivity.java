package it.buch85.timbrum;

import it.buch85.timbrum.request.LoginRequest.LoginResult;
import it.buch85.timbrum.request.RecordTimbratura;
import it.buch85.timbrum.request.TimbraturaRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

	private Button buttonEnter;
	private Button buttonExit;
	private TimbrumPreferences timbrumPreferences;
	private ListView listView;
	private ToggleButton buttonSafeExit;
	private ToggleButton buttonSafeEnter;
	private SafeButtonManager safeButtonManager;
	private Button buttonRefresh;

	/** The view to show the ad. */
	private AdView adView;
	public static MainActivity instance;

	class SafeButtonManager {
		HashMap<Button, ToggleButton> map = new HashMap<Button, ToggleButton>();

		private void setupSafe(final ToggleButton buttonSafe,
				final Button button) {
			button.setEnabled(false);
			buttonSafe.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							button.setEnabled(isChecked);
						}
					});
			map.put(button, buttonSafe);
		}

		private void resetSafe(final Button button) {
			button.setEnabled(false);
			ToggleButton toggleButton = map.get(button);
			if (toggleButton != null) {
				toggleButton.setChecked(false);
			}
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance=this;
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			safeButtonManager = new SafeButtonManager();
			timbrumPreferences = new TimbrumPreferences(
					PreferenceManager.getDefaultSharedPreferences(this));
			// getSupportFragmentManager().beginTransaction().add(R.id.container,
			// new PlaceholderFragment()).commit();
			buttonRefresh = (Button) findViewById(R.id.buttonRefresh);
			buttonEnter = (Button) findViewById(R.id.button_enter);
			buttonExit = (Button) findViewById(R.id.button_exit);
			buttonSafeEnter = (ToggleButton) findViewById(R.id.button_safe_enter);
			buttonSafeExit = (ToggleButton) findViewById(R.id.button_safe_exit);
			listView = (ListView) findViewById(R.id.listView1);
			safeButtonManager.setupSafe(buttonSafeEnter, buttonEnter);
			safeButtonManager.setupSafe(buttonSafeExit, buttonExit);
			buttonEnter.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					safeButtonManager.resetSafe(buttonEnter);
					enter();
				}
			});
			buttonExit.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					safeButtonManager.resetSafe(buttonExit);
					exit();
				}
			});

			buttonRefresh.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					refresh();
				}
			});
			// Look up the AdView as a resource and load a request.
		    AdView adView = (AdView)this.findViewById(R.id.adMobadView);
		    AdRequest adRequest = new AdRequest.Builder().build();
		    adView.loadAd(adRequest);
		}
		enableDisableButtons();
	}

	protected void refresh() {
		new TimbrumTask().execute();
	}

	protected void exit() {
		new TimbrumTask(TimbraturaRequest.VERSO_USCITA).execute();

	}

	protected void enter() {
		new TimbrumTask(TimbraturaRequest.VERSO_ENTRATA)
				.execute();
	}

	private void enableDisableButtons() {
		boolean arePreferencesValid = timbrumPreferences.arePreferencesValid();
		buttonRefresh.setEnabled(arePreferencesValid);
		buttonSafeEnter.setEnabled(arePreferencesValid);
		buttonSafeExit.setEnabled(arePreferencesValid);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (adView != null) {
			adView.resume();
		}
		enableDisableButtons();
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (!paused && timbrumPreferences.arePreferencesValid()) {
			new TimbrumTask().execute();
		}
	}

	boolean paused = false;

	@Override
	protected void onPause() {
		paused = true;
		if (adView != null) {
			adView.pause();
		}
		super.onPause();
	}

	/** Called before the activity is destroyed. */
	@Override
	public void onDestroy() {
		// Destroy the AdView.
		if (adView != null) {
			adView.destroy();
		}
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	private final class TimbrumTask extends
			AsyncTask<String, String, ArrayList<RecordTimbratura>> {
		String versoTimbratura = null;
		private ProgressDialog progressDialog;

		String message = "";
		private Timbrum timbrum;

		public TimbrumTask() {
			this(null);
		}

		public TimbrumTask(String timbratura) {
			this.versoTimbratura = timbratura;
			progressDialog = new ProgressDialog(MainActivity.this);
			progressDialog.setCancelable(false);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.setTitle(getString(R.string.loading));
			progressDialog.setMessage(getString(R.string.please_wait));
			timbrum = new Timbrum(timbrumPreferences.getHost(),
					timbrumPreferences.getUsername(),
					timbrumPreferences.getPassword());
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			listView.setAdapter(null);
			progressDialog.show();
		}

		@Override
		protected ArrayList<RecordTimbratura> doInBackground(String... params) {
			try {
				publishProgress(getString(R.string.logging_in));
				LoginResult loginResult = timbrum.login();
				if (loginResult.isSuccess()) {
					if (versoTimbratura != null) {
						publishProgress(getString(R.string.timbrum_in_progress));
						timbrum.timbra(versoTimbratura);
						if (versoTimbratura
								.equals(TimbraturaRequest.VERSO_ENTRATA)) {
							publishProgress(getString(R.string.entered));
						} else if (versoTimbratura
								.equals(TimbraturaRequest.VERSO_ENTRATA)) {
							publishProgress(getString(R.string.exited));
						}
					}
					publishProgress(getString(R.string.loading_logs));
					return timbrum.getReport(new Date());
				} else {
					message = getString(R.string.login_error)+ loginResult.getMessage();
				}
			} catch (Exception e) {
				message = getString(R.string.error) + e.getMessage();
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			for (String value : values) {
				progressDialog.setMessage(value);
			}
		}

		@Override
		protected void onPostExecute(ArrayList<RecordTimbratura> result) {
			progressDialog.dismiss();
			if (result != null) {
				listView.setAdapter(new ArrayAdapter<RecordTimbratura>(
						MainActivity.this, R.layout.row, R.id.textViewList,
						result));
			} else {
				Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
			}
		}

		@Override
		protected void onCancelled() {
			cancel(true);
			progressDialog.dismiss();
		}

	}

}
