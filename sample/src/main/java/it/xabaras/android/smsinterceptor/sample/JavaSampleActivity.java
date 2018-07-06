package it.xabaras.android.smsinterceptor.sample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import it.xabaras.android.smsinterceptor.SmsInterceptor;

/**
 * Created by Paolo Montalto on 04/07/18.
 */
public class JavaSampleActivity extends AppCompatActivity {
    private SmsInterceptor smsInterceptor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        smsInterceptor = new SmsInterceptor(this, getLifecycle());
        smsInterceptor.setNumberFilter("+3912345678");
        smsInterceptor.setBodyFilter("^Hello.*$");
    }

    public void startSmsInterceptor(View view) {
        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED ) {
            smsInterceptor.startListening(new SmsInterceptor.OnSmsReceivedListener() {
                @Override
                public void onSmsReceived(@NotNull String fromNumber, @NotNull String message) {
                    new AlertDialog.Builder(JavaSampleActivity.this)
                            .setTitle(R.string.app_name)
                            .setMessage(String.format("You received a message from %1$s.\nMessage:\n%2$s", fromNumber, message))
                            .setNeutralButton(android.R.string.ok, null)
                            .create()
                            .show();
                }
            });
            Toast.makeText(this, "Started listening!", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECEIVE_SMS}, 1001);
        }
    }

    public void stopSmsInterceptor(View view) {
        if ( smsInterceptor != null ) {
            smsInterceptor.stopListening();
            Toast.makeText(this, "Ended listening!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if ( requestCode == 1001  ) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSmsInterceptor(null);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
