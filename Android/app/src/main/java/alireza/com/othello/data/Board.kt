package alireza.com.othello.data

import android.util.Log
import java.lang.Exception

class Board(xSize: Int, ySize: Int) {

    val xSize: Int
    val ySize: Int
    val allCounters: ArrayList<ArrayList<Counter>>

    fun convertMapToInt() :ArrayList<ArrayList<Int>>{
        val boardInt = ArrayList<ArrayList<Int>>()
        for(x in 0..xSize){
            val row = ArrayList<Int>()
            for(y in 0..ySize){
                val num = when(allCounters[x][y].color){
                    Color.Black -> 1
                    Color.White -> 2
                    Color.None -> 0
                }
                row.add(num)
            }
            boardInt.add(row)
        }
        return boardInt
    }

    fun logMap() : String{
        var string = ""
        for(x in 0..xSize){
            for(y in 0..ySize){

                val num = when(allCounters[x][y].color){
                    Color.Black -> 1
                    Color.White -> 2
                    Color.None -> 0
                }
                string = "$string $num "
            }

            string = "$string \n"
        }
        return string
    }

    init {
        if(xSize >= 0){
            this.xSize = xSize
        }else{
            throw Exception("negative X")
        }
        if(ySize >= 0){
            this.ySize = ySize
        }else{
            throw Exception("negative Y")
        }
        this.allCounters = ArrayList()
        for (x in 0..xSize) {
            val rowCounter = ArrayList<Counter>()
            for (y in 0..ySize) {
                val position = CounterPosition(x, y)
                val counter = Counter(position, Color.None)
                rowCounter.add(counter)
            }
            allCounters.add(rowCounter)
        }
        allCounters[3][3].color = Color.White
        allCounters[4][4].color = Color.White
        allCounters[3][4].color = Color.Black
        allCounters[4][3].color = Color.Black
        //logMap()
    }

    fun convertMapToString() : String{
        var temp = ""
        for(i in 0..xSize){
            for(j in 0..ySize){
                when(allCounters[i][j].color){
                    Color.Black ->{
                        temp += "${Color.Black.ordinal}"
                    }
                    Color.White -> {
                        temp += "${Color.White.ordinal}"
                    }
                    Color.None -> {
                        temp += "${Color.None.ordinal}"
                    }
                }
            }
        }
        return temp
    }


}