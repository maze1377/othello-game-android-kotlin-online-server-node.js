package alireza.com.othello

import alireza.com.othello.constant.*
import alireza.com.othello.server.CheckState
import alireza.com.othello.uiClass.Player
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_menu.*
import kotlinx.android.synthetic.main.login_dialog.*
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.sql.Date
import java.util.*
import kotlin.collections.ArrayList


class Menu : AppCompatActivity() {

    private lateinit var btnList : ArrayList<String>
    private lateinit var button : Button
    private lateinit var viewMessage : Button
    private lateinit var messageLayout : ConstraintLayout
    private lateinit var messageText : TextView
    private lateinit var imagesList : ArrayList<Int>
    private lateinit var fadeIn : Animation
    private lateinit var fadeOut : Animation
    private var lang = "en"
    private var theme : String = " "

    private var secretID = " "
    private var name = " "
    private var isUser = false
    private var signIn = false

    private var getMassageSW = false

    private var getMassageThread : GetMassageThread? = null

    private fun getSetting(){
        var data = getSharedPreferences(SETTING, Context.MODE_PRIVATE)
        val sw = data.getBoolean(EXIST,false)
        if(sw) {
            lang = data.getString(LANGUAGE,"en")
            theme = data.getString(THEME,"default")
        }
        data = getSharedPreferences(PLAYER_INFO, Context.MODE_PRIVATE)
        signIn = data.getBoolean(EXIST,false)
        if(signIn){
            name = data.getString(PLAYER_NAME,"Unknown")
            secretID = data.getString(PLAYER_SECRET_ID,"                ")
            isUser = data.getBoolean(IS_USER,false)
        }else{
            name = "not Signed in"
        }
    }
    @Suppress("DEPRECATION")
    private fun loadLang(){
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSetting()
        loadTheme(this)
        loadLang()
        setContentView(R.layout.activity_menu)

        btnList = ArrayList()
        button = btnMenu
        imagesList = ArrayList()

        makeUI()

    }

    override fun onResume() {
        super.onResume()
        val data = getSharedPreferences(SETTING, Context.MODE_PRIVATE)
        val sw = data.getBoolean(EXIST,false)
        if(sw) {
            val lang = data.getString(LANGUAGE,"en")
            val theme = data.getString(THEME,"default")
            if (lang != this.lang || theme != this.theme){
                finish()
                startActivity(intent)
            }
        }
        getMassageSW = false
        if(signIn){
            getMassage()
        }
    }

    override fun onStop() {
        getMassageSW = true
        super.onStop()
    }

    private fun makeUI(){


        if(theme == resources.getString(R.string.wood)){
            btnMenu.setTextColor(Color.WHITE)
        }

        Player.play(this,2)

        back.setOnTouchListener(OnSwipeTouchListener(this))

        btnList.add(resources.getString(R.string.single_player))
        btnList.add(resources.getString(R.string.multi_player))
        btnList.add(resources.getString(R.string.online))
        btnList.add(resources.getString(R.string.setting))
        btnList.add(resources.getString(R.string.exit))

        fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        imagesList.add(R.mipmap.pl_vs_ai)
        imagesList.add(R.mipmap.pl_vs_pl)
        imagesList.add(R.mipmap.online)
        imagesList.add(R.mipmap.settings)
        imagesList.add(R.mipmap.exit)

        messageLayout = messageBox
        messageText = messageTextView
        viewMessage = messageBtn

        messageLayout.visibility = View.VISIBLE

        viewMessage.setOnClickListener {
            val intent = Intent(this,Massage::class.java)
            startActivity(intent)
        }


    }

    private fun loadTheme(activity: Activity) {
        when (theme) {
            resources.getString(R.string.def) -> {
                activity.setTheme(R.style.AppTheme)
            }
            resources.getString(R.string.dark) -> {
                activity.setTheme(R.style.dark)
            }
            resources.getString(R.string.wood) ->{
                activity.setTheme(R.style.wooden)
            }
            resources.getString(R.string.blue) ->{
                activity.setTheme(R.style.blue)
            }
            else -> {
                activity.setTheme(R.style.AppTheme)
            }
        }
    }

    fun openMenu(view: View){
        if(button.text.toString() == btnList[4]){
            finish()
            return
        }
        val intent = when(button.text.toString()){
            btnList[0] ->{
                Intent(this,SinglePlayer::class.java)
            }
            btnList[1] ->{
                Intent(this,MultiPlayerGame::class.java)
            }
            btnList[2] ->{
                val sw = CheckState().isConnected(this)
                if(sw){
                    if(signIn) {
                        Intent(this, OnlineGetInfo::class.java)
                    }else{
                        showDialogBox()
                        null
                    }
                }else{
                    Toast.makeText(this,"Check Internet Connection", Toast.LENGTH_LONG).show()
                    null
                }
            }
            btnList[3] ->{
                Intent(this,Setting::class.java)
            }
            else -> {
                throw Exception("mistake")
            }
        }
        if(intent != null) {
            startActivity(intent)
        }
    }

    private fun showDialogBox(){
        val text = " you need to sign in"
        val cdd = LoginDialog(this,text)
        cdd.show()
    }

