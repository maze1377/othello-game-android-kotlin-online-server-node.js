package alireza.com.othello.data

class Game(boardSize : Int,val playerOne : Player,val playerTwo : Player) {
    companion object {
        fun isValidMove(color : Color,board : Board) : ArrayList<ArrayList<Int>> {
            if(color == Color.None){
                throw Exception("invalid color")
            }
            val boardInt = board.convertMapToInt()

            val possibleBoard = Board(board.xSize,board.ySize)
            possibleBoard.allCounters[3][3].color = Color.None
            possibleBoard.allCounters[3][4].color = Color.None
            possibleBoard.allCounters[4][4].color = Color.None
            possibleBoard.allCounters[4][3].color = Color.None

            val boardAns = possibleBoard.convertMapToInt()

            checkVertical(boardInt,color,boardAns)
            checkHorizontal(boardInt,color,boardAns)
            checkDiagonal(boardInt,color,boardAns)

            return boardAns
        }
        private fun checkDiagonal(boardInt: ArrayList<ArrayList<Int>>, color : Color, boardAns : ArrayList<ArrayList<Int>>) {

            var x = boardInt.size-2
            var y: Int
            val otherColor : Int = if (color == Color.Black){
                Color.White.ordinal
            }else{
                Color.Black.ordinal
            }

            //top left to bottom right
            while (x >= 0){
                y=0
                val bugX = x
                while (y < boardInt[0].size-2 && x < boardInt.size-2){
                    if(boardInt[x][y] == color.ordinal) {
                        var tempY = y+1
                        var tempX = x+1
                        var sw = false
                        while (tempX < boardInt.size-1 && tempY < boardInt[0].size-1) {
                            if (boardInt[tempX][tempY] == otherColor) {
                                sw = true
                            }else{
                                break
                            }
                            tempX++
                            tempY++
                        }
                        if(sw && boardInt[tempX][tempY] == 0){
                            boardAns[tempX][tempY]++
                        }
                        x = tempX
                        y = tempY
                    }else {
                        x++
                        y++
                    }
                }
                x = bugX
                x--
            }
            y =1
            while(y < boardInt.size-2){
                x = 0
                val bugY = y
                while (x < boardInt.size-2 && y< boardInt[0].size -2) {
                    if (boardInt[x][y] == color.ordinal) {
                        var tempY = y + 1
                        var tempX = x + 1
                        var sw = false
                        while (tempX < boardInt.size-1 && tempY < boardInt[x].size-1) {
                            if (boardInt[tempX][tempY] == otherColor) {
                                sw = true
                            } else {
                                break
                            }
                            tempX++
                            tempY++
                        }
                        if (sw && boardInt[tempX][tempY] == 0) {
                            boardAns[tempX][tempY]++
                        }
                        x = tempX
                        y = tempY
                    } else {
                        x++
                        y++
                    }
                }
                y = bugY
                y++
            }

            //bottom right to top left
            y = 2
            while(y <= boardInt[0].size-1){
                x = boardInt.size-1
                val bugY = y
                while (x > 1 && y > 1){
                    if(boardInt[x][y] == color.ordinal) {
                        var tempY = y-1
                        var tempX = x-1
                        var sw = false
                        while (tempX > 0 && tempY > 0 ){
                            if (boardInt[tempX][tempY] == otherColor) {
                                sw = true
                            }else{
                                break
                            }
                            tempX--
                            tempY--
                        }
                        if(sw && boardInt[tempX][tempY] == 0){
                            boardAns[tempX][tempY]++
                        }
                        x = tempX
                        y = tempY
                    }else{
                        x--
                        y--
                    }
                }
                y = bugY
                y++
            }
            x = boardInt.size-2
            while (x > 1){
                y=boardInt[0].size-1
                val bugX = x
                while (x > 1 && y > 1){
                    if(boardInt[x][y] == color.ordinal) {
                        var tempY = y-1
                        var tempX = x-1
                        var sw = false
                        while (tempX > 0 && tempY > 0 ){
                            if (boardInt[tempX][tempY] == otherColor) {
                                sw = true
                            }else{
                                break
                            }
                            tempX--
                            tempY--
                        }
                        if(sw && boardInt[tempX][tempY] == 0){
                            boardAns[tempX][tempY]++
                        }
                        x = tempX
                        y = tempY
                    }else{
                        x--
                        y--
                    }
                }
                x = bugX
                x--
            }


            //top right to bottom left
            y = 2
            while (y <= boardInt[0].size-1){
                x = 0
                val bugY = y
                while (y > 1 && x < boardInt.size-2){
                    if(boardInt[x][y] == color.ordinal) {
                        var tempY = y-1
                        var tempX = x+1
                        var sw = false
                        while (tempX < boardInt.size-1 && tempY > 0) {
                            if (boardInt[tempX][tempY] == otherColor) {
                                sw = true
                            }else{
                                break
                            }
                            tempX++
                            tempY--
                        }
                        if(sw && boardInt[tempX][tempY] == 0){
                            boardAns[tempX][tempY]++
                        }
                        x = tempX
                        y = tempY
                    }else {
                        x++
                        y--
                    }
                }
                y = bugY
                y++
            }
            x = 1
            while (x < boardInt.size-2){
                y = boardInt[0].size-1
                val bugX = x
                while (x <boardInt.size-2 && y > 1){
                    if(boardInt[x][y] == color.ordinal) {
                        var tempY = y-1
                        var tempX = x+1
                        var sw = false
                        while (tempX < boardInt.size-1 && tempY > 0 ){
                            if (boardInt[tempX][tempY] == otherColor) {
                                sw = true
                            }else{
                                break
                            }
                            tempX++
                            tempY--
                        }
                        if(sw && boardInt[tempX][tempY] == 0){
                            boardAns[tempX][tempY]++
                        }
                        x = tempX
                        y = tempY
                    }else{
                        x++
                        y--
                    }
                }
                x = bugX
                x++
            }

            //bottom left to top right
            x = 2
            while (x <= boardInt.size-1){
                y = 0
                val bugX = x
                while (y < boardInt[0].size - 2 && x > 1){
                    if(boardInt[x][y] == color.ordinal) {
                        var tempY = y+1
                        var tempX = x-1
                        var sw = false
                        while (tempX > 0 && tempY < boardInt[0].size-1 ){
                            if (boardInt[tempX][tempY] == otherColor) {
                                sw = true
                            }else{
                                break
                            }
                            tempX--
                            tempY++
                        }
                        if(sw && boardInt[tempX][tempY] == 0){
                            boardAns[tempX][tempY]++
                        }
                        x = tempX
                        y = tempY
                    }else{
                        x--
                        y++
                    }
                }
                x = bugX
                x++
            }
            y = 1
            while (y < boardInt[0].size -2 ){
                x = boardInt.size-1
                val bugY = y
                while (x > 1 && y < boardInt[0].size - 2){
                    if(boardInt[x][y] == color.ordinal) {
                        var tempY = y+1
                        var tempX = x-1
                        var sw = false
                        while (tempX > 0 && tempY < boardInt[0].size - 1 ){
                            if (boardInt[tempX][tempY] == otherColor) {
                                sw = true
                            }else{
                                break
                            }
                            tempX--
                            tempY++
                        }
                        if(sw && boardInt[tempX][tempY] == 0){
                            boardAns[tempX][tempY]++
                        }
                        x = tempX
                        y = tempY
                    }else{
                        x--
                        y++
                    }
                }
                y = bugY
                y++
            }

        }
        private fun checkHorizontal(boardInt: ArrayList<ArrayList<Int>>, color : Color, boardAns : ArrayList<ArrayList<Int>>) {

            var x = 0
            var y : Int
            val otherColor : Int = if (color == Color.Black){
                Color.White.ordinal
            }else{
                Color.Black.ordinal
            }

            while(x < boardInt.size){
                y = 0
                while(y < boardInt[x].size - 1){
                    if(boardInt[x][y] == color.ordinal) {
                        var temp = y+1
                        var sw = false
                        while (temp < boardInt[x].size-1) {
                            if (boardInt[x][temp] == otherColor) {
                                sw = true
                            }else{
                                break
                            }
                            temp++
                        }
                        if(sw && boardInt[x][temp] == 0){
                            boardAns[x][temp]++
                        }
                        y = temp
                    }else {
                        y++
                    }
                }
                x++
            }

            //check vertical right to left
            x = 0
            while(x < boardInt.size){
                y = boardInt[x].size-1
                while(1 < y){
                    if(boardInt[x][y] == color.ordinal) {
                        var temp = y-1
                        var sw = false
                        while (temp > 0) {
                            if (boardInt[x][temp] == otherColor){
                                sw = true
                            }else{
                                break
                            }
                            temp--
                        }
                        if(sw && boardInt[x][temp] == 0){
                            boardAns[x][temp] ++
                        }
                        y = temp
                    }else {
                        y--
                    }
                }
                x++
            }

        }
        private fun checkVertical(boardInt: ArrayList<ArrayList<Int>> , color : Color , boardAns : ArrayList<ArrayList<Int>>){

            var x = 0
            var y = 0
            val otherColor : Int = if (color == Color.Black){
                Color.White.ordinal
            }else{
                Color.Black.ordinal
            }

            while(y < boardInt[0].size){
                x = 0
                while(x < boardInt.size - 1){
                    if(boardInt[x][y] == color.ordinal) {
                        var temp = x+1
                        var sw = false
                        while (temp < boardInt.size-1) {
                            if (boardInt[temp][y] == otherColor) {
                                sw = true
                            }else{
                                break
                            }
                            temp++
                        }
                        if(sw && boardInt[temp][y] == 0){
                            boardAns[temp][y]++
                        }
                        x = temp
                    }else {
                        x++
                    }
                }
                y++
            }

            y = 0
            while(y < boardInt.size){
                x = boardInt[x].size-1
                while(1 < x){
                    if(boardInt[x][y] == color.ordinal) {
                        var temp = x-1
                        var sw = false
                        while (temp > 0) {
                            if (boardInt[temp][y] == otherColor){
                                sw = true
                            }else{
                                break
                            }
                            temp--
                        }
                        if(sw && boardInt[temp][y] == 0){
                            boardAns[temp][y] ++
                        }
                        x = temp
                    }else {
                        x--
                    }
                }
                y++
            }
        }

        fun hasEnd(board : Board) : Boolean{
            var map = isValidMove(Color.Black,board)
            map.forEach { it ->
                it.forEach{
                    if (it != 0){
                        return false
                    }
                }
            }
            map = isValidMove(Color.White,board)
            map.forEach { it ->
                it.forEach{
                    if (it != 0){
                        return false
                    }
                }
            }
            return true
        }

        fun findWinner(board: Board) : Int{
            if(hasEnd(board)) {
                var temp = 0
                for (i in 0..board.xSize) {
                    for (j in 0..board.ySize) {
                        if(board.allCounters[i][j].color == Color.White){
                            temp++
                        }
                    }
                }
                var black = 0
                for (i in 0..board.xSize) {
                    for (j in 0..board.ySize) {
                        if(board.allCounters[i][j].color == Color.Black){
                            black++
                        }
                    }
                }
                return when {
                    //1 white
                    //2 black
                    temp > black -> 1
                    temp == black -> 0
                    temp < black -> 2
                    else -> throw Exception("ridi in winner")
                }
            }else{
                return -1
            }
        }

        fun play(i:Int,j:Int,color : Color,board: Board){
            if(color == Color.White){
                board.allCounters[i][j].color = Color.White
            }else{
                board.allCounters[i][j].color = Color.Black
            }
            val revers = findCounterToReverse(i,j,color,board)
            for(x in 0 until revers.size){
                for(y in 0 until revers[0].size){
                    if(revers[x][y] > 0){
                        if(color == Color.White){
                            board.allCounters[x][y].color = Color.White
                        }else{
                            board.allCounters[x][y].color = Color.Black
                        }
                    }
                }
            }
        }

        private fun findCounterToReverse(i:Int,j:Int,color : Color,board: Board): ArrayList<ArrayList<Int>> {

            val possibleBoard = Board(board.xSize,board.ySize)
            possibleBoard.allCounters[3][3].color = Color.None
            possibleBoard.allCounters[3][4].color = Color.None
            possibleBoard.allCounters[4][4].color = Color.None
            possibleBoard.allCounters[4][3].color = Color.None
            val boardAns = possibleBoard.convertMapToInt()
            board.logMap()
            val boardInt = board.convertMapToInt()

            val otherColor : Int = if (color == Color.Black){
                Color.White.ordinal
            }else{
                Color.Black.ordinal
            }

            //left to right
            var temp = 0
            var sw = false
            for(y in (j+1)..board.ySize){
                if(boardInt[i][y] == otherColor){
                    temp++
                }else{
                    if(boardInt[i][y] == color.ordinal){
                        sw = true
                    }
                    break
                }
            }
            if(temp > 0 && sw){
                for(y in (j+1) until (j+1+temp)){
                    boardAns[i][y]++
                }
            }

            //right to left
            temp = 0
            sw = false
            var y = j-1
            while(y >= 0){
                if(boardInt[i][y] == otherColor){
                    temp++
                }else{
                    if(boardInt[i][y] == color.ordinal){
                        sw = true
                    }
                    break
                }
                y--
            }
            if(temp > 0 && sw){
                for(y in (j-temp)..(j-1)){
                    boardAns[i][y]++
                }
            }

            //top to bottom
            temp = 0
            sw = false
            for (x in (i + 1)..board.xSize) {
                if (boardInt[x][j] == otherColor) {
                    temp++
                } else {
                    if (boardInt[x][j] == color.ordinal) {
                        sw = true
                    }
                    break
                }
            }
            if (temp > 0 && sw) {
                for (x in (i + 1) until (i + 1 + temp)) {
                    boardAns[x][j]++
                }
            }



            //bottom to top
            temp = 0
            sw = false
            var x = i - 1
            while(x >= 0){
                if(boardInt[x][j] == otherColor){
                    temp++
                }else{
                    if(boardInt[x][j] == color.ordinal){
                        sw = true
                    }
                    break
                }
                x--
            }
            if(temp > 0 && sw){
                for(z in (i-temp)..(i-1)){
                    boardAns[z][j]++
                }
            }

            //top left to bottom right
            temp = 0
            sw = false
            x = i + 1
            y = j + 1
            while(x <= board.xSize && y <= board.ySize){
                if(boardInt[x][y] == otherColor){
                    temp++
                }else{
                    if(boardInt[x][y] == color.ordinal){
                        sw = true
                    }
                    break
                }
                x++
                y++
            }
            if(temp > 0 && sw){
                for(k in 1..temp){
                    boardAns[i+k][j+k] ++
                }
            }

            //bottom left to top right
            temp = 0
            sw = false
            x = i - 1
            y = j + 1
            while(x >= 0 && y <= board.ySize){
                if(boardInt[x][y] == otherColor){
                    temp++
                }else{
                    if(boardInt[x][y] == color.ordinal){
                        sw = true
                    }
                    break
                }
                x--
                y++
            }
            if(temp > 0 && sw){
                for(k in 1..temp){
                    boardAns[i-k][j+k] ++
                }
            }

            //top right to bottom left
            temp = 0
            sw = false
            x = i + 1
            y = j - 1
            while(x <= board.xSize && y >= 0){
                if(boardInt[x][y] == otherColor){
                    temp++
                }else{
                    if(boardInt[x][y] == color.ordinal){
                        sw = true
                    }
                    break
                }
                x++
                y--
            }
            if(temp > 0 && sw){
                for(k in 1..temp){
                    boardAns[i+k][j-k] ++
                }
            }

            //bottom right to top left
            temp = 0
            sw = false
            x = i - 1
            y = j - 1
            while(x >=0 && y >= 0){
                if(boardInt[x][y] == otherColor){
                    temp++
                }else{
                    if(boardInt[x][y] == color.ordinal){
                        sw = true
                    }
                    break
                }
                x--
                y--
            }
            if(temp > 0 && sw){
                for(k in 1..temp){
                    boardAns[i-k][j-k] ++
                }
            }


            return boardAns
        }

    }

