package alireza.com.othello.server

import android.os.AsyncTask
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class HttpAsyncTask : AsyncTask<String,String,String>(){

    /*
            0 -> POST register user
            1 -> GET online friends
            2 -> POST i'm online
            3 -> POST i'm new player
            4 -> POST i want to login
            5 -> PUT update username
            6 -> PUT login user

            p0[1] = url address

            p0[2] = json to send if needed

            //GET
            //POST (add new person)
            //PUT (updateING)
            //DELETE


   */



    override fun doInBackground(vararg p0: String?): String {
        try {
            Log.i("myUrl",p0[1])
            val url = URL(p0[1])
            val urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.connectTimeout = 1000
            when (p0[0]) {
                "0" ->{
                    urlConnection.requestMethod = "POST"
                    urlConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8")
                    urlConnection.setRequestProperty("Accept", "application/json")
                    urlConnection.doInput = true
                    urlConnection.doOutput = true

                    val os = DataOutputStream(urlConnection.outputStream)
                    os.writeBytes(p0[2])

                    os.flush()
                    os.close()
                    val responseCode = urlConnection.responseCode
                    if(responseCode == 200){
                        publishProgress("0",urlConnection.responseMessage)
                        //every thing is good
                    }else{
                        Log.i("STATUS", urlConnection.responseCode.toString())
                        Log.i("MSG", urlConnection.responseMessage)
                        //ridi
                    }
                }
                "1" -> {
                    urlConnection.requestMethod = "GET"
                    urlConnection.doOutput = true

                    val inputStream = urlConnection.inputStream

                    val str = streamToString(inputStream)

                    publishProgress(str)

                }
                "2" -> {
                    urlConnection.requestMethod = "POST"
                    urlConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8")
                    urlConnection.setRequestProperty("Accept", "application/json")
                    urlConnection.doOutput = true
                    urlConnection.doInput = true

                    Log.i("JSON", p0[2].toString())

                    val os = DataOutputStream(urlConnection.outputStream)
                    os.writeBytes(p0[2].toString())

                    os.flush()
                    os.close()

                    val responseCode = urlConnection.responseCode
                    if(responseCode == 200){
                        //every thing is good
                        Log.i("STATUS", urlConnection.responseCode.toString())
                        Log.i("MSG", urlConnection.responseMessage)
                    }else{
                        //ridi
                    }

                }

            }

            urlConnection.disconnect()

        } catch (ex: Exception) {
            ex.printStackTrace()
        }


        return ""
    }

    override fun onProgressUpdate(vararg values: String?) {

        when(values[0]) {
            "0" -> {
                val json = JSONObject(values[1])
                val secretId = json.getString("_id")
                val name = json.getString("name")
                val email = json.getString("email")
                val isGold = json.getBoolean("isGold")
                val coins = json.getInt("Coins")
                Log.i("registerd player","name $name - email $email -coins $coins")
                //todo save secret id and other
            }
        }

        Log.i("done", values[0])

    }


    private fun streamToString(stream: InputStream): String {
        var str = ""

        try {
            val reader = BufferedReader(InputStreamReader(stream))
            str = reader.readText()
            reader.close()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }


        return str
    }


}