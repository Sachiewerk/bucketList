package ua.aengussong.www.bucketlist.fingerprint;

import android.app.KeyguardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import ua.aengussong.www.bucketlist.R;
import ua.aengussong.www.bucketlist.activity.MainActivity;

public class FingerprintCheck extends AppCompatActivity {

    private KeyStore keystore;
    private static final String KEY_NAME = "aengussong";
    private Cipher cipher;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint_check);

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        FingerprintManager fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (!fingerprintManager.isHardwareDetected() || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Intent intent = new Intent(FingerprintCheck.this, MainActivity.class);
            startActivity(intent);
        } else {
            if (!fingerprintManager.hasEnrolledFingerprints()) {
                Intent intent = new Intent(FingerprintCheck.this, MainActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.register_fingerprint));
                startActivity(intent);
            } else {
                if (!keyguardManager.isKeyguardSecure()) {
                    Intent intent = new Intent(FingerprintCheck.this, MainActivity.class);
                    intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.enable_lock));
                    startActivity(intent);
                } else {
                    genKey();

                    if (cypherInit()) {
                        FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
                        FingerprintHelper helper = new FingerprintHelper(this);
                        helper.startAuthentification(fingerprintManager, cryptoObject);
                    }
                }
            }
        }
    }

    private void genKey() {
        try {
            keystore = KeyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }

        try {
            keystore.load(null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setUserAuthenticationRequired(true)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .build());
            }
            keyGenerator.generateKey();
        } catch (IOException | NoSuchAlgorithmException | CertificateException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

    }

    private boolean cypherInit() {
        try {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES+"/"
                    +KeyProperties.BLOCK_MODE_CBC+"/"+KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();}

            try {
                keystore.load(null);
                SecretKey key = (SecretKey) keystore.getKey(KEY_NAME, null);
                cipher.init(cipher.ENCRYPT_MODE, key);
                return true;
            } catch (IOException e1) {
                e1.printStackTrace();
                return false;
            } catch (NoSuchAlgorithmException e1) {
                e1.printStackTrace();
                return false;
            } catch (CertificateException e1) {
                e1.printStackTrace();
                return false;
            } catch (UnrecoverableKeyException e1) {
                e1.printStackTrace();
                return false;
            } catch (KeyStoreException e1) {
                e1.printStackTrace();
                return false;
            } catch (InvalidKeyException e1) {
                e1.printStackTrace();
                return false;
            }
    }
}
