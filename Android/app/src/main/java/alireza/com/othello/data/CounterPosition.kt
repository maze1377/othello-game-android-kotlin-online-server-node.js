package alireza.com.othello.data

import java.lang.Exception

class CounterPosition(xPosition: Int,yPosition: Int) {

    var xPosition : Int = 0
    var yPosition : Int = 0
    init {
        if(xPosition >= 0){
            this.xPosition = xPosition
        }else{
            throw Exception("negative X")
        }
        if(yPosition >= 0){
            this.yPosition = yPosition
        }else{
            throw Exception("negative Y")
        }
    }

}