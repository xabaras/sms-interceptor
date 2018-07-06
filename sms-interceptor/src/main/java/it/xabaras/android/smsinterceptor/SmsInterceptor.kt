package it.xabaras.android.smsinterceptor

import android.Manifest
import android.Manifest.permission.RECEIVE_SMS
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.support.annotation.RequiresPermission
import android.support.v4.content.ContextCompat
import android.telephony.SmsMessage

/**
 * A simple, lifecycle aware, utility class to help you intercept sms messages from within Android apps
 */
class SmsInterceptor(val context: Context, val lifecycle: Lifecycle? = null) : LifecycleObserver {
    private var numberFilter: List<String> = listOf()
    private var bodyFilter: String? = null
    private var lambdaBodyFilter: ((String) -> Boolean)? = null
    private var onSmsReceivedListener: OnSmsReceivedListener? = null
    private var onSmsReceivedCallback: ((fromNumber: String, message: String) -> Unit )? = null
    private lateinit var mBroadcastReceiver: SmsBroadcastReceiver
    private var isListening: Boolean = false
    private val SMS_RECEIVED_ACTION: Lazy<String> = lazy {
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT )
            Telephony.Sms.Intents.SMS_RECEIVED_ACTION
        else
            "android.provider.Telephony.SMS_RECEIVED"
    }

    /**
     * Sets one or more numbers to filter the messages on
     *
     * @param[numberFilter] one or more phone numbers
     */
    fun setNumberFilter(vararg numberFilter: String) {
        this.numberFilter = numberFilter.toList()
    }

    /**
     * Sets a regular expression that will be used to filter messages based on their body
     *
     * @param[regExp] a regular expression to test the body on
     */
    fun setBodyFilter(regExp : String) {
        this.bodyFilter = regExp
        numberFilter
    }

    /**
     * Sets a lambda expression that will be used to filter messages based on their body
     *
     * @param[filter] a lambda expression to test the body on
     */
    fun setBodyFilter(filter: (messageBody: String) -> Boolean) {
        this.lambdaBodyFilter = filter
    }

    /**
     * Registers a BroadcastReceiver and starts listening for incoming SMSs
     * @param listener
     */
    @RequiresPermission(RECEIVE_SMS)
    fun startListening(listener: OnSmsReceivedListener) {
        if (!arePermissionsGranted()) return

        inner_startListening()

        onSmsReceivedListener = listener
    }

    /**
     * Registers a BroadcastReceiver and starts listening for incoming SMSs
     * @param listener
     */
    @RequiresPermission(RECEIVE_SMS)
    fun startListening(callback: ((fromNumber: String, message: String) -> Unit )?) {
        if (!arePermissionsGranted()) return

        inner_startListening()

        onSmsReceivedCallback = callback
    }

    private fun inner_startListening() {
        if ( !this::mBroadcastReceiver.isInitialized ) {
            mBroadcastReceiver = SmsBroadcastReceiver()
        }
        context.registerReceiver(
                mBroadcastReceiver,
                IntentFilter(SMS_RECEIVED_ACTION.value)
        )
        lifecycle?.addObserver(this)
        isListening = true
    }

    fun arePermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Finishes listening for incoming SMSs
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    @Throws(IllegalStateException::class)
    fun inner_stopListening() {
        if ( !this::mBroadcastReceiver.isInitialized ) throw IllegalStateException("You must have first called startListening")

        context.unregisterReceiver(mBroadcastReceiver)
        isListening = false
    }

    /**
     * Finishes listening for incoming SMSs
     */
    @Throws(IllegalStateException::class)
    fun stopListening() {
        inner_stopListening()
        lifecycle?.removeObserver(this)
    }

    /**
     * Resumes listening (intended to be used for lifecycle handling, e.g. Activity.onResume if you called stopListening in onPause or onStop )
     * @throws IllegalStateException if you didn't call startListening
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    @Throws(IllegalStateException::class)
    fun resumeListening() {
        if ( !this::mBroadcastReceiver.isInitialized ) throw IllegalStateException("You must have first called startListening")

        if (isListening) return

        context.registerReceiver(
                mBroadcastReceiver,
                IntentFilter(SMS_RECEIVED_ACTION.value)
        )

        isListening = true
    }

    private inner class SmsBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if ( intent?.action == SMS_RECEIVED_ACTION.value ) {
                val messages: List<SmsMessage> = getMessagesFromIntent(intent)
                for (msg: SmsMessage in messages) {
                    val fromNumber = msg.displayOriginatingAddress

                    if ( numberFilter.isNotEmpty() && !numberFilter.contains(fromNumber) ) continue

                    val message = msg.displayMessageBody

                    if ( bodyFilter?.isNotEmpty() == true && !message.matches(Regex.fromLiteral(bodyFilter!!)) ) continue


                    if ( lambdaBodyFilter != null && lambdaBodyFilter?.invoke(message) != true ) continue

                    onSmsReceivedListener?.onSmsReceived(fromNumber, message)
                    onSmsReceivedCallback?.invoke(fromNumber, message)
                }
            }
        }

        @Suppress("DEPRECATION")
        fun getMessagesFromIntent(intent: Intent?) : List<SmsMessage> {
            var messages: ArrayList<SmsMessage> = arrayListOf()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                messages.addAll(Telephony.Sms.Intents.getMessagesFromIntent(intent)!!)
            } else {
                val bundle: Bundle? = intent?.extras

                bundle?.let {
                    val pdus = bundle.get("pdus") as Array<*>?
                    pdus?.let {
                        for ( o in it  ) {
                            val msg: SmsMessage
                            val format = bundle.getString("format")
                            msg = if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M )
                                SmsMessage.createFromPdu(o as ByteArray, format)
                            else
                                SmsMessage.createFromPdu(o as ByteArray)

                            messages.add(msg)
                        }
                    }
                }
            }
            return messages
        }
    }

    interface OnSmsReceivedListener {
        fun onSmsReceived(fromNumber: String, message: String)
    }
}