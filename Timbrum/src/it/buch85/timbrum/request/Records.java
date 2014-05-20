package it.buch85.timbrum.request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by mbacer on 16/04/14.
 */
public class Records {
    String[] headers;
    ArrayList<String[]> data;
    ArrayList<Record> records=new ArrayList<>();

    Records(String[] headers, ArrayList<String[]> data) {
        this.headers = headers;
        this.data = data;
        
        for(int i=0;i<data.size();i++){
        	Record r=new Record(data.get(i));
        	records.add(r);
        }
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(Arrays.toString(headers));
        buffer.append("\n");
        buffer.append("\n");
        for (String[] record : data) {
            buffer.append(Arrays.toString(record));
            buffer.append("\n");
        }

        return buffer.toString();
    }

    public String[] getHeaders() {
        return headers;
    }

    public ArrayList<Record> getData() {
        return records;
    }
    
    public class Record{
		private String[] strings;
		public Record(String[] strings) {
			this.strings = strings;
		}
		@Override
		public String toString() {
			StringBuffer buffer=new StringBuffer();
			for(int i=0;i<strings.length;i++){
				buffer.append(headers[i] + " "+ strings[i]);
				if(i==strings.length-1){
					buffer.append("\n");
				}
			}
			return buffer.toString();
		}
    	
    }
}
