package alireza.com.othello.data

import android.util.Log

class AI(name : String,color :Color) : Player(name,color) {

    private lateinit var lastMove :CounterPosition
    private var temp = 0L
    fun autoMove(board : Board) : CounterPosition {
        max(-1,2,board)
        Log.i("ai all move",temp.toString())
        //findBestMove(board,hashMap)
        return lastMove
    }

    private fun max(nowDepth : Int,maxDepth : Int,board: Board) : Int{
        val moveScore = ArrayList<Int>()
        val myDepth = nowDepth + 1

        //first time should return the position
        if (myDepth == 0){
            val map = Game.isValidMove(color,board)
            val myPossibleMove = ArrayList<ArrayList<Int>>()
            for (i in 0 until map.size) {
                for (j in 0 until  map.size) {
                    if (map[i][j] != 0){
                        val move = ArrayList<Int>()
                        val newBoard = makeBoard(board)
                        Game.play(i,j,color,newBoard)
                        move.add(i)
                        move.add(j)
                        moveScore.add(min(myDepth,maxDepth,newBoard))
                        move.add(moveScore.last())
                        myPossibleMove.add(move)
                    }
                }
            }
            val max = maxInt(moveScore)
            myPossibleMove.forEach {
                if(max == it[2]){
                    lastMove = CounterPosition(it[0],it[1])
                }
            }
            return 152
        }

        val end = Game.hasEnd(board)
        if (end){
            val ans = Game.findWinner(board)
            if (ans == 1){
                return Int.MIN_VALUE
            }
            if (ans == 0){
                return 0
            }
            if (ans == 2){
                return Int.MAX_VALUE
            }
        }
        //next time return evaluate value
        return if (myDepth == maxDepth){
            evaluated(board.convertMapToInt())
        }else{
            val map = Game.isValidMove(color,board)
            for (i in 0 until map.size) {
                for (j in 0 until  map.size) {
                    if (map[i][j] != 0){
                        val newBoard = makeBoard(board)
                        Game.play(i,j,color,newBoard)
                        moveScore.add(min(myDepth,maxDepth,newBoard))
                    }
                }
            }
            maxInt(moveScore)
        }
    }
    private fun maxInt(moveScore : ArrayList<Int>) : Int{
        var max = Int.MIN_VALUE
        moveScore.forEach{
            if(max < it){
                max = it
            }
        }
        return max
    }
    private fun minInt(moveScore : ArrayList<Int>) : Int{
        var min = Int.MAX_VALUE
        moveScore.forEach{
            if(min > it){
                min = it
            }
        }
        return min
    }
    private fun makeBoard(board: Board): Board {
        val retBoard = Board(board.xSize,board.ySize)
        for (x in 0 until board.xSize) {
            for (y in 0 until  board.ySize) {
                retBoard.allCounters[x][y].color = board.allCounters[x][y].color
            }
        }
        return retBoard
    }
    private fun min(nowDepth : Int,maxDepth : Int,board: Board) : Int{
        val moveScore = ArrayList<Int>()
        val myDepth = nowDepth + 1

        val end = Game.hasEnd(board)
        if (end){
            val ans = Game.findWinner(board)
            if (ans == 1){
                return Int.MIN_VALUE
            }
            if (ans == 0){
                return 0
            }
            if (ans == 2){
                return Int.MAX_VALUE
            }
        }

        val enemyColor = if(color == Color.White){
            Color.Black
        }else{
            Color.White
        }
        if (myDepth == maxDepth){
            evaluated(board.convertMapToInt())
        }else{
            val map = Game.isValidMove(enemyColor,board)
            for (i in 0 until map.size) {
                for (j in 0 until  map.size) {
                    if (map[i][j] != 0){
                        val newBoard = makeBoard(board)
                        Game.play(i,j,color,newBoard)
                        moveScore.add(max(myDepth,maxDepth,newBoard))
                    }
                }
            }
            return minInt(moveScore)
        }
        return 0
    }
    private fun evaluated(map : ArrayList<ArrayList<Int>>) : Int{
        temp++
        var black = 0
        var white = 0
        for (i in 0 until map.size) {
            for (j in 0 until  map.size) {
                val scale = getEdgePoint(i,j)
                if (map[i][j] == 1){
                    white +=scale
                }else {
                    if (map[i][j] == 2){
                        black+=scale
                    }
                }
            }
        }
        return black - white
    }

    private fun getEdgePoint(i :Int,j : Int) : Int{
        var point = 1
        if( i == 0 || i == 7){
            point+=2
        }
        if( j == 0 || j == 7){
            point+=2
        }
        return point
    }

    private fun findBestMove(board: Board,hashMap: HashMap<String, Boolean>){
        val map = Game.isValidMove(color,board)
        for (i in 0 until map.size) {
            for (j in 0 until  map.size) {
                if (map[i][j] != 0){
                    val newBoard = makeBoard(board)
                    Game.play(i,j,color,newBoard)
                    if(isOk(board, hashMap)){
                        lastMove = CounterPosition(i,j)
                        return
                    }
                }
            }
        }
        Log.i("ai log","you will win")
    }

    private fun isOk(board: Board,hashMap : HashMap<String,Boolean>) : Boolean{
        val str = board.convertMapToString()
        return !hashMap.contains(str)
    }

}