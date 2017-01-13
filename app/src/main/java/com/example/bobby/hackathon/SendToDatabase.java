package com.example.bobby.hackathon;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.InputStream;
import java.util.ArrayList;

public class SendToDatabase extends AsyncTask {

    String Geraete_ID = "3";
    String Beschreibung ="Klopapierbeacon";
    String Gebaeude_ID = "3";
    String Raum_ID = "3";
    String Kabinen_ID = "3";
    InputStream is = null;
    String result = null;
    String line = null;
    int code;

    @Override
    protected Object doInBackground(Object[] params) {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        String id = (String) params[0];
        String fuellstand = (String) params[1];
        //STRING


        nameValuePairs.add(new BasicNameValuePair("Geraete_ID", id));
        nameValuePairs.add(new BasicNameValuePair("Fuellstand", fuellstand ));





        try {
            HttpClient httpclient = new DefaultHttpClient();
            // Bei Bedarf IP-Adresse austauschen
            HttpPost httppost = new HttpPost("http://192.168.0.5/Klopapierspender.php");
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
            Log.e("pass 1", "connection success ");

        } catch (Exception e) {
            Log.e("Fehler", e.toString());
        }



        return null;
    }

}
