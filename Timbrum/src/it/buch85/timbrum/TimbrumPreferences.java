package it.buch85.timbrum;

import android.content.SharedPreferences;

public class TimbrumPreferences {
	private SharedPreferences sharedPrefs;

	public TimbrumPreferences(SharedPreferences sharedPrefs) {
		this.sharedPrefs = sharedPrefs;
	}
	
	public String getUsername() {
		return sharedPrefs.getString("pref_username", null);
	}

	public String getPassword() {
		return sharedPrefs.getString("pref_password", null);
	}

	public String getHost() {
		return sharedPrefs.getString("pref_host", null);
	}
	
	public boolean arePreferencesValid(){
		return isHostValid() && isUsernameValid() && isPasswordValid(); 
	}

	private boolean isUsernameValid() {
		return getUsername()!=null && !getUsername().isEmpty();
	}
	
	private boolean isPasswordValid() {
		return getPassword()!=null && !getPassword().isEmpty();
	}

	private boolean isHostValid() {
		return getHost()!=null && !getHost().isEmpty();
	}
}
