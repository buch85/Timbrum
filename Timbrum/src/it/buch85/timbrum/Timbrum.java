package it.buch85.timbrum;

import it.buch85.timbrum.request.LoginRequest;
import it.buch85.timbrum.request.Records;
import it.buch85.timbrum.request.ReportRequest;
import it.buch85.timbrum.request.TimbraturaRequest;

import java.io.IOException;
import java.util.Date;

import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;

import android.net.http.AndroidHttpClient;

/**
 * Created by mbacer on 23/04/14.
 */
public class Timbrum {


    public static String LOGIN_URL             = "/servlet/cp_login";
    public static String TIMBRUS_URL           = "/servlet/ushp_ftimbrus";
    public static String SQL_DATA_PROVIDER_URL = "/servlet/SQLDataProviderServer";

    private final String              username;
    private final String              password;
    private final AndroidHttpClient httpclient;
	private BasicHttpContext context;
	private String host;

    public Timbrum(String host, String username, String password) {
        this.host = host;
		this.username = username;
        this.password = password;
        
        context=new BasicHttpContext();
        BasicCookieStore cookieStore=new BasicCookieStore();
        context.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        httpclient = AndroidHttpClient.newInstance("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11");
    }

    public Records getReport(Date date) throws Exception {
        ReportRequest report = new ReportRequest(httpclient,context);
        report.setUrl(host+SQL_DATA_PROVIDER_URL);
        return report.getTimbrature(new Date());
    }

    public boolean login() throws IOException {
        LoginRequest login = new LoginRequest(httpclient,context);
        login.setUrl(host+LOGIN_URL);
        login.setUsername(username);
        login.setPassword(password);
        return login.submit();
    }


    public void timbra(String verso) throws IOException {
        TimbraturaRequest timbratura = new TimbraturaRequest(httpclient,context);
        timbratura.setUrl(host+TIMBRUS_URL);
        if (TimbraturaRequest.VERSO_ENTRATA.equals(verso)) {
            timbratura.entrata();
        } else {
            timbratura.uscita();
        }
    }

    public void close() {
        httpclient.close();
    }
}
