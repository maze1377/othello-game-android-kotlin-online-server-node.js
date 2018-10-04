package alireza.com.othello

import alireza.com.othello.constant.*
import alireza.com.othello.data.OnlinePlayer
import alireza.com.othello.server.CheckState
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_online_get_info.*
import kotlinx.android.synthetic.main.other_player_in_online.view.*
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

class OnlineGetInfo : AppCompatActivity() {

    private lateinit var context : Context
    private lateinit var progressBar : ProgressBar
    private lateinit var reqLayout : LinearLayout

    private var onlinePlayerList = ArrayList<OnlinePlayer>()
    private var stateThread : UpdateStateThread? = null
    private var checkGameReqThread : CheckGameReqThread? = null
    private var checkAnswerThread : CheckAnswerThread? = null
    private var updateOnlinePlayerThread : UpdateOnlinePlayerListThread? = null

    private var workSwitch = false
    private var workSwitchReq = false

    private var secretID : String = " "
    private var isUser : Boolean = false
    var name = ""

    private var onlineState = true
    private var friendToPlay = ""

    //game req code
    //i want to play with other
    private var tempReqCode = " "
    //other want to play with me
    private var otherReqCode = ""

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
    @Suppress("DEPRECATION")
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
        setContentView(R.layout.activity_online_get_info)

