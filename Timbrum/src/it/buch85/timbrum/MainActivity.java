package it.buch85.timbrum;

import it.buch85.timbrum.request.Records;
import it.buch85.timbrum.request.Records.Record;

import java.util.Date;

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
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
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
		Toast.makeText(this, "Exit", 3).show();
	}

	protected void enter() {
		Toast.makeText(this, "Enter", 3).show();
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
		new AsyncTask<String,Void,Records>(){
			@Override
			protected Records doInBackground(String... params) {
				Timbrum timbrum=new Timbrum(timbrumPreferences.getHost(),timbrumPreferences.getUsername(),timbrumPreferences.getPassword());
				try {
					if(timbrum.login()){
						return timbrum.getReport(new Date());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}finally{
					timbrum.close();
				}
				return null;
			}
			
			@Override
			protected void onPostExecute(Records result) {
				if(result!=null){
					listView.setAdapter(new ArrayAdapter<Record>(MainActivity.this, R.layout.row,result.getData()));
				}
			};
		}.execute();
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
