package it.xabaras.android.smsinterceptor.sample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import it.xabaras.android.smsinterceptor.SmsInterceptor

/**
 * Created by Paolo Montalto on 04/07/18.
 */
class KotlinSampleActivity : AppCompatActivity() {
    private val smsInterceptor: SmsInterceptor = SmsInterceptor(this, lifecycle)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        smsInterceptor.setNumberFilter("+3912345678", "+441235678")
        smsInterceptor.setBodyFilter {
            it.startsWith("Hello")
        }
    }

    fun startSmsInterceptor(view: View?) {
        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED ) {
            smsInterceptor.startListening { fromNumber, message ->
                AlertDialog.Builder(this@KotlinSampleActivity)
                        .setTitle(R.string.app_name)
                        .setMessage("You received a message from $fromNumber.\nMessage:\n$message")
                        .setNeutralButton(android.R.string.ok, null)
                        .create()
                        .show()
            }
            Toast.makeText(this, "Started listening!", Toast.LENGTH_LONG).show()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECEIVE_SMS), 1001)
        }
    }

    fun stopSmsInterceptor(view: View) {
        try {
            smsInterceptor?.stopListening()
            Toast.makeText(this, "Ended listening!", Toast.LENGTH_LONG).show()
        } catch (e: IllegalStateException) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 1001) {
            if ( grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSmsInterceptor(null)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
