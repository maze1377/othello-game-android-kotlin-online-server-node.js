package alireza.com.othello

import alireza.com.othello.constant.*
import alireza.com.othello.server.CheckState
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class Login : AppCompatActivity() {


    private lateinit var emailView : EditText
    private lateinit var passwordView : EditText
    private lateinit var progressBarView : ProgressBar
    private lateinit var sign : Button
    private var lang = "en"
    private var theme : String = " "
    private fun getSetting(){
        val data = getSharedPreferences(SETTING, Context.MODE_PRIVATE)
        val sw = data.getBoolean(EXIST,false)
        if(sw) {
            lang = data.getString(LANGUAGE,"en")
            theme = data.getString(THEME,"default")
        }
    }

    private fun loadLang() {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSetting()
        loadLang()
        setContentView(R.layout.activity_login)

        this.emailView = email
        this.passwordView = password
        this.progressBarView = progressBar
        this.sign = email_sign_in_button

        sign.setOnClickListener {
            val networkState = CheckState().isConnected(this)
            if (networkState) {
                progressBarView.visibility = ProgressBar.VISIBLE
                var txt = emailView.text.toString()
                if (txt.length > 4 && txt.contains("@")) {
                    txt = passwordView.text.toString()
                    if (txt.length > 4) {
                        progressBarView.visibility = ProgressBar.VISIBLE
                        sendToServer()
                    } else {
                        Toast.makeText(this, "Invalid Password", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Invalid Email", Toast.LENGTH_LONG).show()
                }
            }else{
                Toast.makeText(this, "Check Internet Connection", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun saveData(username : String,secretID : String) {
        val data = getSharedPreferences(PLAYER_INFO, Context.MODE_PRIVATE)
        val editor = data.edit()
        editor.putBoolean(EXIST,true)
        editor.putString(PLAYER_NAME,username)
        editor.putString(PLAYER_SECRET_ID,secretID)
        editor.putString(PLAYER_EMAIL,emailView.text.toString())
        editor.putString(PLAYER_PASSWORD,passwordView.text.toString())
        val sw = editor.commit()
        if(sw){
            Toast.makeText(this,"data saved", Toast.LENGTH_LONG).show()
            openMenu()
        }else{
            Log.i("Login","DATA NOT SAVED")
        }
    }

    private fun sendToServer() {

        val server = Register()
        val url = "$URL_STR/api/users/login"
        val json = makeJson()
        server.execute("0", url, json.toString())
    }

    private fun makeJson() : JSONObject {

        val json = JSONObject()
        json.put("email",emailView.text.toString())
        json.put("password",passwordView.text.toString())
        return json
    }

    @SuppressLint("StaticFieldLeak")
    inner class Register : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg p0: String?): String {
            try {
                Log.i("myUrl", p0[1])
                val url = URL(p0[1])
                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.connectTimeout = 1000

                urlConnection.requestMethod = "PUT"
                urlConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8")
                urlConnection.setRequestProperty("Accept", "application/json")
                urlConnection.doInput = true
                urlConnection.doOutput = true

                val os = DataOutputStream(urlConnection.outputStream)
                os.writeBytes(p0[2])
                os.flush()
                os.close()

                val responseCode = urlConnection.responseCode
                if (responseCode == 200) {
                    val str = streamToString(urlConnection.inputStream)
                    publishProgress(str)
                    //every thing is good
                } else {
                    //TODO handel error
                    progressBarView.visibility = ProgressBar.INVISIBLE
                    Log.i("STATUS", urlConnection.responseCode.toString())
                    Log.i("MSG", urlConnection.responseMessage)
                    Log.i("MSG", streamToString(urlConnection.errorStream))
                }

            } catch (e: Exception) {

            }

            return " "

        }

        override fun onProgressUpdate(vararg values: String?) {
            Log.i("json send to me",values[0])
            val json = JSONObject(values[0])
            val secretId = json.getString("_id")
            val name = json.getString("name")
            saveData(name,secretId)

        }

        override fun onPostExecute(result: String?) {
        }
    }

    private fun openMenu(){
        Toast.makeText(this, "welcome", Toast.LENGTH_LONG).show()
        val intent = Intent(this, Menu::class.java)
        startActivity(intent)
        finish()
    }

}
