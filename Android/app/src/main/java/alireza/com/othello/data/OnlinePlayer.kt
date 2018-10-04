package alireza.com.othello.data

class OnlinePlayer(name: String,val email: String,var state: String,var score: Int,val secretID : String) : Player(name, Color.None)