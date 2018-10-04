package alireza.com.othello

import alireza.com.othello.constant.*
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.AsyncTask
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_show_massage.*
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.sql.Date
import java.util.*

class ShowMassage : AppCompatActivity() {

    private var secretID : String = ""
    private var isUser : Boolean = false
    private var email : String = ""
    private var name : String = ""
    private var lang = "en"
    private var theme : String = " "
    var pms : ArrayList<alireza.com.othello.uiClass.Massage>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSetting()
        loadLang()
        setContentView(R.layout.activity_show_massage)
        val extra = intent.extras
        val num = extra.getInt(MASSAGE_NUM,0)
        readData()
        pms = getMassageFromStorage()
        makeUI(num)

    }

    private fun getSetting(){
        val data = getSharedPreferences(SETTING, Context.MODE_PRIVATE)
        val sw = data.getBoolean(EXIST,false)
        if(sw) {
            lang = data.getString(LANGUAGE,"en")
            theme = data.getString(THEME,"default")
        }
    }
    @Suppress("DEPRECATION")
    private fun loadLang() {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }
    private fun readData(){
        val data = getSharedPreferences(PLAYER_INFO, Context.MODE_PRIVATE)
        val sw = data.getBoolean(EXIST,false)
        if(sw){
            name = data.getString(PLAYER_NAME,"Unknown")
            secretID = data.getString(PLAYER_SECRET_ID,"                ")
            isUser = data.getBoolean(IS_USER,false)
            email = data.getString(PLAYER_EMAIL,"")
        }else{

        }
    }

    private fun makeUI(num : Int){
        val pm = pms!![num]
        senderName.text = pm.sender
        massageText.text = pm.text
        massageTime.text = pm.date.toString()
        senderEmail.text = pm.sender
        if(pm.mode == "friendreq"){
            friendReqAnswerLayout.visibility = ConstraintLayout.VISIBLE
        }

        rejectFriendReq.setOnClickListener {
            pms!![num].mode = "none"
            saveMassage(pms!!)
            friendReqAnswerLayout.visibility = ConstraintLayout.INVISIBLE
            finish()
        }

        acceptFriendReq.setOnClickListener {
            answerToOtherReq(pm.sender)
            friendReqAnswerLayout.visibility = ConstraintLayout.INVISIBLE
            pms!![num].mode = "none"
            finish()
        }

    }


    //answer to friend req
    private fun answerToOtherReq(email : String){
        val server = FriendServer(this)
        val url = "$URL_STR/api/users/friends/answer"
        val json = JSONObject()
        json.put("email",this.email)
        json.put("target",email)
        server.execute("1",url,json.toString())
    }

    //server functions
    @SuppressLint("StaticFieldLeak")
    inner class FriendServer(val context : Context) : AsyncTask<String, String, String>() {

        /*
            1 -> send answer to friend req

         */

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
                    publishProgress(p0[0], str)
                    //every thing is good
                } else {
                    Log.i("STATUS", urlConnection.responseCode.toString())
                    Log.i("MSG", urlConnection.responseMessage)
                    Log.i("MSG", streamToString(urlConnection.errorStream))
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

            return " "

        }

        override fun onProgressUpdate(vararg values: String?) {
            when(values[0]){
                "1" ->{
                    saveMassage(pms!!)
                    Toast.makeText(context,"done",Toast.LENGTH_SHORT).show()
                }

            }


        }
    }

    private fun getMassageFromStorage() : ArrayList<alireza.com.othello.uiClass.Massage>{
        val data = getSharedPreferences(MASSAGE, Context.MODE_PRIVATE)
        val sw = data.getBoolean(EXIST, false)
        val pms = ArrayList<alireza.com.othello.uiClass.Massage>()
        if (sw) {
            val i = data.getInt(MASSAGE_COUNT, 0)
            for (index in 0..i) {
                val dataGetter = getSharedPreferences("mass$index", Context.MODE_PRIVATE)
                val sender = dataGetter.getString(MASSAGE_SENDER,"unknown")
                val date = Date(dataGetter.getLong(MASSAGE_DATE,0))
                val text = dataGetter.getString(MASSAGE_TEXT,"empty")
                val mode = dataGetter.getString(MASSAGE_MODE,"none")
                val read = dataGetter.getBoolean(MASSAGE_IS_READ,false)
                val pm = alireza.com.othello.uiClass.Massage(sender,text,date,read,mode)
                pms.add(pm)
            }
        }
        return pms
    }
    private fun saveMassage(massage : ArrayList<alireza.com.othello.uiClass.Massage>){
        val data = getSharedPreferences(MASSAGE, Context.MODE_PRIVATE)
        val sw = data.getBoolean(EXIST,false)
        if(sw) {
        }else{
            val editor = data.edit()
            editor.putBoolean(EXIST,true)
            editor.putInt(MASSAGE_COUNT,massage.size)
            editor.apply()
        }
        var i = 0
        massage.forEach {
            Log.i("save massage",it.mode)
            val oneMassage = getSharedPreferences("mass$i", Context.MODE_PRIVATE)
            val edit = oneMassage.edit()
            edit.putLong(MASSAGE_DATE,it.date.time)
            edit.putString(MASSAGE_TEXT,it.text)
            edit.putString(MASSAGE_SENDER,it.sender)
            edit.putString(MASSAGE_MODE,it.mode)
            edit.putBoolean(MASSAGE_IS_READ,it.isRead)
            edit.apply()
            edit.commit()
            i++
        }
    }
}
