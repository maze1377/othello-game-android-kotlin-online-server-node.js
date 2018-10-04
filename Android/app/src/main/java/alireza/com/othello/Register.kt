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
import kotlinx.android.synthetic.main.activity_register.*
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class Register : AppCompatActivity() {

    private lateinit var emailView : EditText
    private lateinit var passwordView : EditText
    private lateinit var username : EditText
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
        setContentView(R.layout.activity_register)

        this.emailView = email
        this.passwordView = password
        this.username = userName
        this.progressBarView = progressBar
        this.sign = email_sign_in_button

        sign.setOnClickListener {
            val networkState = CheckState().isConnected(this)
            if (networkState) {
                var txt = emailView.text.toString()
                if (txt.length > 4 && txt.contains("@")) {
                    txt = passwordView.text.toString()
                    if (txt.length > 4) {
                        txt = username.text.toString()
                        if (txt.length > 2) {
                            progressBarView.visibility = ProgressBar.VISIBLE
                            sendToServer()
                        }else{
                            Toast.makeText(this, "Invalid Username", Toast.LENGTH_LONG).show()
                        }
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

    private fun openMenu(){
        val intent = Intent(this,Menu::class.java)
        startActivity(intent)
        finish()
    }

    private fun sendToServer() {
        Log.i("register" , "send data to server")
        val server = Register(this)
        val url = "$URL_STR/api/users/create"
        val json = makeJson()
        server.execute(url, json.toString())

    }

    private fun makeJson() : JSONObject{

        val json = JSONObject()
        json.put("isUser",true)
        json.put("name",username.text.toString())
        json.put("email",emailView.text.toString())
        json.put("password",passwordView.text.toString())
        return json
    }

    @SuppressLint("StaticFieldLeak")
    inner class Register(val context : Context) : AsyncTask<String,String,String>() {

        override fun doInBackground(vararg p0: String?): String {
            try {
                Log.i("myUrl", p0[0])
                val url = URL(p0[0])
                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.connectTimeout = 5000

                urlConnection.requestMethod = "POST"
                urlConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8")
                urlConnection.setRequestProperty("Accept", "application/json")
                urlConnection.doInput = true
                urlConnection.doOutput = true

                val os = DataOutputStream(urlConnection.outputStream)
                os.writeBytes(p0[1])
                os.flush()
                os.close()

                val responseCode = urlConnection.responseCode
                if (responseCode == 200) {
                    val str = streamToString(urlConnection.inputStream)
                    publishProgress(str)
                    //every thing is good
                } else {
                    progressBarView.visibility = ProgressBar.INVISIBLE
                    Log.i("STATUS", urlConnection.responseCode.toString())
                    Log.i("MSG", urlConnection.responseMessage)
                    val str = streamToString(urlConnection.errorStream)
                    Log.i("MSG", str)
                    runOnUiThread {
                        Toast.makeText(context,str,Toast.LENGTH_LONG).show()
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

            return " "

        }

        override fun onProgressUpdate(vararg values: String?) {
            progressBarView.visibility = ProgressBar.INVISIBLE
            Log.i("json send to me",values[0])
            val json = JSONObject(values[0])
            val secretId = json.getString("_id")
            val name = json.getString("name")
            val email = json.getString("email")
            val isGold = json.getBoolean("isGold")
            //val coins = json.getInt("Coins")
            saveData(name,secretId,email,isGold)

        }
    }

    fun saveData(name :String,secret_ID : String,email : String,isGold : Boolean) {
        val data = getSharedPreferences(PLAYER_INFO, Context.MODE_PRIVATE)
        val editor = data.edit()
        editor.putBoolean(EXIST,true)
        editor.putBoolean(IS_USER,true)
        editor.putBoolean(IS_GOLD,isGold)
        editor.putString(PLAYER_NAME,name)
        editor.putString(PLAYER_SECRET_ID,secret_ID)
        editor.putString(PLAYER_EMAIL,email)
        editor.putString(PLAYER_PASSWORD,passwordView.text.toString())
        val sw = editor.commit()
        if(sw){
            Toast.makeText(this,"data saved",Toast.LENGTH_LONG).show()
        }else{
            Log.i("register","DATA NOT SAVED")
        }
        openMenu()
    }


}