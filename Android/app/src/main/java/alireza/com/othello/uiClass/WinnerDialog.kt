package alireza.com.othello.uiClass

import alireza.com.othello.R
import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import kotlinx.android.synthetic.main.winner_dialog.*

class WinnerDialog(private val activity: Activity,private val winner : String) : Dialog(activity),View.OnClickListener {

        lateinit var btn : Button

        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.winner_dialog)
        btn = okBtn
        btn.setOnClickListener(this)
        winnerText.text = if(winner == "draw"){
            "The Game is Draw"
        }else {
            "$winner is findWinner"
        }
    }

    override fun onClick(p0: View?) {
        when (p0){
            btn ->{
                activity.finish()
            }
        }
        dismiss()
    }

    override fun onStop() {
        activity.finish()
        super.onStop()
    }

}