    val board : Board = Board(boardSize,boardSize)

    fun boardInInt() : ArrayList<ArrayList<Int>> {
        return board.convertMapToInt()
    }

    private fun isEnd() : Boolean{
        var map = isValidMove(Color.Black,this.board)
        map.forEach { it ->
            it.forEach{
                if (it != 0){
                    return false
                }
            }
        }
        map = isValidMove(Color.White,this.board)
        map.forEach { it ->
            it.forEach{
                if (it != 0){
                    return false
                }
            }
        }
        return true
    }

    /*
        1 => pl one wined
        0 => draw
        2 => pl two wined
        -1 => not end
     */
    fun findWinner() : Int{
        if(isEnd()) {
            var temp = 0
            for (i in 0..board.xSize) {
                for (j in 0..board.ySize) {
                    if(board.allCounters[i][j].color == playerOne.color){
                        temp++
                    }
                }
            }
            val countNum = board.xSize * board.ySize
            return when {
                temp > (countNum/2) -> 1
                temp == (countNum/2) -> 0
                temp < (countNum/2) -> 2
                else -> throw Exception("ridi in winner")
            }
        }else{
            return -1
        }
    }

    fun getCount(color :Color):Int{
        var temp = 0
        for (i in 0..board.xSize) {
            for (j in 0..board.ySize) {
                if(board.allCounters[i][j].color == color){
                    temp++
                }
            }
        }
        return temp
    }

