package alireza.com.othello

import alireza.com.othello.constant.*
import alireza.com.othello.data.OnlinePlayer
import android.annotation.SuppressLint
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
import kotlinx.android.synthetic.main.activity_friend_manage.*
import kotlinx.android.synthetic.main.friend_in_manage.view.*
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList


class FriendManage : AppCompatActivity() {

    private var secretID : String = ""
    private var isUser : Boolean = false
    private var email : String = ""
    private var name : String = ""
    private var lang = "en"
    private var theme : String = " "

    private lateinit var pr : ProgressBar
    private lateinit var playReq : LinearLayout
    private var friendList = ArrayList<OnlinePlayer>()

    private var checkGameReqThread : CheckGameReqThread? = null
    private var checkAnswerThread : CheckAnswerThread? = null
    private var friendsThread : UpdateFriendListThread? = null
    private var stateThread : UpdateStateThread? = null

    private var onlineState = true
    private var friendToPlay = ""
    private var workSwitch = false
    private var workSwitchReq = false

    private var tempReqCode = ""
    private var otherReqCode = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSetting()
        loadLang()
        setContentView(R.layout.activity_friend_manage)

        readData()
        makeUi()

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
        updateOnlineFriend()
        super.onResume()
    }


    private fun makeUi(){

        pr = progressBarView
        playReq = playRequestLay

        //send friend req
        sendReq.setOnClickListener{
            val email = emailEdit.text.toString()
            if(email.contains("@")) {
                if(email != this.email) {
                    pr.visibility = View.VISIBLE
                    sendReq(email)
                }else{
                    Toast.makeText(this,"enter other person email",Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this,"enter valid email",Toast.LENGTH_SHORT).show()
            }
        }

        acceptReq.setOnClickListener {
            answerToOtherGameReq(true)
        }

        rejectReq.setOnClickListener {
            answerToOtherGameReq(false)
        }

    }

    //send friend request
    private fun sendReq(email : String){
        hideKeyboard(this)
        val server = FriendServer(this)
        val url = "$URL_STR/api/users/friends/req"
        val json = JSONObject()
        json.put("email",this.email)
        json.put("target",email)
        server.execute("1",url,json.toString())

    }

    //data functions
    private fun readData(){
        val data = getSharedPreferences(PLAYER_INFO, Context.MODE_PRIVATE)
        val sw = data.getBoolean(EXIST,false)
        if(sw){
            name = data.getString(PLAYER_NAME,"Unknown")
            secretID = data.getString(PLAYER_SECRET_ID,"                ")
            isUser = data.getBoolean(IS_USER,false)
            email = data.getString(PLAYER_EMAIL,"")
        }
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

    //send state fun
    private fun sendState(state : String){

        when(state){
            "online" ->{
                stateThread = UpdateStateThread(this)
                stateThread!!.start()
                stateThread!!.sendState()
            }
            "offline" ->{
                onlineState = false
                val server = FriendServer(this)
                var url = "$URL_STR/api/users/state"
                var json = makeStateJson(state)
                server.execute("3",url,json.toString())
                val server2 = FriendServer(this)
                url = "$URL_STR/api/online/removeplayer"
                json = JSONObject()
                json.put("name",name)
                json.put("_id",secretID)
                server2.execute("3",url,json.toString())
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
    inner class UpdateStateThread(val context : Context) : Thread(){

        override fun run() {

            out@ while(true){
                val nowTime = System.currentTimeMillis()
                while (true) {
                    if(workSwitch){
                        break@out
                    }
                    val now = System.currentTimeMillis()
                    if (now > nowTime + 30000) {
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
            val server = FriendServer(context)
            var url = "$URL_STR/api/users/state"
            var json = makeStateJson()
            server.execute("3",url,json.toString())
            val server2 = FriendServer(context)
            url = "$URL_STR/api/online/addplayer"
            json = JSONObject()
            val acc = context as FriendManage
            json.put("name", acc.name)
            json.put("_id",secretID)
            server2.execute("3",url,json.toString())
        }

    }

    //delete friend
    private fun deleteFriends(email : String){
        pr.visibility = ProgressBar.VISIBLE
        val server = FriendServer(this)
        val url = "$URL_STR/api/users/friends/delete"
        val json = makeDeleteJson(email)
        server.execute("4",url,json.toString())

    }
    private fun makeDeleteJson(target : String) : JSONObject {
        val json = JSONObject()
        json.put("email", email)
        json.put("target", target)
        return json
    }

    private fun nextPage(gameID : String){
        val intent = Intent(this,OnlineGame::class.java)
        intent.putExtra(BOARD_ID,gameID)
        intent.putExtra(FRIEND_NAME,friendToPlay)
        startActivity(intent)
        finish()
    }


    //start match fun
    private fun friendMatch(friendID :String){
        pr.visibility = ProgressBar.VISIBLE
        if(tempReqCode == " "){
            val server = FriendServer(this)
            val url = "$URL_STR/api/online/addrequiest"
            val json = makeJsonMatch(friendID)
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
    inner class CheckAnswerThread(val context : Context) : Thread(){

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
            val server = FriendServer(context)
            val url = "$URL_STR/api/online/requieststate"
            val json = JSONObject()
            json.put("_bord",tempReqCode)
            server.execute("5",url,json.toString())
        }

    }
    //if it was yes
    private fun getGameID(tempBool: Boolean){
        val server = FriendServer(this)
        val url = "$URL_STR/api/online/javab/yes"
        val json = JSONObject()
        if(tempBool) {
            json.put("_bord", tempReqCode)
        }else{
            json.put("_bord", otherReqCode)
        }
        server.execute("6",url,json.toString())
    }
    //if it was no
    private fun sendNo(tempBool : Boolean){
        //temp bool if true i didn't want to play
        //if false send no because other didn't want
        val server = FriendServer(this)
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

    //friends fun and class
    private fun updateOnlineFriend() {
        onlineState = true
        workSwitch = false
        friendsThread = UpdateFriendListThread(this)
        friendsThread!!.start()
        friendsThread!!.getFriendsList()
    }
    inner class UpdateFriendListThread(val context : Context) : Thread(){

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
                        getFriendsList()
                        break
                    }
                }
            }

        }

        fun getFriendsList(){
            val server = FriendServer(context)
            val url = "$URL_STR/api/users/friends/get"
            val json = makeJson()
            server.execute("0",url,json.toString())
        }

    }
    private fun makeJson() : JSONObject {
        val json = JSONObject()
        json.put("_id", secretID)
        return json
    }
    private fun updateList(){
        val adapter = Adapter(this,friendList)
        myFriendListView.adapter = adapter
    }
    inner class Adapter(val context : Context,private val playerList : ArrayList<OnlinePlayer>) : BaseAdapter() {

        @SuppressLint("ViewHolder", "InflateParams")
        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
            val player = playerList[p0]
            val inflator = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflator.inflate(R.layout.friend_in_manage,null)
            view.friendUsername.text = player.name
            view.friendEmail.text = player.email
            view.netWorkState.text = player.state
            view.playFriend.setOnClickListener {
                if(player.state == "online"){
                    friendToPlay = player.name
                    friendMatch(player.secretID)
                }
            }
            view.deleteFriend.setOnClickListener {
                Log.i("send req","other player ${player.email}")
                deleteFriends(player.email)
            }
            return view
        }

        override fun getItem(p0: Int): Any {
            return playerList[p0]
        }

        override fun getItemId(p0: Int): Long {
            return 0
        }

        override fun getCount(): Int {
            return playerList.size
        }


    }


    //other want to play with me
    //game req fun
    private fun checkHaveGameReq(){
        workSwitchReq = false
        checkGameReqThread = CheckGameReqThread(this)
        checkGameReqThread!!.start()
        checkGameReqThread!!.checkNewReq()
    }
    inner class CheckGameReqThread(val context: Context) : Thread(){

        override fun run() {

            out@ while(true){
                val nowTime = System.currentTimeMillis()
                while (true) {
                    val now = System.currentTimeMillis()
                    if(workSwitchReq){
                        break@out
                    }
                    if (now > nowTime + 3000) {
                        checkNewReq()
                        break
                    }
                }
            }

        }

        fun checkNewReq(){
            val server = FriendServer(context)
            val url = "$URL_STR/api/online/state"
            val json = JSONObject()
            json.put("_id",secretID)
            server.execute("7",url,json.toString())
        }

    }
    //if yes get the req
    private fun getGameReq(){
        workSwitchReq = true
        val server = FriendServer(this)
        val url = "$URL_STR/api/online/codereq"
        val json = JSONObject()
        json.put("_id",secretID)
        server.execute("8",url,json.toString())
    }
    //show game req
    private fun showTheGameReqUI(username : String){
        playRequestLay.visibility = View.VISIBLE
        val str = "$username want to play"
        friendName.text = str
    }
    private fun answerToOtherGameReq(ans : Boolean){
        playRequestLay.visibility = View.INVISIBLE
        if (!ans){
            friendToPlay = ""
        }else{
            pr.visibility = ProgressBar.VISIBLE
        }
        val server = FriendServer(this)
        val url = "$URL_STR/api/online/javab"
        val json = JSONObject()
        json.put("_bord",otherReqCode)
        json.put("state",ans)
        server.execute("11",url,json.toString())
        if(ans) {
            getGameID(false)
        }else {
            sendNo(true)
            workSwitchReq = false
            checkHaveGameReq()
        }
    }


    //server functions
    @SuppressLint("StaticFieldLeak")
    inner class FriendServer(val context : Context) : AsyncTask<String, String, String>() {

        /*
            0 -> get list
            1 -> send friend req
            2 -> send match req
            3 -> send state
            4 -> delete friends
            5 -> check my game req answer
            6 -> answer to my req is yes so i get game code
            7 -> check if some one want to play with me
            8 -> i get game code req and show dialog(other want to play with me)
            9 ->
            10 ->if i say yes get game code and start it
            11 ->send my answer to other game req
            12 ->get all massage
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
                    if(urlConnection.responseCode.toString() == "400"){
                        Toast.makeText(context,"player have game req",Toast.LENGTH_LONG).show()
                    }
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
                //get friend list
                "0"-> {
                    val list = ArrayList<OnlinePlayer>()
                    val json = JSONObject(values[1])
                    val friends = json.getJSONArray("friends")
                    val length = friends.length()
                    for(i in 0 until length){
                        val friend = friends.getJSONObject(i)
                        val name = friend.getString("name")
                        val email = friend.getString("email")
                        val state = friend.getString("state")
                        val id = friend.getString("_id")
                        //TODO get score
                        val pl = OnlinePlayer(name,email,state,0,id)
                        list.add(pl)
                    }
                    if (friendList != list){
                        friendList = list
                        updateList()
                    }
                }
                //send friend req
                "1" -> {
                    Toast.makeText(context,"Friend request send",Toast.LENGTH_SHORT).show()
                    emailEdit.setText("")
                    pr.visibility = View.INVISIBLE
                    //do nothing
                }
                //send match req
                "2" -> {
                    pr.visibility = ProgressBar.INVISIBLE
                    workSwitchReq = true
                    Toast.makeText(context,"wait for the answer",Toast.LENGTH_SHORT).show()
                    val json = JSONObject(values[1])
                    tempReqCode = json.getString("code")
                    checkAnswerThread = CheckAnswerThread(context)
                    checkAnswerThread!!.start()
                }
                //send state
                "3" ->{

                }
                //delete friend
                "4" ->{
                    pr.visibility = ProgressBar.INVISIBLE
                    UpdateFriendListThread(context).getFriendsList()
                }
                // check for other answer to my req
                "5" ->{
                    val json = JSONObject(values[1])
                    val ans = json.getString("answer")
                    when(ans){
                        "wait" ->{
                            Log.i("my game state", "wait")
                        }
                        "yes" ->{
                            workSwitch = true
                            getGameID(true)
                        }
                        "no" ->{
                            workSwitch = true
                            Toast.makeText(context,"player didn't accept",Toast.LENGTH_SHORT).show()
                            sendNo(false)
                            checkHaveGameReq()
                        }
                    }
                }
                "6" ->{
                    tempReqCode = " "
                    val json = JSONObject(values[1])
                    val gameID = json.getString("code")
                    nextPage(gameID)
                }
                //check other want to play with me
                "7" ->{
                    val json = JSONObject(values[1])
                    val ans = json.getBoolean("answer")
                    if (ans){
                        getGameReq()
                    }
                }
                //yes ,other want to play with me
                "8" ->{
                    val json = JSONObject(values[1])
                    otherReqCode = json.getString("code")
                    friendToPlay = json.getString("username")
                    showTheGameReqUI(friendToPlay)
                }
                //akbar mige no ro befrest majbori
                "9" ->{
                    playRequestLay.visibility = View.INVISIBLE
                }
                "11" ->{

                }

            }


        }
    }




}
