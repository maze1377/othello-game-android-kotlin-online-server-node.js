package alireza.com.othello.data

import android.util.Log

class MakeAi(hashMapF: HashMap<String,Boolean>) {
/*
    private val a1 = AI("one",Color.White)
    private val a2 = AI("two",Color.Black)
    var hashMapFirst = HashMap<String,Boolean>()
    private var game : Game
    private var lastBoard : Board
    private var lastBoard2 : Board

    init {
        hashMapFirst = hashMapF
        game = Game(7,a1,a2)
        lastBoard = game.board
        lastBoard2 = game.board
    }

    fun play(){

        while (true) {
            lastBoard = game.board
            var move = a1.autoMove(game.board,hashMapFirst)
            game.play(move.xPosition, move.yPosition, a1.color, game.board)
            var sw = Game.hasEnd(game.board)
            if (sw) {
                val num = Game.findWinner(game.board)
                when (num) {
                    1 -> {
                        learnFromLoseFirst(game.board)
                    }
                    2 -> {
                        learnFromLoseLast(game.board)
                    }
                    0 -> {
                        //draw
                    }
                    -1 ->{

                    }
                }
                return
            }
            lastBoard2 = game.board
            move = a2.autoMove(game.board,hashMapLast,lastBoard2)
            game.play(move.xPosition, move.yPosition, a2.color, game.board)
            sw = Game.hasEnd(game.board)
            if (sw) {
                val num = Game.findWinner(game.board)
                when (num) {
                    1 -> {
                        learnFromLoseLast(game.board)
                    }
                    2 -> {
                        learnFromLoseFirst(game.board)
                    }
                    0 -> {
                        //draw
                    }
                    -1 ->{

                    }
                }
                return
            }
        }
    }

    fun learnFromLoseFirst(board: Board){
        val str = board.convertMapToString()
        hashMapFirst.put(str,true)
        Log.i("learn",str)
    }

    fun learnFromLoseLast(board: Board){
        val str = board.convertMapToString()
        hashMapLast.put(str,true)
        Log.i("learnLast",str)
    }

*/
}