    fun play(i:Int,j:Int,color : Color,board: Board) :  ArrayList<ArrayList<Int>> {
        if(color == Color.White){
            board.allCounters[i][j].color = Color.White
        }else{
            board.allCounters[i][j].color = Color.Black
        }
        val revers = findCounterToReverse(i,j,color)
        for(x in 0 until revers.size){
            for(y in 0 until revers[0].size){
                if(revers[x][y] > 0){
                    if(color == Color.White){
                        board.allCounters[x][y].color = Color.White
                    }else{
                        board.allCounters[x][y].color = Color.Black
                    }
                }
            }
        }
        return revers
    }

    private fun findCounterToReverse(i:Int,j:Int,color : Color): ArrayList<ArrayList<Int>> {

        val possibleBoard = Board(board.xSize,board.ySize)
        possibleBoard.allCounters[3][3].color = Color.None
        possibleBoard.allCounters[3][4].color = Color.None
        possibleBoard.allCounters[4][4].color = Color.None
        possibleBoard.allCounters[4][3].color = Color.None
        val boardAns = possibleBoard.convertMapToInt()
        board.logMap()
        val boardInt = board.convertMapToInt()

        val otherColor : Int = if (color == Color.Black){
            Color.White.ordinal
        }else{
            Color.Black.ordinal
        }

        //left to right
        var temp = 0
        var sw = false
        for(y in (j+1)..board.ySize){
            if(boardInt[i][y] == otherColor){
                temp++
            }else{
                if(boardInt[i][y] == color.ordinal){
                    sw = true
                }
                break
            }
        }
        if(temp > 0 && sw){
            for(y in (j+1) until (j+1+temp)){
                boardAns[i][y]++
            }
        }

        //right to left
        temp = 0
        sw = false
        var y = j-1
        while(y >= 0){
            if(boardInt[i][y] == otherColor){
                temp++
            }else{
                if(boardInt[i][y] == color.ordinal){
                    sw = true
                }
                break
            }
            y--
        }
        if(temp > 0 && sw){
            for(y in (j-temp)..(j-1)){
                boardAns[i][y]++
            }
        }

        //top to bottom
        temp = 0
        sw = false
        for (x in (i + 1)..board.xSize) {
            if (boardInt[x][j] == otherColor) {
                temp++
            } else {
                if (boardInt[x][j] == color.ordinal) {
                    sw = true
                }
                break
            }
        }
        if (temp > 0 && sw) {
            for (x in (i + 1) until (i + 1 + temp)) {
                boardAns[x][j]++
            }
        }



        //bottom to top
        temp = 0
        sw = false
        var x = i - 1
        while(x >= 0){
            if(boardInt[x][j] == otherColor){
                temp++
            }else{
                if(boardInt[x][j] == color.ordinal){
                    sw = true
                }
                break
            }
            x--
        }
        if(temp > 0 && sw){
            for(z in (i-temp)..(i-1)){
                boardAns[z][j]++
            }
        }

        //top left to bottom right
        temp = 0
        sw = false
        x = i + 1
        y = j + 1
        while(x <= board.xSize && y <= board.ySize){
            if(boardInt[x][y] == otherColor){
                temp++
            }else{
                if(boardInt[x][y] == color.ordinal){
                    sw = true
                }
                break
            }
            x++
            y++
        }
        if(temp > 0 && sw){
            for(k in 1..temp){
                boardAns[i+k][j+k] ++
            }
        }

        //bottom left to top right
        temp = 0
        sw = false
        x = i - 1
        y = j + 1
        while(x >= 0 && y <= board.ySize){
            if(boardInt[x][y] == otherColor){
                temp++
            }else{
                if(boardInt[x][y] == color.ordinal){
                    sw = true
                }
                break
            }
            x--
            y++
        }
        if(temp > 0 && sw){
            for(k in 1..temp){
                boardAns[i-k][j+k] ++
            }
        }

        //top right to bottom left
        temp = 0
        sw = false
        x = i + 1
        y = j - 1
        while(x <= board.xSize && y >= 0){
            if(boardInt[x][y] == otherColor){
                temp++
            }else{
                if(boardInt[x][y] == color.ordinal){
                    sw = true
                }
                break
            }
            x++
            y--
        }
        if(temp > 0 && sw){
            for(k in 1..temp){
                boardAns[i+k][j-k] ++
            }
        }

        //bottom right to top left
        temp = 0
        sw = false
        x = i - 1
        y = j - 1
        while(x >=0 && y >= 0){
            if(boardInt[x][y] == otherColor){
                temp++
            }else{
                if(boardInt[x][y] == color.ordinal){
                    sw = true
                }
                break
            }
            x--
            y--
        }
        if(temp > 0 && sw){
            for(k in 1..temp){
                boardAns[i-k][j-k] ++
            }
        }


        return boardAns
    }

}