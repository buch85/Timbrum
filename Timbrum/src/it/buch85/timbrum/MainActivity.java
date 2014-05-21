package it.buch85.timbrum;

import it.buch85.timbrum.request.LoginRequest.LoginResult;
import it.buch85.timbrum.request.RecordTimbratura;
import it.buch85.timbrum.request.TimbraturaRequest;

import java.util.ArrayList;
import java.util.Date;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

	private Button buttonEnter;
	private Button buttonExit;
	private TimbrumPreferences timbrumPreferences;
	private ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			timbrumPreferences=new TimbrumPreferences(PreferenceManager.getDefaultSharedPreferences(this));
			getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
			buttonEnter = (Button) findViewById(R.id.button_enter);
			buttonExit = (Button) findViewById(R.id.button_exit);
			listView= (ListView)findViewById(R.id.listView1);
			buttonEnter.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					enter();
				}
			});
			buttonExit.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					exit();
				}
			});
			
		}
		enableDisableButtons();
	}

	protected void exit() {
		new TimbrumTask(TimbraturaRequest.VERSO_USCITA).execute();
		
	}

	protected void enter() {
		new TimbrumTask(TimbraturaRequest.VERSO_ENTRATA).execute();
	}

	private void enableDisableButtons() {
		boolean arePreferencesValid = timbrumPreferences.arePreferencesValid();
		buttonEnter.setEnabled(arePreferencesValid);
		buttonExit.setEnabled(arePreferencesValid);
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
		enableDisableButtons();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if(timbrumPreferences.arePreferencesValid()){
			new TimbrumTask().execute();
		}
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

	private final class TimbrumTask extends AsyncTask<String, String, ArrayList<RecordTimbratura>> {
		String versoTimbratura=null;
		private ProgressDialog progressDialog;
		
		String message="";
		private Timbrum timbrum;
		public TimbrumTask() {
			this(null);
		}
		
		public TimbrumTask(String timbratura) {
			this.versoTimbratura=timbratura;
			progressDialog=new ProgressDialog(MainActivity.this);
			progressDialog.setTitle("Loading");
			progressDialog.setMessage("Please wait...");
			timbrum=new Timbrum(timbrumPreferences.getHost(),timbrumPreferences.getUsername(),timbrumPreferences.getPassword());
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
				publishProgress("Logging in...");
				LoginResult loginResult=timbrum.login();
				if(loginResult.isSuccess()){
					if(versoTimbratura!=null){
						publishProgress("Sto timbrando...");
						timbrum.timbra(versoTimbratura);
						if(versoTimbratura.equals(TimbraturaRequest.VERSO_ENTRATA)){
							publishProgress("Entrato");
						}else if(versoTimbratura.equals(TimbraturaRequest.VERSO_ENTRATA)){
							publishProgress("Uscito");
						}
					}
					publishProgress("Caricamento info...");
					return timbrum.getReport(new Date());
				}else{
					message="Login Error: " +loginResult.getMessage();
				}
			} catch (Exception e) {
				message="Error: " +e.getMessage();
			}finally{
				timbrum.close();
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			for(String value:values){
				progressDialog.setMessage(value);
			}
		}

		@Override
		protected void onPostExecute(ArrayList<RecordTimbratura> result) {
			
			
			progressDialog.dismiss();
			if(result!=null){
				listView.setAdapter(new ArrayAdapter<RecordTimbratura>(MainActivity.this, R.layout.row,R.id.textViewList,result));
			}else{
				Toast.makeText(MainActivity.this, message, 5).show();
			}
		}
		
		@Override
		protected void onCancelled() {
			cancel(true);
			progressDialog.dismiss();
		}

		
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
		
		
	}
	


}
