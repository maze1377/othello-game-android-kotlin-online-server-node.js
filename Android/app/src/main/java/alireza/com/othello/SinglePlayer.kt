package alireza.com.othello

import alireza.com.othello.constant.*
import alireza.com.othello.data.*
import alireza.com.othello.uiClass.WinnerDialog
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.activity_single_player.*
import java.io.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@Suppress("DEPRECATION")
class SinglePlayer : AppCompatActivity() {

    private lateinit var mainLayout : LinearLayout
    private val counterList = ArrayList<ArrayList<ImageView>>()
    private var turn = true
    private var validMove = java.util.ArrayList<java.util.ArrayList<Int>>()
    private lateinit var game : Game

    private var lang = "en"
    private var theme : String = " "

    private var boardBackground = 0
    private var emptyCounter = 0
    private var validCounter = 0
    private var blackCounter = 0
    private var whiteCounter = 0

    private lateinit var ai : AI
    private lateinit var playerOne : Player

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
    private fun getSetting(){
        val data = getSharedPreferences(SETTING, Context.MODE_PRIVATE)
        val sw = data.getBoolean(EXIST,false)
        if(sw) {
            lang = data.getString(LANGUAGE,"en")
            theme = data.getString(THEME,"default")
        }

    }
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
        loadTheme()
        loadLang()
        setContentView(R.layout.activity_single_player)

        mainLayout = images_layout

        playerOne = Player("PlayerOne", Color.White)
        ai = AI("DeepGreen", Color.Black)

        game = Game(7,playerOne,ai)

        makeUI()

        validMove = Game.isValidMove(playerOne.color,game.board)
        showPossibleMove()

