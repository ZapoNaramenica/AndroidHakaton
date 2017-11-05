package rs.devtech.naramenica.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.RemoteException;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pwittchen.swipe.library.Swipe;
import com.github.pwittchen.swipe.library.SwipeListener;
import com.google.gson.Gson;

import net.gotev.speech.GoogleVoiceTypingDisabledException;
import net.gotev.speech.Speech;
import net.gotev.speech.SpeechDelegate;
import net.gotev.speech.SpeechRecognitionNotAvailable;
import net.gotev.speech.ui.SpeechProgressView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import rs.devtech.naramenica.R;
import rs.devtech.naramenica.events.BeaconEvent;
import rs.devtech.naramenica.network.BeaconModel;
import rs.devtech.naramenica.service.MyService;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.topLeftButton)
    ImageButton topLeftButton;
    @BindView(R.id.topRightButton)
    ImageButton topRightButton;
    @BindView(R.id.bottomLeftButton)
    ImageButton bottomLeftButton;
    @BindView(R.id.buttomRightButton)
    ImageButton bottomRightButton;

    @BindView(R.id.progress)
    SpeechProgressView progressView;

    private Swipe swipe;

    @BindView(R.id.main_container)
    LinearLayout mContainer;
    TextToSpeech textToSpeech;
    private OkHttpClient client;
    private boolean isServieStarted ;
    private String speechResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Speech.init(this, getPackageName());

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.UK);
                }
            }
        });


        swipe = new Swipe();
        swipe.setListener(new SwipeListener() {
            @Override
            public void onSwipingLeft(final MotionEvent event) {
            }

            @Override
            public void onSwipedLeft(final MotionEvent event) {

                if(!isServieStarted) {
                    ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(200);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    startService(new Intent(MainActivity.this, MyService.class));
                    isServieStarted=true;
                    Toast.makeText(MainActivity.this, "Service enabled", Toast.LENGTH_SHORT).show();

                }else{
                    ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(200);

                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(200);

                    isServieStarted=false;
                    Toast.makeText(MainActivity.this, "Service disabled", Toast.LENGTH_SHORT).show();

                    stopService(new Intent(MainActivity.this, MyService.class));
                }
            }

            @Override
            public void onSwipingRight(final MotionEvent event) {
            }

            @Override
            public void onSwipedRight(final MotionEvent event) {
                ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(200);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSwipingUp(final MotionEvent event) {
            }

            @Override
            public void onSwipedUp(final MotionEvent event) {
                ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(200);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSwipingDown(final MotionEvent event) {
            }

            @Override
            public void onSwipedDown(final MotionEvent event) {
                ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(200);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                MainActivityPermissionsDispatcher.implementSpeechRecognitionWithPermissionCheck(MainActivity.this);
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        swipe.dispatchTouchEvent(ev);
        return super.dispatchTouchEvent(ev);

    }

    @Subscribe
    public void onEvent(SwipeRighEvent event) {
        Toast.makeText(MainActivity.this, "right", Toast.LENGTH_SHORT).show();

    }

    @NeedsPermission(Manifest.permission.RECORD_AUDIO)
    public void implementSpeechRecognition() {
        try {
            if (Speech.getInstance().isListening()) {
                Speech.getInstance().stopListening();
            } else {
                Speech.getInstance().startListening(new SpeechDelegate() {
                    @Override
                    public void onStartOfSpeech() {
                        Log.i("speech", "speech recognition is now active");
                        progressView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onSpeechRmsChanged(float value) {
                        Log.d("speech", "rms is now: " + value);
                    }

                    @Override
                    public void onSpeechPartialResults(List<String> results) {
                        StringBuilder str = new StringBuilder();
                        for (String res : results) {
                            str.append(res).append(" ");
                        }

                        Log.i("speech", "partial result: " + str.toString().trim());
                    }

                    @Override
                    public void onSpeechResult(String result) {
                        progressView.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
                        MyService.speechText=result;
                    }
                });
            }
        } catch (SpeechRecognitionNotAvailable exc) {
            Log.e("speech", "Speech recognition is not available on this device!");
            // You can prompt the user if he wants to install Google App to have
            // speech recognition, and then you can simply call:
            //
            // SpeechUtil.redirectUserToGoogleAppOnPlayStore(this);
            //
            // to redirect the user to the Google App page on Play Store
        } catch (GoogleVoiceTypingDisabledException exc) {
            Log.e("speech", "Google voice typing must be enabled!");
        }

    }

    @Override
    protected void onDestroy() {
        Speech.getInstance().shutdown();
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
    Runnable networkThread = new Runnable() {
        @Override
        public void run() {
            try {
                String response = getText("");
                BeaconModel model = new Gson().fromJson(response, BeaconModel.class);
                Speech.getInstance().say(model.getInstructions());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };

    private String getText(String url) throws IOException {
        Request request = new Request.Builder()
                .url("http://192.168.100.100:8000/api/v1/beacons/")
                .addHeader("Authorization", "Basic a29yaXNuaWs6S3JpbEMzU2xlcDBnTWlzaEA=")
                .build();

        Response response = client.newCall(request).execute();

        return response.body().string();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBeaconEvent(BeaconEvent event) {
        Log.i("HELLLO_WORLD", event.getBeacon().getParserIdentifier());

    }

    @OnClick(R.id.topLeftButton)
    public void onLeftTopClick() {
        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        toneGen1.startTone(ToneGenerator.TONE_CDMA_ALERT_INCALL_LITE, 150);
        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(50);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        textToSpeech.speak("TOP LEFT BUTTON", TextToSpeech.QUEUE_FLUSH, null);
    }

    @OnClick(R.id.topRightButton)
    public void onRightTopCLick() {
        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        toneGen1.startTone(ToneGenerator.TONE_CDMA_CALL_SIGNAL_ISDN_INTERGROUP, 150);
        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(200);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        textToSpeech.speak("TOP RIGHT BUTTON", TextToSpeech.QUEUE_FLUSH, null);
    }

    @OnClick(R.id.bottomLeftButton)
    public void onBottomLeftClick() {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale(Manifest.permission.RECORD_AUDIO)
    void showRationaleForRecordVoice(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setMessage("This permission is required for feature speech recognizer")
                .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        request.proceed();
                    }
                })
                .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        request.cancel();
                    }
                }).show();


    }

    @OnPermissionDenied(Manifest.permission.RECORD_AUDIO)
    void showDeniedForRecordVoice() {
        Toast.makeText(this, "RECORD_AUDIO permission denied", Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.RECORD_AUDIO)
    void showNeverAskForRecordVoice() {
        Toast.makeText(this, "Record audio", Toast.LENGTH_SHORT).show();
    }

}
