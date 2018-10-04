package alireza.com.othello

import alireza.com.othello.constant.*
import alireza.com.othello.uiClass.Massage
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.activity_massage.*
import kotlinx.android.synthetic.main.other_massage_layout.view.*
import java.sql.Date

class Massage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_massage)

        val pms = getMassageFromStorage()
        val adapter = MassageListAdapter(this,pms)
        mainList.adapter = adapter

    }

    inner class MassageListAdapter(private val context : Context,private val massageList : ArrayList<Massage>) : BaseAdapter() {

        @SuppressLint("ViewHolder", "InflateParams")
        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
            val pm = massageList[p0]
            val inflator = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflator.inflate(R.layout.other_massage_layout,null)
            view.massageSender.text = pm.sender
            val txt = if (pm.text.length > 30) {
                "${pm.text.subSequence(0, 30)}..."
            }else{
                pm.text
            }
            view.massageText.text = txt
            view.time.text = pm.date.toString()
            view.readState.text = if(pm.isRead){
                "read"
            }else{
                "not read"
            }
            view.setOnClickListener{
                massageList[p0].isRead = true
                view.readState.text = "read"
                saveMassage(massageList)
                val intent = Intent(context,ShowMassage::class.java)
                intent.putExtra(MASSAGE_NUM,p0)
                startActivity(intent)

            }
            return view
        }

        override fun getItem(p0: Int): Any {
            return massageList[p0]
        }

        override fun getItemId(p0: Int): Long {
            return p0.toLong()
        }

        override fun getCount(): Int {
            return massageList.size
        }
    }

    private fun saveMassage(massage : ArrayList<alireza.com.othello.uiClass.Massage>){
        val data = getSharedPreferences(MASSAGE, Context.MODE_PRIVATE)
        val sw = data.getBoolean(EXIST,false)
        if(sw) {
        }else{
            val editor = data.edit()
            editor.putBoolean(EXIST,true)
            editor.putInt(MASSAGE_COUNT,massage.size)
            editor.apply()
        }
        var i = 0
        massage.forEach {
            Log.i("save in Massage","${it.isRead}")
            val oneMassage = getSharedPreferences("mass$i", Context.MODE_PRIVATE)
            val edit = oneMassage.edit()
            edit.putLong(MASSAGE_DATE,it.date.time)
            edit.putString(MASSAGE_TEXT,it.text)
            edit.putString(MASSAGE_SENDER,it.sender)
            edit.putString(MASSAGE_MODE,it.mode)
            edit.putBoolean(MASSAGE_IS_READ,it.isRead)
            edit.apply()
            edit.commit()
            i++
        }
    }
    private fun getMassageFromStorage() : ArrayList<alireza.com.othello.uiClass.Massage>{
        val data = getSharedPreferences(MASSAGE, Context.MODE_PRIVATE)
        val sw = data.getBoolean(EXIST, false)
        val pms = ArrayList<alireza.com.othello.uiClass.Massage>()
        if (sw) {
            val i = data.getInt(MASSAGE_COUNT, 0)
            for (index in 0 until i) {
                val dataGetter = getSharedPreferences("mass$index", Context.MODE_PRIVATE)
                val sender = dataGetter.getString(MASSAGE_SENDER,"unknown")
                val date = Date(dataGetter.getLong(MASSAGE_DATE,0))
                val text = dataGetter.getString(MASSAGE_TEXT,"empty")
                val mode = dataGetter.getString(MASSAGE_MODE,"none")
                val read = dataGetter.getBoolean(MASSAGE_IS_READ,false)
                val pm = alireza.com.othello.uiClass.Massage(sender,text,date,read,mode)
                pms.add(pm)
            }
        }
        return pms
    }

}