        readData()
        makeUI()

    }

    override fun onStop() {
        workSwitch = true
        workSwitchReq = true
        onlineState = false
        sendState("offline")
        super.onStop()
    }
    override fun onResume() {
        onlineState = true
        workSwitch = false
        workSwitchReq = false
        sendState("online")
        checkHaveGameReq()
        getOnlinePlayer()
        super.onResume()
    }

    private fun readData(){
        val data = getSharedPreferences(PLAYER_INFO, Context.MODE_PRIVATE)
        val sw = data.getBoolean(EXIST,false)
        if(sw){
            name = data.getString(PLAYER_NAME,"Unknown")
            secretID = data.getString(PLAYER_SECRET_ID,"                ")
            isUser = data.getBoolean(IS_USER,false)
        }
    }
    private fun makeUI(){

        context = this
        this.progressBar = progressBarId
        this.reqLayout = playRequestLayout
        id.text = name

        addFriend.setOnClickListener {
            val intent = Intent(this,FriendManage::class.java)
            startActivity(intent)
        }
        btn.setOnClickListener {
            val networkState = CheckState().isConnected(this)
            if (networkState) {
                progressBar.visibility = ProgressBar.VISIBLE
                reqLayout.visibility = LinearLayout.INVISIBLE
                autoMatch()
            }
        }
        accept.setOnClickListener {
            answerToOtherReq(true)
        }
        reject.setOnClickListener {
            answerToOtherReq(false)
        }

    }

    //get online list worked
    private fun getOnlinePlayer(){
        workSwitch = false
        onlineState = true
        updateOnlinePlayerThread = UpdateOnlinePlayerListThread()
        updateOnlinePlayerThread!!.start()
        updateOnlinePlayerThread!!.getOnlinePlayerList()
    }
    private fun showOnlinePlayer(){
        val adapter = OnlinePlayerListAdapter(this,onlinePlayerList)
        playerListView.adapter = adapter

    }
    inner class OnlinePlayerListAdapter(private val context : Context,private val playerList : ArrayList<OnlinePlayer>) : BaseAdapter() {

        @SuppressLint("ViewHolder", "InflateParams")
        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
            val player = playerList[p0]
            val inflator = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflator.inflate(R.layout.other_player_in_online,null)
            view.playerName.text = player.name
            view.netState.text = player.state
            view.score.text = "0"
            view.playOnline.setOnClickListener {
                sendReq(player.secretID)
                friendToPlay = player.name
            }
            //TODO show player score

            return view
        }

        override fun getItem(p0: Int): Any {
            return playerList[p0]
        }

        override fun getItemId(p0: Int): Long {
            return p0.toLong()
        }

        override fun getCount(): Int {
            return playerList.size
        }
    }
    inner class UpdateOnlinePlayerListThread : Thread(){

        override fun run() {

            out@ while(true){
                val nowTime = System.currentTimeMillis()
                while (true) {
                    if(workSwitch){
                        break@out
                    }
                    val now = System.currentTimeMillis()
                    if (now > nowTime + 7000) {
                        if (!onlineState){
                            return
                        }
                        getOnlinePlayerList()
                        break
                    }
                }
            }

        }

        fun getOnlinePlayerList(){
            val server = PlayOnline()
            val url = "$URL_STR/api/online/all"
            val json = JSONObject()
            json.put("_id",secretID)
            server.execute("1",url,json.toString())
        }

    }

    //other want to play with me worked
    //game req fun
    private fun checkHaveGameReq(){
        workSwitchReq = false
        checkGameReqThread = CheckGameReqThread()
        checkGameReqThread!!.start()
        checkGameReqThread!!.checkNewReq()
    }
    inner class CheckGameReqThread : Thread(){

        override fun run() {

            out@ while(true){
                val nowTime = System.currentTimeMillis()
                while (true) {
                    val now = System.currentTimeMillis()
                    if(workSwitchReq){
                        break@out
                    }
                    if (now > nowTime + 5000) {
                        checkNewReq()
                        break
                    }
                }
            }

        }

        fun checkNewReq(){
            val server = PlayOnline()
            val url = "$URL_STR/api/online/state"
            val json = JSONObject()
            json.put("_id",secretID)
            server.execute("4",url,json.toString())
        }

    }
    //if yes get the req
    private fun getGameReq(){
        workSwitchReq = true
        val server = PlayOnline()
        val url = "$URL_STR/api/online/codereq"
        val json = JSONObject()
        json.put("_id",secretID)
        server.execute("6",url,json.toString())
    }
    private fun showTheReqUI(username : String){
        reqLayout.visibility = View.VISIBLE
        val str = "$username want to play"
        friendUsername.text = str
    }
    //answer to their req
    private fun answerToOtherReq(ans : Boolean){
        reqLayout.visibility = LinearLayout.INVISIBLE
        if (!ans){
            friendToPlay = ""
        }else{
            progressBar.visibility = ProgressBar.VISIBLE
        }
        val server = PlayOnline()
        val url = "$URL_STR/api/online/javab"
        val json = JSONObject()
        json.put("_bord",otherReqCode)
        json.put("state",ans)
        server.execute("7",url,json.toString())
        if(ans) {
            Log.i("answer to other req","getGameReq")
            getGameID(false)
        }else {
            sendNo(true)
            workSwitchReq = false
            checkGameReqThread = CheckGameReqThread()
            checkGameReqThread!!.start()
        }
    }

    //i want to play with other
    private fun autoMatch(){
        //TODO autoMatch
    }

    //send req to online player
    private fun sendReq(id : String){
        progressBar.visibility = ProgressBar.VISIBLE
        if (tempReqCode ==  " ") {
            workSwitchReq = false
            val server = PlayOnline()
            val url = "$URL_STR/api/online/addrequiest"
            val json = makeJsonMatch(id)
            server.execute("2", url, json.toString())
        }else{
            Toast.makeText(this,"you have one match req",Toast.LENGTH_LONG).show()
        }
    }
    private fun makeJsonMatch(email : String) : JSONObject{
        val json = JSONObject()
        json.put("_id",secretID)
        json.put("target",email)
        return json
    }
    //check for other answer
    inner class CheckAnswerThread : Thread(){

        override fun run() {

            out@ while(true){
                val nowTime = System.currentTimeMillis()
                while (true) {
                    val now = System.currentTimeMillis()
                    if(workSwitch){
                        break@out
                    }
                    if (now > nowTime + 5000) {
                        checkReqAnswer()
                        break
                    }
                }
            }

        }

        private fun checkReqAnswer(){
            val server = PlayOnline()
            val url = "$URL_STR/api/online/requieststate"
            val json = JSONObject()
            json.put("_bord",tempReqCode)
            server.execute("5",url,json.toString())
        }

    }
    //if it was yes
    private fun getGameID(temp : Boolean){
        val server = PlayOnline()
        val url = "$URL_STR/api/online/javab/yes"
        val json = JSONObject()
        if(temp) {
            json.put("_bord", tempReqCode)
        }else{
            json.put("_bord", otherReqCode)
        }
        server.execute("3",url,json.toString())
    }
    //if it was no
    private fun sendNo(tempBool : Boolean){
        Log.i("send No","temp : $tempReqCode")
        val server = PlayOnline()
        val url = "$URL_STR/api/online/javab/no"
        val json = JSONObject()
        if(tempBool){
            json.put("_bord", otherReqCode)
        }else {
            json.put("_bord", tempReqCode)
        }
        server.execute("9",url,json.toString())
        tempReqCode = " "
        friendToPlay = ""
    }

    //open new page and start game worked
    private fun nextPage(gameID : String){
        val intent = Intent(this,OnlineGame::class.java)
        intent.putExtra(BOARD_ID,gameID)
        intent.putExtra(FRIEND_NAME,friendToPlay)
        finish()
        startActivity(intent)
    }

    //state fun and class
    private fun sendState(state : String){

        when(state){
            "online" ->{
                stateThread = UpdateStateThread(this)
                stateThread!!.start()
                stateThread!!.sendState()
            }
            "offline" ->{
                onlineState = false
                val server = PlayOnline()
                var url = "$URL_STR/api/users/state"
                var json = makeStateJson(state)
                server.execute("0",url,json.toString())
                val server2 = PlayOnline()
                url = "$URL_STR/api/online/removeplayer"
                json = JSONObject()
                json.put("name",name)
                json.put("_id",secretID)
                server2.execute("0",url,json.toString())
            }
            "playing" ->{

            }
            else -> {

            }
        }

    }
    private fun makeStateJson(state: String = "online") : JSONObject {
        val json = JSONObject()
        json.put("_id",secretID)
        json.put("state",state)
        return json
    }
    inner class UpdateStateThread(val activity : Activity) : Thread(){

        override fun run() {

            out@ while(true){
                val nowTime = System.currentTimeMillis()
                while (true) {
                    if(workSwitch){
                        break@out
                    }
                    val now = System.currentTimeMillis()
                    if (now > nowTime + 10000) {
                        if (!onlineState){
                            return
                        }
                        sendState()
                        break
                    }
                }
            }

        }

        fun sendState(){
            val server = PlayOnline()
            var url = "$URL_STR/api/users/state"
            var json = makeStateJson()
            server.execute("0",url,json.toString())
            val server2 = PlayOnline()
            url = "$URL_STR/api/online/addplayer"
            json = JSONObject()
            val acc = activity as OnlineGetInfo
            json.put("name",acc.name)
            json.put("_id",secretID)
            server2.execute("0",url,json.toString())
        }

    }

    //server class
    @SuppressLint("StaticFieldLeak")
    inner class PlayOnline : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg p0: String?): String {
            try {
                if(p0[0] == "0"){
                    Log.i("getInfo","json : "+p0[2])
                }
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
                    Log.i("getting","done")
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
                //send state
                "0" -> {
                    Log.i("onlineGetInfo","done")
                }
                //get online Player
                "1" ->{
                    val json = JSONObject(values[1])
                    Log.i("server get online",json.toString())
                    val players = json.getJSONArray("player")
                    onlinePlayerList = ArrayList()
                    for(i in 0 until players.length()){
                        val pl = players[i] as JSONObject
                        val name = pl.getString("name")
                        val id = pl.getString("id")
                        val player = OnlinePlayer(name,"@.com","Online",0,id)
                        onlinePlayerList.add(player)
                    }
                    showOnlinePlayer()
                }
                //send match req
                "2" -> {
                    progressBar.visibility = ProgressBar.INVISIBLE
                    workSwitchReq = true
                    Toast.makeText(context,"wait for the answer",Toast.LENGTH_SHORT).show()
                    val json = JSONObject(values[1])
                    tempReqCode = json.getString("code")
                    checkAnswerThread = CheckAnswerThread()
                    checkAnswerThread!!.start()
                }
                //get game id when you want to play
                "3" ->{
                    tempReqCode = " "
                    val json = JSONObject(values[1])
                    val gameID = json.getString("code")
                    nextPage(gameID)
                }
                //other want to play with me
                "4" ->{
                    val json = JSONObject(values[1])
                    val ans = json.getBoolean("answer")
                    if (ans){
                        getGameReq()
                    }
                }
                // check for other answer to my req
                "5" ->{
                    val json = JSONObject(values[1])
                    val ans = json.getString("answer")
                    when(ans){
                        "exit" -> {
                            runOnUiThread{
                                Toast.makeText(context,"request time out",Toast.LENGTH_LONG).show()
                            }
                            workSwitch = true
                            sendNo(false)
                            checkHaveGameReq()
                        }
                        "wait" ->{
                            Log.i("my game state", "wait")
                        }
                        "yes" ->{
                            Log.i("my game state", "yes")
                            workSwitch = true
                            getGameID(true)
                        }
                        "no" ->{
                            Log.i("my game state", "no")
                            workSwitch = true
                            Toast.makeText(context,"player didn't accept",Toast.LENGTH_SHORT).show()
                            sendNo(false)
                            checkHaveGameReq()
                            getOnlinePlayer()
                        }
                    }
                }
                //get game req that other want to play with me
                "6" ->{
                    val json = JSONObject(values[1])
                    otherReqCode = json.getString("code")
                    friendToPlay = json.getString("username")
                    showTheReqUI(friendToPlay)
                }
                //get game code and start game if i send yes
                "7" ->{

                }
                //akbar mige no ro befrest majbori
                "9" ->{
                    reqLayout.visibility = View.INVISIBLE
                }
            }


        }
    }

}
