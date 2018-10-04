package alireza.com.othello

import alireza.com.othello.constant.EXIST
import alireza.com.othello.constant.LANGUAGE
import alireza.com.othello.constant.SETTING
import alireza.com.othello.constant.THEME
import alireza.com.othello.server.CheckState
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login_or_not.*
import java.util.*

class LoginOrNot : AppCompatActivity() {

    private var lang = "en"
    private var theme : String = " "
    private fun getSetting(){
        val data = getSharedPreferences(SETTING, Context.MODE_PRIVATE)
        val sw = data.getBoolean(EXIST,false)
        if(sw) {
            lang = data.getString(LANGUAGE,"en")
            theme = data.getString(THEME,"default")
        }
    }
    private fun loadLang() {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSetting()
        loadLang()
        setContentView(R.layout.activity_login_or_not)

        login.setOnClickListener {
            val sw = CheckState().isConnected(this)
            if(sw){
                val intent = Intent(this,Login::class.java)
                startActivity(intent)
                finish()
            }else{
                Toast.makeText(this,"Check Internet Connection", Toast.LENGTH_LONG).show()
            }
        }

        register.setOnClickListener{
            val sw = CheckState().isConnected(this)
            if(sw){
                val intent = Intent(this,Register::class.java)
                startActivity(intent)
                finish()
            }else{
                Toast.makeText(this,"Check Internet Connection", Toast.LENGTH_LONG).show()
            }
        }

        signInAsGuest.setOnClickListener{
            val sw = CheckState().isConnected(this)
            if(sw){
                doSomething()
                val intent = Intent(this,Menu::class.java)
                startActivity(intent)
                finish()
            }else{
                Toast.makeText(this,"Check Internet Connection", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun doSomething(){

    }
}