        //saveMap()
    }

    @SuppressLint("ResourceType")
    private fun showPossibleMove(){
        for(i in 0..7) {
            for(j in 0..7){
                if(validMove[i][j] > 0){
                    counterList[i][j].setImageResource(validCounter)
                }
            }
        }
    }
    @SuppressLint("ResourceType")
    private fun hidePossibleMove(){
        for(i in 0..7) {
            for(j in 0..7){
                if(validMove[i][j] > 0){
                    counterList[i][j].setImageResource(this.emptyCounter)
                }
            }
        }
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
    private fun dpToPx(dp: Int, context: Context): Int {
        val density = context.resources.displayMetrics.density
        return Math.round(dp.toFloat() * density)
    }
    private fun makeUI(){

        val tempSize = dpToPx(41,this)

        for(i in 0..8){
            val row = LinearLayout(this)
            val imageRow = java.util.ArrayList<ImageView>()
            for(j in 0..8){
                val image = ImageView(this)
                image.setOnClickListener {
                    btnClicked(image,i,j)
                }
                val params = ViewGroup.LayoutParams(tempSize,tempSize)
                image.layoutParams = params
                imageRow.add(j,image)
                row.addView(image,j)
            }
            counterList.add(i,imageRow)
            mainLayout.addView(row,i)
        }

        counterList[3][4].setImageResource(blackCounter)
        counterList[4][3].setImageResource(blackCounter)
        counterList[3][3].setImageResource(whiteCounter)
        counterList[4][4].setImageResource(whiteCounter)


    }
    @SuppressLint("ResourceType")
    private fun btnClicked(view : ImageView, i : Int, j :Int){
        if(validMove[i][j] > 0){

            hidePossibleMove()

            if(turn) {
                val reverse = game.play(i,j,Color.White,game.board)
                runOnUiThread {
                    updateUI(reverse)
                }
            }

            val r = Runnable {
                do {
                    if(checkEnd()){
                        break
                    }
                    validMove = Game.isValidMove(ai.color,game.board)
                    val sw = canMove()
                    if (sw) {
                        getAndShowAiMove()
                        validMove = Game.isValidMove(playerOne.color, game.board)
                        val can = canMove()
                        if(can){
                            showPossibleMove()
                            break
                        }
                    }else {
                        validMove = Game.isValidMove(playerOne.color, game.board)
                        val can = canMove()
                        if(can){
                            showPossibleMove()
                            break
                        }else{
                            if(checkEnd()){
                                break
                            }
                        }
                    }
                }while (true)
            }
            val handler = Handler()
            handler.postDelayed(r,1000)


        }

    }
    private fun checkEnd() : Boolean{
        when(game.findWinner()){
            -1 -> {
                return false
            }
            0 -> {
                showDialogBox("draw")
                return true
            }
            1 -> {
                //saveMap()
                showDialogBox("Player One")
                return true
            }
            2 ->{
                showDialogBox("AI")
                return true
            }
            else ->{
                return false
            }
        }
    }
    private fun showDialogBox(winner : String){
        runOnUiThread {
            val cdd = WinnerDialog(this,winner)
            //cdd.window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
            cdd.show()
            alireza.com.othello.uiClass.Player.play(this,1)
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

    private fun updateUI(reverse : java.util.ArrayList<java.util.ArrayList<Int>>?){
        val board = game.boardInInt()
        var delay : Long = 100
        for(i in 0..7) {
            for(j in 0..7){
                var sw = false
                if (reverse != null){
                    if(reverse[i][j] > 0){
                        sw = true
                    }
                }
                when(board[i][j]){
                    0 -> {
                        this.counterList[i][j].setImageResource(this.emptyCounter)
                    }
                    1 -> {
                        if (sw){
                            val anim = getDrawable(R.drawable.reverse_animation_white_to_black) as AnimationDrawable
                            this.counterList[i][j].setImageDrawable(anim)
                            val runnable = Runnable {
                                anim.start()
                            }
                            val handler = Handler()
                            handler.postDelayed(runnable,delay)
                            delay+=100
                        }else {
                            this.counterList[i][j].setImageResource(this.blackCounter)
                        }
                    }
                    2 -> {
                        if (sw){
                            val anim = getDrawable(R.drawable.reverse_animation_black_to_white) as AnimationDrawable
                            this.counterList[i][j].setImageDrawable(anim)
                            val runnable = Runnable {
                                anim.start()
                            }
                            val handler = Handler()
                            handler.postDelayed(runnable,delay)
                            delay+=100
                        }else {
                            this.counterList[i][j].setImageResource(this.whiteCounter)
                        }
                    }
                    else->{
                        throw Exception("ridi")
                    }
                }
            }
        }

    }
    private var hashMap = HashMap<String,Boolean>()
    private fun getAndShowAiMove(){
        //hashMap = readMap() as HashMap<String,Boolean>
        //Log.i("logining map",hashMap.toString())
        val move = ai.autoMove(game.board)
        val reverse = game.play(move.xPosition,move.yPosition,Color.Black,game.board)
        updateUI(reverse)
        //4ta comment kardam
    }

    private fun saveMapf(hashMap : HashMap<*,*>){
        Log.i("saving hash map",hashMap.toString())
        val temp = getSharedPreferences(HASH_MAP_FIRST, Context.MODE_PRIVATE)
        val ed = temp.edit().putBoolean("dataExist",true)
        ed.apply()
        ed.commit()
        val file = File(getDir("data1", Context.MODE_PRIVATE), "map")
        val outputStream = ObjectOutputStream(FileOutputStream(file))
        outputStream.writeObject(hashMap)
        outputStream.flush()
        outputStream.close()
    }

    private fun saveMap(){
        Log.i("hashMap saved",hashMap.toString())
        val temp = getSharedPreferences(HASH_MAP, Context.MODE_PRIVATE)
        val ed = temp.edit().putBoolean("dataExist",true)
        ed.apply()
        ed.commit()
        val file = File(getDir("dataHash", Context.MODE_PRIVATE), "map")
        val outputStream = ObjectOutputStream(FileOutputStream(file))
        outputStream.writeObject(hashMap)
        outputStream.flush()
        outputStream.close()
    }

    private fun readMapFirst() : HashMap<*,*>?{
        val share = getSharedPreferences(HASH_MAP_FIRST, Context.MODE_PRIVATE)
        val sw = share.getBoolean("dataExist",false)
        if (sw) {
            val file = File(getDir("data1", Context.MODE_PRIVATE), "map")
            val inputStream = ObjectInputStream(FileInputStream(file))
            val temp = inputStream.readObject() as HashMap<*, *>
            return temp as HashMap<String, Boolean>
        }
        Log.i("read hash","make new")
        return HashMap<String,Boolean>()
    }

    private fun readMap() : HashMap<*,*>?{
        val share = getSharedPreferences(HASH_MAP, Context.MODE_PRIVATE)
        val sw = share.getBoolean("dataExist",false)
        if (sw) {
            val file = File(getDir("dataHash", Context.MODE_PRIVATE), "map")
            val inputStream = ObjectInputStream(FileInputStream(file))
            val temp = inputStream.readObject() as HashMap<*, *>
            return temp as HashMap<String, Boolean>
        }
        Log.i("read hash","make new")
        return HashMap<String,Boolean>()
    }



}
