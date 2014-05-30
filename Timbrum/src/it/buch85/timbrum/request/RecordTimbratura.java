package it.buch85.timbrum.request;

import it.buch85.timbrum.MainActivity;
import it.buch85.timbrum.R;


public class RecordTimbratura {
	private String[] strings;
	private String[] headers;
	public RecordTimbratura(String[] strings,String[] headers) {
		this.strings = strings;
		this.headers = headers;
	}
	
	//DAYSTAMP, TIMETIMBR, DIRTIMBR, CAUSETIMBR, TYPETIMBR, IPTIMBR
	@Override
	public String toString() {
		int timeIndex =getIndexFor("TIMETIMBR");
		int dirIndex =getIndexFor("DIRTIMBR");
		String time= strings[timeIndex];
		String dir= strings[dirIndex];
		String message= time + " "+ (dir.equals(TimbraturaRequest.VERSO_ENTRATA)?MainActivity.instance.getString(R.string.entry):MainActivity.instance.getString(R.string.exit));
		return message;
	}

	private int getIndexFor(String string) {
		for(int i=0;i<=headers.length;i++){
			if(headers[i].equals(string)){
				return i;
			}
		}
		return -1;
	}
}
