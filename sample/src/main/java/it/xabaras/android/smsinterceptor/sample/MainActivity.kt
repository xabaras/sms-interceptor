package it.xabaras.android.smsinterceptor

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import it.xabaras.android.smsinterceptor.sample.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val smsInterceptor = SmsInterceptor(this, lifecycle)
        smsInterceptor.setNumberFilter("3474425788")
        smsInterceptor.setBodyFilter({
            it.startsWith("Hello")
        })
        smsInterceptor.startListening { fromNumber, message ->
            AlertDialog.Builder(applicationContext)
                    .setTitle(R.string.app_name)
                    .setMessage(String.format("You received a message from %1\$s.\nMessage:\n%2\$s", fromNumber, message))
                    .create()
                    .show()
        }
    }
}
