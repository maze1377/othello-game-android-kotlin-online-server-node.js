package alireza.com.othello

import alireza.com.othello.constant.EXIST
import alireza.com.othello.constant.LANGUAGE
import alireza.com.othello.constant.SETTING
import alireza.com.othello.constant.THEME
import alireza.com.othello.data.Color
import alireza.com.othello.data.Game
import alireza.com.othello.data.Player
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
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_multi_player_game.*
import java.util.*


class MultiPlayerGame : AppCompatActivity() {

    private val btnList = ArrayList<ArrayList<ImageView>>()
    private lateinit var mainLayout : LinearLayout

    private var turn = true
    private var validMove = ArrayList<ArrayList<Int>>()
    private lateinit var game : Game
    private var lang = "en"
    private var theme : String = " "

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

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSetting()
        loadLang()
        loadTheme()
        setContentView(R.layout.activity_multi_player_game)

        Log.i("theme in multi", theme)

        val playerOne = Player("PlayerOne",Color.White)
        val playerTwo = Player("PlayerTwo",Color.Black)

        game = Game(7,playerOne,playerTwo)

        mainLayout = mainBoardTableLayout

        makeUI()
        updateUI( null )

        validMove = Game.isValidMove(playerOne.color,game.board)
        showPossibleMove()

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


    @SuppressLint("ResourceType")
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
            this.btnList.add(i,imageRow)
            mainLayout.addView(row,i)
        }

        this.btnList[3][3].setImageResource(blackCounter)
        this.btnList[4][4].setImageResource(blackCounter)
        this.btnList[3][4].setImageResource(whiteCounter)
        this.btnList[4][3].setImageResource(whiteCounter)

    }

    @SuppressLint("ResourceType")
    private fun btnClicked(view : ImageView, i : Int, j :Int){
        if(validMove[i][j] > 0){
            Log.i("hide","clicked")
            hidePossibleMove()
            val reverse : ArrayList<ArrayList<Int>>?
            validMove = if(turn) {
                view.setImageResource(this.whiteCounter)
                reverse = game.play(i,j,Color.White,game.board)
                Game.isValidMove(Color.Black,game.board)
            }else{
                view.setImageResource(this.blackCounter)
                reverse = game.play(i,j,Color.Black,game.board)
                Game.isValidMove(Color.White,game.board)
            }
            updateUI(reverse)
            val sw = canMove()
            if(sw) {
                showPossibleMove()
                changeTurn()
            }else{
                checkEnd()
                if(turn){
                    Toast.makeText(this,"no move for Black",Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(this,"no move for White",Toast.LENGTH_LONG).show()
                }
                validMove = if(turn) {
                    Game.isValidMove(Color.White,game.board)
                }else{
                    Game.isValidMove(Color.Black,game.board)
                }
                showPossibleMove()
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

    private fun showDialogBox(winner : String){
        val cdd = WinnerDialog(this,winner)
        //cdd.window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        cdd.show()
        alireza.com.othello.uiClass.Player.play(this,1)
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
    }

    @SuppressLint("ResourceType")
    private fun updateUI( reverse : ArrayList<ArrayList<Int>>? ){
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
                        this.btnList[i][j].setImageResource(this.emptyCounter)
                    }
                    1 -> {
                        if (sw){
                            val anim = getDrawable(R.drawable.reverse_animation_white_to_black) as AnimationDrawable
                            this.btnList[i][j].setImageDrawable(anim)
                            val runnable = Runnable {
                                anim.start()
                            }
                            val handler = Handler()
                            handler.postDelayed(runnable,delay)
                            delay+=100
                        }else {
                            this.btnList[i][j].setImageResource(this.blackCounter)
                        }
                    }
                    2 -> {
                        if (sw){
                            val anim = getDrawable(R.drawable.reverse_animation_black_to_white) as AnimationDrawable
                            this.btnList[i][j].setImageDrawable(anim)
                            val runnable = Runnable {
                                anim.start()
                            }
                            val handler = Handler()
                            handler.postDelayed(runnable,delay)
                            delay+=100
                        }else {
                            this.btnList[i][j].setImageResource(this.whiteCounter)
                        }
                    }
                    else->{
                        throw Exception("ridi")
                    }
                }
            }
        }

        val plOneScore = OnePoint
        val plTwoScore = TwoPoint

        var count = game.getCount(game.playerOne.color)
        plOneScore.text = count.toString()

        count = game.getCount(game.playerTwo.color)
        plTwoScore.text = count.toString()
    }

}
