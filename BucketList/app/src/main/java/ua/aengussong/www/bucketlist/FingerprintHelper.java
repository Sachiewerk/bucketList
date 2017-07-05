package ua.aengussong.www.bucketlist;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;

/**
 * Created by coolsmileman on 13.06.2017.
 */

@RequiresApi(api = Build.VERSION_CODES.M)
class FingerprintHelper extends FingerprintManager.AuthenticationCallback{

    private Context context;

    public FingerprintHelper(Context context){
        this.context = context;
    }

    public void startAuthentification(FingerprintManager fingerprintManager, FingerprintManager.CryptoObject cryptoObject) {
        CancellationSignal onCancellationSignal = new CancellationSignal();
        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED)
            return;
        fingerprintManager.authenticate(null, onCancellationSignal,0,this,null);
    }

    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }
}

