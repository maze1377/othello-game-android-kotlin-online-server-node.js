package alireza.com.othello.server;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class test {


    public String test(){

        HttpURLConnection connection = null;
        InputStream inputStream = null;
        StringBuffer stringBuffer = new StringBuffer();

        try{
            connection = (HttpURLConnection) new URL("http://cryptic-ravine-76227.herokuapp.com/test").openConnection();
            Log.i("test","runed1");
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            Log.i("test","runed3");
            connection.connect();
            Log.i("test","runed2");
            //Read the response

            inputStream = connection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line  ;

            while ((line = bufferedReader.readLine()) != null){
                    stringBuffer.append(line + "\r\n");
            }

            connection.disconnect();
            inputStream.close();

        }catch (Exception e ){
            Log.e("cathc","here");
            e.printStackTrace();
        }

        return stringBuffer.toString();

    }

    public String sendTest(){
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        StringBuffer stringBuffer = new StringBuffer();

        try{
            connection = (HttpURLConnection) new URL("").openConnection();
            connection.setRequestMethod("POST");


        }catch (Exception e ){
            e.printStackTrace();
        }


        return null;
    }

}
