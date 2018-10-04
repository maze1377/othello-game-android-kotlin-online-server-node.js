package alireza.com.othello

import alireza.com.othello.constant.*
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_setting.*
import java.util.*

class Setting : AppCompatActivity() , AdapterView.OnItemSelectedListener{

    private lateinit var username : TextView
    private lateinit var edit : Button
    private lateinit var friendManage : Button
    private lateinit var langSpinner : Spinner
    private lateinit var themeSpinner : Spinner
    private val themeData = ArrayList<String>()
    private val langData = ArrayList<String>()

    private var secretID = " "
    private var name = " "
    private var isUser = false

    private var lang : String = "en"
    private var theme : String? = null
    private var sw = 0
    private var signIn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSetting()
        loadLang()
        loadTheme(this)
        setContentView(R.layout.activity_setting)

        friendManage = friendManageBtn
        edit = editBtn
        username = idSetting
        langSpinner = langSpinnerId
        themeSpinner = themeSpinnerId

        themeData.add(resources.getString(R.string.def))
        themeData.add(resources.getString(R.string.dark))
        themeData.add(resources.getString(R.string.blue))
        themeData.add(resources.getString(R.string.wood))

        langData.add(resources.getString(R.string.en))
        langData.add(resources.getString(R.string.fa))

        makeUI()

    }

    @Suppress("DEPRECATION")
    private fun loadLang() {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }

    private fun loadTheme(activity: Activity) {
        Log.i("loadTheme", theme)
        when (theme) {
            resources.getString(R.string.def) -> {
                activity.setTheme(R.style.AppTheme)
            }
            resources.getString(R.string.dark) -> {
                activity.setTheme(R.style.dark)
            }
            resources.getString(R.string.wood) ->{
                activity.setTheme(R.style.wooden)
            }
            resources.getString(R.string.blue) ->{
                activity.setTheme(R.style.blue)
            }
            else -> {
                activity.setTheme(R.style.AppTheme)
            }
        }
    }

    private fun makeUI(){

        //show username
        username.text = name
        if(!signIn){
            val str = "Sign in"
            edit.text = str
        }


        // btn to edit the username
        edit.setOnClickListener {
            if (signIn) {
                val dialog = AlertDialog.Builder(this)
                dialog.setTitle("Enter new username")
                val txt = EditText(this)
                txt.setText(R.string.username)
                dialog.setView(txt)

                dialog.setPositiveButton("Ok") { dialogInterface: DialogInterface, i: Int ->

                    val text = txt.text.toString()
                    if (text.length > 4) {
                        saveUsername(text)
                        dialogInterface.dismiss()
                    } else {
                        Toast.makeText(this, "Enter valid username", Toast.LENGTH_SHORT).show()
                    }
                }

                dialog.setNegativeButton("Cancel") { dialogInterface: DialogInterface, i: Int ->
                    dialogInterface.cancel()
                }

                dialog.show()
            }else{
                val intent = Intent(this,LoginOrNot::class.java)
                startActivity(intent)
            }
        }

        // btn go to friend page
        friendManage.setOnClickListener {
            if(signIn){
                val intent = Intent(this,FriendManage::class.java)
                startActivity(intent)
            }else{
                Toast.makeText(this,"need to Login",Toast.LENGTH_SHORT).show()
            }
        }

        //make lang and theme spinner
        var adapter  = ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,themeData)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        themeSpinner.adapter = adapter
        themeSpinner.onItemSelectedListener = this
        themeSpinner.setSelection(themeData.indexOf(theme))


        adapter  = ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,langData)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        langSpinner.adapter = adapter
        langSpinner.onItemSelectedListener = this
        val l :Int = getIndexOfLang()
        langSpinner.setSelection(l)

    }

    private fun getIndexOfLang() : Int{
        return when(lang){
            "en" -> 0
            "fa" -> 1
            "ra" -> 2
            else -> 0
        }

    }

    private fun saveUsername(text : String){
        //TODO send new username to server
    }


    private fun getSetting(){
        var data = getSharedPreferences(SETTING, Context.MODE_PRIVATE)
        val sw = data.getBoolean(EXIST,false)
        if (sw) {
            lang = data.getString(LANGUAGE,"en")
            theme = data.getString(THEME,"default")
        }
        data = getSharedPreferences(PLAYER_INFO, Context.MODE_PRIVATE)
        signIn = data.getBoolean(EXIST,false)
        if(signIn){
            name = data.getString(PLAYER_NAME,"Unknown")
            secretID = data.getString(PLAYER_SECRET_ID,"                ")
            isUser = data.getBoolean(IS_USER,false)
        }else{
            name = "not Signed in"
        }

    }

    private fun saveSetting(){
        val data = getSharedPreferences(SETTING, Context.MODE_PRIVATE)
        val editor = data.edit()
        putTheme(editor)
        putLang(editor)
        editor.apply()
    }

    private fun putLang(editor : SharedPreferences.Editor){
        val index = langSpinner.selectedItemId.toInt()
        when(index){
            0 ->{
                editor.putString(LANGUAGE,"en")
            }
            1 ->{
                editor.putString(LANGUAGE,"fa")
            }
            2 ->{
                editor.putString(LANGUAGE,"ru")
            }
            else -> {
                Toast.makeText(this,"shit",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun putTheme(editor: SharedPreferences.Editor){
        val index = themeSpinner.selectedItemId.toInt()

        when(index){
            0 ->{
                editor.putString(THEME,"default")
            }
            1 ->{
                editor.putString(THEME,"dark")
            }
            2 ->{
                editor.putString(THEME,"blue")
            }
            3 ->{
                editor.putString(THEME,"wooden")
            }
            else -> {
                Toast.makeText(this,"shit",Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStop() {
        saveSetting()
        super.onStop()
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        Log.i("Setting-nothing","shit")
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        if (themeSpinner == p0 && sw > 1) {
            theme = themeData[p2]
            saveSetting()
            finish()
            startActivity(intent)
        }
        if (p0 == langSpinner && sw > 1) {
            lang = langData[p2]
            saveSetting()
            finish()
            startActivity(intent)
        }
        sw++

    }
}
