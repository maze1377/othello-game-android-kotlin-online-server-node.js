package alireza.com.othello

import alireza.com.othello.constant.*
import alireza.com.othello.data.Color
import alireza.com.othello.data.Game
import alireza.com.othello.data.Player
import alireza.com.othello.uiClass.WinnerDialog
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.AnimationDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_online_game.*
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

class OnlineGame : AppCompatActivity() {

    private val btnList = java.util.ArrayList<java.util.ArrayList<ImageView>>()
    private lateinit var mainLayout : LinearLayout


    private var lang = "en"
    private var theme : String = " "
    private var secretID : String = " "
    private var isUser : Boolean = false
    private var name = ""
    private var target = ""

    private var boardID = ""
    private var myColor = Color.None

    private var getMoveThread : GetMoveThread? = null
    private var pr : ProgressBarThread? = null
    private var prWork = true
    private var gettingMove = true

    private var validMove = ArrayList<ArrayList<Int>>()

    private lateinit var game : Game
    private var turn = false

    private fun getSetting(){
        var data = getSharedPreferences(SETTING, Context.MODE_PRIVATE)
        var sw = data.getBoolean(EXIST,false)
        if(sw) {
            lang = data.getString(LANGUAGE,"en")
            theme = data.getString(THEME,"default")
        }
        data = getSharedPreferences(PLAYER_INFO, Context.MODE_PRIVATE)
        sw = data.getBoolean(EXIST,false)
        if(sw){
            name = data.getString(PLAYER_NAME,"Unknown")
            secretID = data.getString(PLAYER_SECRET_ID,"                ")
            isUser = data.getBoolean(IS_USER,false)
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

    private var boardBackground = 0
    private var emptyCounter = 0
    private var validCounter = 0
    private var blackCounter = 0
    private var whiteCounter = 0
    @SuppressLint("Recycle", "ResourceType")
    private fun loadTheme(){
        val attrs = intArrayOf(R.attr.empty_counter, R.attr.valid_counter, R.attr.black_counter,R.attr.white_counter,R.attr.board_background)
        val tm = when (theme) {
            resources.getString(R.string.def) -> {
                R.style.AppTheme
            }
            resources.getString(R.string.dark) -> {
                R.style.dark
            }
            resources.getString(R.string.wood) ->{
                R.style.wooden
            }
            resources.getString(R.string.blue) ->{
                R.style.blue
            }
            else -> {
                R.style.AppTheme
            }
        }
        this.setTheme(tm)
        val ta = obtainStyledAttributes(tm, attrs)
        boardBackground = ta.getResourceId(4,R.mipmap.empty_board)
        emptyCounter = ta.getResourceId(0,R.mipmap.empty_counter)
        validCounter = ta.getResourceId(1,R.mipmap.valid_move_counter)
        blackCounter = ta.getResourceId(2,R.mipmap.black_counter_only_desined)
        whiteCounter = ta.getResourceId(3,R.mipmap.white_counter_only_desined)

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSetting()
        loadLang()
        loadTheme()
        setContentView(R.layout.activity_online_game)
        val extra = intent.extras
        target = extra.get(FRIEND_NAME) as String
        boardID = extra.get(BOARD_ID) as String

        getColorFromServer()

    }

    //send state
    private fun sendState(state : String){

        when(state){
            "online" ->{

            }
            "offline" ->{
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
                val server = PlayOnline()
                val url = "$URL_STR/api/users/state"
                val json = makeStateJson(state)
                server.execute("0",url,json.toString())
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

    override fun onResume() {
        if(!turn){
            getLastMove()
        }
        sendState("playing")
        super.onResume()
    }

    override fun onStop() {
        sendState("offline")
        gettingMove = false
        super.onStop()
    }

    private fun dpToPx(dp: Int, context: Context): Int {
        val density = context.resources.displayMetrics.density
        return Math.round(dp.toFloat() * density)
    }

    private fun makeUI(){

        mainLayout = mainBoardTableLayout

        plOne.text = name
        plTwo.text = target

        val tempSize = dpToPx(41,this)

        for(i in 0 until 8){
            val row = LinearLayout(this)
            val imageRow = java.util.ArrayList<ImageView>()
            for(j in 0 until 8){
                val image = ImageView(this)
                image.setOnClickListener {
                    btnClicked(image,i,j)
                }
                val params = ViewGroup.LayoutParams(tempSize,tempSize)
                image.layoutParams = params
                imageRow.add(j,image)
                row.addView(image,j)
            }
            this.btnList.add(i,imageRow)
            mainLayout.addView(row,i)
        }

        this.btnList[3][3].setImageResource(whiteCounter)
        this.btnList[4][4].setImageResource(whiteCounter)
        this.btnList[3][4].setImageResource(blackCounter)
        this.btnList[4][3].setImageResource(blackCounter)


    }

    private fun btnClicked(view : ImageView, i : Int, j :Int){
        if (turn) {
            if (validMove[i][j] > 0) {
                val reverse: java.util.ArrayList<java.util.ArrayList<Int>>?
                hidePossibleMove()

                if (myColor == Color.White) {
                    Log.i("online game","white played")
                    view.setImageResource(this.whiteCounter)
                    reverse = game.play(i, j, Color.White, game.board)
                    validMove = Game.isValidMove(Color.Black, game.board)
                } else {
                    Log.i("online game","black played")
                    view.setImageResource(this.blackCounter)
                    reverse = game.play(i, j, Color.Black, game.board)
                    validMove = Game.isValidMove(Color.White, game.board)
                }
                updateUI(reverse)
                sendMove(i, j)
                checkEnd()

                val sw = canMove()
                if (sw) {
                    Log.i("online game","they can move")
                    changeTurn()
                    getLastMove()
                } else {
                    Log.i("online game","they can't move")
                    validMove = Game.isValidMove(myColor, game.board)
                    showPossibleMove()
                }

            }
        }else{
            Toast.makeText(this,"not your turn",Toast.LENGTH_LONG).show()
        }

    }

    private fun canMove() : Boolean{
        for(i in 0..7) {
            for(j in 0..7){
                if(validMove[i][j] > 0){
                    return true
                }
            }
        }
        return false
    }

    @SuppressLint("ResourceType")
    private fun updateUI( reverse : java.util.ArrayList<java.util.ArrayList<Int>>? ) {
        val board = game.boardInInt()
        var delay: Long = 100
        for (i in 0..7) {
            for (j in 0..7) {
                var sw = false
                if (reverse != null) {
                    if (reverse[i][j] > 0) {
                        sw = true
                    }
                }
                when (board[i][j]) {
                    0 -> {
                        this.btnList[i][j].setImageResource(this.emptyCounter)
                    }
                    1 -> {
                        if (sw) {
                            val anim = getDrawable(R.drawable.reverse_animation_white_to_black) as AnimationDrawable
                            this.btnList[i][j].setImageDrawable(anim)
                            val runnable = Runnable {
                                anim.start()
                            }
                            val handler = Handler()
                            handler.postDelayed(runnable, delay)
                            delay += 100
                        } else {
                            this.btnList[i][j].setImageResource(this.blackCounter)
                        }
                    }
                    2 -> {
                        if (sw) {
                            val anim = getDrawable(R.drawable.reverse_animation_black_to_white) as AnimationDrawable
                            this.btnList[i][j].setImageDrawable(anim)
                            val runnable = Runnable {
                                anim.start()
                            }
                            val handler = Handler()
                            handler.postDelayed(runnable, delay)
                            delay += 100
                        } else {
                            this.btnList[i][j].setImageResource(this.whiteCounter)
                        }
                    }
                    else -> {
                        throw Exception("ridi")
                    }
                }
            }
        }
    }
    private fun showPossibleMove(){
        for(i in 0..7) {
            for(j in 0..7){
                if(validMove[i][j] > 0){
                    this.btnList[i][j].setImageResource(validCounter)
                }
            }
        }
    }
    @SuppressLint("ResourceType")
    private fun hidePossibleMove(){
        for(i in 0..7) {
            for(j in 0..7){
                if(validMove[i][j] > 0){
                    this.btnList[i][j].setImageResource(this.emptyCounter)
                }
            }
        }
    }

    private fun checkEnd(){
        when(game.findWinner()){
            -1 -> {
                return
            }
            0 -> {
                showDialogBox("draw")
            }
            1 -> {
                showDialogBox("Player One")
            }
            2 ->{
                showDialogBox("Player Two")
            }
        }
    }


    private fun resetTimer(){
        if(pr != null){
            prWork = false
        }
        progressBar.progress = 1
        progressBar.max = 30
        progressBar.incrementProgressBy(1)
        pr = ProgressBarThread(progressBar)
        pr!!.start()
    }

    private fun showOtherMove(x : String,y : String){
        val i = x.toInt()
        val j = y.toInt()
        val reverse = game.play(i,j,game.playerTwo.color,game.board)
        if(myColor == Color.White){
            btnList[i][j].setImageResource(this.blackCounter)
        }else{
            btnList[i][j].setImageResource(this.whiteCounter)
        }
        updateUI(reverse)
        validMove = if(myColor == Color.White){
            Game.isValidMove(Color.White,game.board)
        }else{
            Game.isValidMove(Color.Black,game.board)
        }
        showPossibleMove()
    }

    inner class ProgressBarThread(private val pr : ProgressBar) : Thread(){
        override fun run() {

            out@ while(true){
                val nowTime = System.currentTimeMillis()
                while (true) {
                    if (!prWork){
                        break@out
                    }
                    val now = System.currentTimeMillis()
                    if (now > nowTime + 1000) {
                        pr.progress++
                        remainTime.text = "${(30 - pr.progress)}"
                        if (pr.progress == pr.max){
                            runOnUiThread {
                                Log.i("pr Thread","finished")
                                finishGame()
                            }
                            break@out
                        }
                        break
                    }
                }
            }
            Log.i("pr Thread","outed")

        }

        private fun finishGame(){
            val count1 = game.getCount(game.playerOne.color)
            val count2 = game.getCount(game.playerTwo.color)
            if (count1 > count2){
                showDialogBox("Player One")
            }else{
                if (count2 > count1){
                    showDialogBox("Player Two")
                }    else{
                    showDialogBox("draw")
                }
            }
        }
    }
    private fun showDialogBox(winner : String){
        val cdd = WinnerDialog(this,winner)
        //cdd.window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        cdd.show()
        alireza.com.othello.uiClass.Player.play(this,1)
    }


    //join game and get color
    private fun getColorFromServer(){
        val server = PlayOnline()
        val url = "$URL_STR/api/border/join"
        val json = JSONObject()
        json.put("_bord",boardID)
        json.put("_id",secretID)
        server.execute("1",url,json.toString())

    }

    //send my move to server
    private fun sendMove(i : Int,j : Int){

        val server = PlayOnline()
        val url = "$URL_STR/api/border/move/add"
        val json = JSONObject()
        json.put("_bord",boardID)
        json.put("x",i)
        json.put("y",j)
        json.put("_id",secretID)
        server.execute("2",url,json.toString())

    }
    inner class GetMoveThread : Thread(){

        override fun run() {
            out@ while(true){
                val nowTime = System.currentTimeMillis()
                while (true) {
                    val now = System.currentTimeMillis()
                    if (now > nowTime + 2000) {
                        if(!gettingMove){
                            break@out
                        }
                        getLastMove()
                        break
                    }
                }
            }
        }

        private fun getLastMove(){
            val server = PlayOnline()
            val url = "$URL_STR/api/border/move/last"
            val json = JSONObject()
            json.put("_bord",boardID)
            server.execute("3",url,json.toString())
        }

    }
    private fun getLastMove(){
        gettingMove = true
        getMoveThread = GetMoveThread()
        getMoveThread!!.start()
    }


    private fun showTurn(){
        val plOneTurn = FirstPlayerCheck
        val plTwoTurn = SecondPlayerCheck
        if(turn){
            plOneTurn.isChecked = true
            plTwoTurn.isChecked = false
        }else{
            plTwoTurn.isChecked = true
            plOneTurn.isChecked = false
        }
        //resetTimer()
    }

    private fun changeTurn(){
        turn = !turn
        val plOneTurn = FirstPlayerCheck
        val plTwoTurn = SecondPlayerCheck
        if(turn){
            plOneTurn.isChecked = true
            plTwoTurn.isChecked = false
        }else{
            plTwoTurn.isChecked = true
            plOneTurn.isChecked = false
        }
        resetTimer()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.playing_menu_without_friends,menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if(item != null){
            when(item.itemId){
                R.id.Setting -> {
                    val intent = Intent(this,Setting::class.java)
                    startActivity(intent)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

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
                //send tate
                "0" ->{

                }
                //get color
                "1" ->{
                    val json = JSONObject(values[1])
                    val color = json.getInt("color")
                    myColor = if(color == 1){
                        Color.White
                    }else{
                        Color.Black
                    }
                    Log.i("mycolor",myColor.toString())
                    val playerOne = Player(name,myColor)
                    val playerTwo = if(myColor == Color.Black) {
                        Player(target, Color.White)
                    }else{
                        Player(target, Color.Black)
                    }
                    game = Game(7,playerOne,playerTwo)

                    makeUI()

                    if (playerOne.color == Color.White){
                        Log.i("onlineGame","pl one is white")
                        turn = true
                        validMove = Game.isValidMove(Color.White,game.board)
                        showPossibleMove()
                    }else {
                        if(playerOne.color == Color.Black){
                            turn = false
                            getLastMove()
                        }
                    }
                    showTurn()
                }
                //send move
                "2" ->{
                    //nothing
                }
                //get last move
                "3" ->{
                    val json = JSONObject(values[1])
                    if(json.has("userId")) {
                        val id = json.getString("userId")
                        if (id == secretID) {
                            //nothing
                        } else {
                            Log.i("online game", "i get last move for op")
                            gettingMove = false
                            val x = json.getString("x")
                            val y = json.getString("y")
                            showOtherMove(x, y)
                            resetTimer()
                            changeTurn()
                        }
                    }else{
                        Log.i("getMove","notPlayed")
                    }
                }
            }


        }
    }


}
