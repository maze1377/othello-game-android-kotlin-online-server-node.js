package alireza.com.othello.data

class Counter(position: CounterPosition,color : Color) {

    val position : CounterPosition = position
    var color : Color = Color.None
    init {
        this.color = color
    }

}