package alireza.com.othello.uiClass

import alireza.com.othello.R
import android.content.Context
import android.media.MediaPlayer

object Player {

    private lateinit var mp : MediaPlayer

    fun play(ctx : Context, i : Int){
        mp = when(i){
            1 ->{
                MediaPlayer.create(ctx, R.raw.singel_player_win)
            }
            2 ->{
                MediaPlayer.create(ctx, R.raw.start_sound)
            }
            3 ->{
                MediaPlayer.create(ctx, R.raw.start_sound)
            }
            4 ->{
                MediaPlayer.create(ctx, R.raw.start_sound)
                //TODO find notification sound
            }
            else -> {
                MediaPlayer()
            }

        }
        mp.start()
    }


}