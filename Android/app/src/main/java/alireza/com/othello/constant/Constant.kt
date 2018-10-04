package alireza.com.othello.constant

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

const val MASSAGE = "massagePresrence"
const val MASSAGE_COUNT = "PLAYER_TWO"
const val MASSAGE_SENDER = "ms_sender"
const val MASSAGE_DATE = "ms_tarikh"
const val MASSAGE_IS_READ = "Ms_readed"
const val MASSAGE_TEXT = "ms_mat"
const val MASSAGE_MODE = "Massage_Mode"
const val MASSAGE_NUM = "Massage_number"

const val HASH_MAP = "saveAiData"
const val HASH_MAP_FIRST = "saveAiDataOther"


const val PLAYER_INFO = "playerInfo"
const val EXIST = "exist"
const val IS_USER = "thatUser"
const val IS_GOLD = "thatGold"
const val PLAYER_NAME = "playerName"
const val PLAYER_EMAIL = "PlayerEmail"
const val PLAYER_PASSWORD = "PlayerPassWord"
const val PLAYER_SECRET_ID = "PlayErSecret"
const val BOARD_ID = "boardID"
const val FRIEND_NAME = "friendName"

const val SETTING = "Setting"
const val THEME = "Theme"
const val LANGUAGE = "lang"

const val URL_STR = "http://cryptic-ravine-76227.herokuapp.com"

fun streamToString(stream: InputStream): String {
    var str = ""

    try {
        val reader = BufferedReader(InputStreamReader(stream))
        str = reader.readText()
        reader.close()
    } catch (ex: Exception) {
        ex.printStackTrace()
    }


    return str
}

fun hideKeyboard(activity: Activity) {
    val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    //Find the currently focused view, so we can grab the correct window token from it.
    var view = activity.currentFocus
    //If no view currently has focus, create a new one, just so we can grab a window token from it
    if (view == null) {
        view = View(activity)
    }
    imm!!.hideSoftInputFromWindow(view.windowToken, 0)
}