    private inner class LoginDialog(private val activity: Activity,private val str : String) : Dialog(activity),View.OnClickListener {

        lateinit var btn : Button

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.login_dialog)
            text.text = str
            btn = accept
            btn.setOnClickListener(this)
            btn = reject
            btn.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            when (p0){
                accept ->{
                    val intent = Intent(activity,LoginOrNot::class.java)
                    startActivity(intent)
                }
                reject ->{
                    Toast.makeText(activity,"you can't play online without login",Toast.LENGTH_LONG).show()
                }
            }
            dismiss()
        }

    }

    fun changeMenu(view : View){
        val btn = button
        when(view){
            leftArrow ->{
                imageMenu.startAnimation(fadeOut)
                val str = btn.text.toString()
                val index = btnList.indexOf(str)
                if(index == 0){
                    btn.text = btnList.last()
                    imageMenu.setImageResource(imagesList.last())
                }else{
                    btn.text = btnList[(index-1)]
                    imageMenu.setImageResource(imagesList[(index-1)])
                }
                imageMenu.startAnimation(fadeIn)
            }
            rightArrow ->{
                imageMenu.startAnimation(fadeIn)
                val str = btn.text.toString()
                val index = btnList.indexOf(str)
                if(index == btnList.size-1){
                    btn.text = btnList[0]
                    imageMenu.setImageResource(imagesList[0])
                }else{
                    btn.text = btnList[(index+1)]
                    imageMenu.setImageResource(imagesList[(index+1)])
                }
                imageMenu.startAnimation(fadeIn)
            }
        }

    }

    inner class OnSwipeTouchListener(ctx: Context) : View.OnTouchListener {

        private val gestureDetector: GestureDetector

        init {
            gestureDetector = GestureDetector(ctx, GestureListener())
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            return gestureDetector.onTouchEvent(event)
        }

        private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {

            private val SWIPE_THRESHOLD = 100
            private val SWIPE_VELOCITY_THRESHOLD = 100

            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                var result = false
                try {
                    val diffY = e2.y - e1.y
                    val diffX = e2.x - e1.x
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                onSwipeRight()
                            } else {
                                onSwipeLeft()
                            }
                            result = true
                        }
                    } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeBottom()
                        } else {
                            onSwipeTop()
                        }
                        result = true
                    }
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }

                return result
            }

        }

        fun onSwipeRight() {
            changeMenu(leftArrow)
        }

        fun onSwipeLeft() {
            changeMenu(rightArrow)
        }

        fun onSwipeTop() {}

        fun onSwipeBottom() {}
    }

    //get massage
    private fun getMassage(){
        val sw = CheckState().isConnected(this)
        if(sw){
            messageText.text = ""
            getMassageThread = GetMassageThread()
            getMassageThread!!.start()
            getMassageThread!!.getLastMassage()
        }else{
            showMessage()
        }
    }
    inner class GetMassageThread : Thread(){

        override fun run() {

            out@ while(true){
                val nowTime = System.currentTimeMillis()
                while (true) {
                    if (getMassageSW){
                        return
                    }
                    val now = System.currentTimeMillis()
                    if (now > nowTime + 60000) {
                        getLastMassage()
                        break
                    }
                }
            }

        }

        fun getLastMassage(){
            val server = PlayOnline()
            val url = "$URL_STR/api/users/massage"
            val json = JSONObject()
            json.put("_id",secretID)
            server.execute("0",url,json.toString())
        }

    }
    private fun showMessage(){
        val i = getMassageFromStorage()
        var index = 0
        i.forEach {
            if (!it.isRead){
                index++
            }
        }
        if (index > 0) {
            val str = "You have $index new massage"
            messageText.text = str
            messageLayout.visibility = View.VISIBLE
        }else{
            val str = "you have no new massage"
            messageText.text = str
            messageLayout.visibility = View.VISIBLE
        }
    }

    //server class
    @SuppressLint("StaticFieldLeak")
    inner class PlayOnline : AsyncTask<String, String, String>() {

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
                    //TODO handel error
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
                //get massage state
                "0" -> {
                    val json = JSONObject(values[1])
                    var state = json.getInt("state")
                    if (state != 0){
                        val pms = ArrayList<alireza.com.othello.uiClass.Massage>()
                        val massages = json.getJSONArray("massege")
                        while (state > 0){
                            state--
                            val massage : JSONObject = massages[state] as JSONObject
                            val sender = massage.getString("sender")
                            //val date = Date(massage.getLong("date"))
                            //TODO akbar
                            val text = massage.getString("text")
                            val mode = massage.getString("mode")
                            val pm = alireza.com.othello.uiClass.Massage(sender,text,Date(0),false,mode)
                            pms.add(pm)
                        }
                        Log.i("save massage",pms.toString())
                        saveMassage(pms)
                        showMessage()
                    }else{
                        showMessage()
                    }
                }
            }


        }
    }

    private fun saveMassage(massage : ArrayList<alireza.com.othello.uiClass.Massage>){
        val data = getSharedPreferences(MASSAGE, Context.MODE_PRIVATE)
        val sw = data.getBoolean(EXIST,false)
        val editor = data.edit()
        if(sw) {
            val i = data.getInt(MASSAGE_COUNT,0)
            editor.putInt(MASSAGE_COUNT,(massage.size+i))
        }else{
            editor.putBoolean(EXIST,true)
            editor.putInt(MASSAGE_COUNT,massage.size)
        }
        editor.apply()
        val pms = getMassageFromStorage()
        var i = 0
        massage.addAll(pms)
        massage.forEach {
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
    private fun getMassageFromStorage() : ArrayList<alireza.com.othello.uiClass.Massage>{
        val data = getSharedPreferences(MASSAGE, Context.MODE_PRIVATE)
        val sw = data.getBoolean(EXIST, false)
        val pms = ArrayList<alireza.com.othello.uiClass.Massage>()
        if (sw) {
            val i = data.getInt(MASSAGE_COUNT, 0)
            for (index in 0 until i) {
                Log.i("getting massage","in menu")
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
}
