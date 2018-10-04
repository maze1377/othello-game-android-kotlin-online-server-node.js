package alireza.com.othello

import alireza.com.othello.constant.EXIST
import alireza.com.othello.constant.LANGUAGE
import alireza.com.othello.constant.SETTING
import alireza.com.othello.constant.THEME
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_wellcome.*
import java.util.*

class Welcome : AppCompatActivity() {

    private var lang : String = "en"
    private var theme : String = "default"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSetting()
        loadLang()
        setContentView(R.layout.activity_wellcome)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)

        blink()
    }

    private fun loadLang(){
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }

    private fun getSetting(){
        val data = getSharedPreferences(SETTING, Context.MODE_PRIVATE)
        val sw = data.getBoolean(EXIST,false)
        if(sw) {
            lang = data.getString(LANGUAGE,"fa")
            theme = data.getString(THEME,"default")
        }else{
            Log.i("Welcome-getSetting","data not found")
            val editor = data.edit()
            editor.putBoolean(EXIST,true)
            editor.putString(THEME,"default")
            editor.putString(LANGUAGE,"en")
            editor.apply()
        }
    }

    fun touched(view : View){
        val intent = Intent(this, Menu::class.java)
        startActivity(intent)
        finish()
    }

    private fun blink() {
        val handler = Handler()
        Thread(Runnable {
            val timeToBlink = 500    //in mil
            try {
                Thread.sleep(timeToBlink.toLong())
            } catch (e: Exception) {
            }

            handler.post {
                val txt = touchToStart as TextView
                if (txt.visibility == View.VISIBLE) {
                    txt.visibility = View.INVISIBLE
                } else {
                    txt.visibility = View.VISIBLE
                }
                blink()
            }
        }).start()

    }

}
