package it.buch85.timbrum;

import it.buch85.timbrum.prefs.SettingsActivity;
import it.buch85.timbrum.prefs.TimbrumPreferences;
import it.buch85.timbrum.request.LoginRequest.LoginResult;
import it.buch85.timbrum.request.RecordTimbratura;
import it.buch85.timbrum.request.TimbraturaRequest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private TimbrumPreferences timbrumPreferences;
	private ListView listView;
	private Button buttonRefresh;

	/** The view to show the ad. */
	private AdView adView;
	private TextView workedText;
	private TextView remainingText;
	private TextView remainingLabel;
	private SeekBar seekBar;
	public static MainActivity instance;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			timbrumPreferences = new TimbrumPreferences(PreferenceManager.getDefaultSharedPreferences(this));
			// getSupportFragmentManager().beginTransaction().add(R.id.container,
			// new PlaceholderFragment()).commit();
			seekBar = (SeekBar) findViewById(R.id.seekBar1);
			buttonRefresh = (Button) findViewById(R.id.buttonRefresh);
			listView = (ListView) findViewById(R.id.listView1);
			workedText = (TextView) findViewById(R.id.textWorked);
			remainingText = (TextView) findViewById(R.id.textRemaining);
			remainingLabel = (TextView) findViewById(R.id.textRemainingLabel);
			seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					int progress = seekBar.getProgress();
					if (progress == 0) {
						exit();
					} else if (progress == seekBar.getMax()) {
						enter();
					}
					seekBar.setProgress(seekBar.getMax() / 2);
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
				}

				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				}
			});

			buttonRefresh.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					refresh();
				}
			});
			if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.FROYO) {
				// only for gingerbread and newer versions

				// Look up the AdView as a resource and load a request.
				AdView adView = (AdView) this.findViewById(R.id.adMobadView);
				AdRequest adRequest = new AdRequest.Builder().build();
				adView.loadAd(adRequest);
			}
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
		new TimbrumTask(TimbraturaRequest.VERSO_ENTRATA).execute();
	}

	private void enableDisableButtons() {
		boolean arePreferencesValid = timbrumPreferences.arePreferencesValid();
		buttonRefresh.setEnabled(arePreferencesValid);
		seekBar.setEnabled(arePreferencesValid);
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

	public static String formatTime(long millis) {
		long minute = (long) ((millis / (1000d * 60)) % 60);
		long hour = (long) ((millis / (1000d * 60 * 60)) % 24);

		String time = String.format(Locale.ENGLISH, "%02d:%02d", hour, minute);
		return time;
	}

	
	private final class TimbrumTask extends AsyncTask<String, String, ArrayList<RecordTimbratura>> {
		String versoTimbratura = null;
		private ProgressDialog progressDialog;

		String message = "";
		private Timbrum timbrum;
		protected boolean isConfirmed=true;

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
			timbrum = new Timbrum(timbrumPreferences.getHost(), timbrumPreferences.getUsername(), timbrumPreferences.getPassword());
		}

		@Override
		protected void onPreExecute() {
			listView.setAdapter(null);
			progressDialog.show();
		}

		@Override
		protected ArrayList<RecordTimbratura> doInBackground(String... params) {
			try {
				publishProgress( getString(R.string.logging_in));
				LoginResult loginResult = timbrum.login();
				if (loginResult.isSuccess()) {
					Date now = new Date();
					if (versoTimbratura != null) {
						publishProgress(getString(R.string.loading_logs));
						ArrayList<RecordTimbratura> report = timbrum.getReport(now);
						if (exitAsFirstTimbrum(report) || doubleTimbrum(report) ) {
							isConfirmed=false;
							final CountDownLatch latch=new CountDownLatch(1);
							final DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
							    public void onClick(DialogInterface dialog, int whichButton) {
							    	if(whichButton==1){
							    		isConfirmed=true;
							    	}else{
							    		isConfirmed=false;
							    	}
							    	latch.countDown();
							    }};
							MainActivity.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									createConfirmationDialog(versoTimbratura,onClickListener).show();
								}
							});
							latch.await();
						}
						if(isConfirmed){
							publishProgress(getString(R.string.timbrum_in_progress));
							timbrum.timbra(versoTimbratura);
						}else{
							return report;
						}
					}
					publishProgress(getString(R.string.loading_logs));
					return timbrum.getReport(now);
				} else {
					message = getString(R.string.login_error) + loginResult.getMessage();
				}
			} catch (Exception e) {
				message = getString(R.string.error) + e.getMessage();
			}
			return null;
		}

		private AlertDialog createConfirmationDialog(String direction,DialogInterface.OnClickListener onClickListener) {
			String title= direction.equals(TimbraturaRequest.VERSO_ENTRATA)?getString(R.string.confirm_entry_title):getString(R.string.confirm_exit_title);
			String message= direction.equals(TimbraturaRequest.VERSO_ENTRATA)?getString(R.string.confirm_entry_message):getString(R.string.confirm_exit_message);
			AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
			.setTitle(title)
			.setMessage(message)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setPositiveButton(android.R.string.yes, onClickListener)
			.setNegativeButton(android.R.string.no, onClickListener).create();
			return dialog;
		}

		private boolean doubleTimbrum(ArrayList<RecordTimbratura> report) {
			if(report.isEmpty()){
				return false;
			}else{
				RecordTimbratura recordTimbratura = report.get(report.size()-1);
				return recordTimbratura.getDirection().equals(versoTimbratura);
			}
		}

		private boolean exitAsFirstTimbrum(ArrayList<RecordTimbratura> report) {
			return report.isEmpty() && TimbraturaRequest.VERSO_USCITA.equals(versoTimbratura);
		}

		@Override
		protected void onProgressUpdate(String... values) {
			for (String value : values) {
				progressDialog.setMessage(value);
			}
		}

		@Override
		protected void onPostExecute(ArrayList<RecordTimbratura> result) {
			progressDialog.dismiss();
			remainingLabel.setText(getString(R.string.remaining));
			if (result == null) {
				Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
				return;
			}
			updateView(result);
		}

		private void updateView(ArrayList<RecordTimbratura> result) {
			if (result.size() == 0) {
				workedText.setText(getString(R.string.n_a));
				remainingText.setText(getString(R.string.n_a));
			} else {
				listView.setAdapter(new ArrayAdapter<RecordTimbratura>(MainActivity.this, R.layout.row, R.id.textViewList, result));
				if (validateRecords(result)) {
					Date now = now();
					Date latestExit = now;
					if (result.get(result.size() - 1).isExit()) {
						latestExit = result.get(result.size() - 1).getTimeFor(now);
					}
					long worked = getWorkedTime(result, now, latestExit);
					long millisToWorkADay = timbrumPreferences.getTimeToWork();
					workedText.setText(formatTime(worked));
					long remaining = millisToWorkADay - worked;
					if (remaining < 0) {
						remainingLabel.setText(getString(R.string.exceeding));
						remainingText.setText(formatTime(-remaining));
					} else {
						remainingText.setText(formatTime(remaining));
					}
				} else {
					workedText.setText(getString(R.string.n_a));
					remainingText.setText(getString(R.string.n_a));
				}
			}
		}

		private long getWorkedTime(ArrayList<RecordTimbratura> result, Date now, Date latestExit) {
			long worked = latestExit.getTime() - result.get(0).getTimeFor(now).getTime();
			for (int i = 1; i < result.size(); i++) {
				RecordTimbratura recordTimbratura = result.get(i);
				if (recordTimbratura.getDirection().equals(TimbraturaRequest.VERSO_ENTRATA)) {
					RecordTimbratura prev = result.get(i - 1);
					long pausa = recordTimbratura.getTimeFor(now).getTime() - prev.getTimeFor(now).getTime();
					worked -= pausa;
				}
			}
			return worked;
		}

		private Date now() {
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			c.set(Calendar.SECOND, 0);
			Date now = c.getTime();
			return now;
		}

		private boolean validateRecords(ArrayList<RecordTimbratura> result) {
			String check = TimbraturaRequest.VERSO_ENTRATA;
			for (RecordTimbratura record : result) {
				if (!check.equals(record.getDirection())) {
					return false;
				}
				check = check == TimbraturaRequest.VERSO_ENTRATA ? TimbraturaRequest.VERSO_USCITA : TimbraturaRequest.VERSO_ENTRATA;
			}
			return true;
		}

		@Override
		protected void onCancelled() {
			cancel(true);
			progressDialog.dismiss();
		}
	}
}
