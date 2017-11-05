package rs.devtech.naramenica.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.google.gson.Gson;

import net.gotev.speech.Speech;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.io.IOException;
import java.util.Collection;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rs.devtech.naramenica.network.BeaconModel;

public class MyService extends Service implements BeaconConsumer {
    BeaconManager beaconManager;
    public static String speechText="";
    OkHttpClient client;
    boolean isbinded;
    private boolean requestSent;

    public MyService() {
        client = new OkHttpClient();
        Speech.init(this, getPackageName());

    }

    Runnable networkThread = new Runnable() {
        @Override
        public void run() {
            try {
                String response = getText("http://192.168.100.100:8000/api/v1/beacons/retrieve/" + beaconId+"/");
                Log.i("My Service", "run: " + response);
                BeaconModel model = new Gson().fromJson(response, BeaconModel.class);
                if(speechText.equals(model.getVehicleName())){
                    Speech.getInstance().say("Bus "+model.getVehicleName()+" arrived");
                    speechText="";
                }
                Speech.getInstance().say(model.getInstructions());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };

    private String getText(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();

        return response.body().string();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isbinded) {
            beaconManager = BeaconManager.getInstanceForApplication(this);
            // To detect proprietary beacons, you must add a line like below corresponding to your beacon
            // type.  Do a web search for "setBeaconLayout" to get the proper expression.
//        beaconManager.getBeaconParsers().add(new BeaconParser().
//                setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
                beaconManager.bind(this);
            isbinded=true;
        }
        //EventBus.getDefault().register(MainActivity.class);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    String beaconId;

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0 ) {
                    Beacon beacon=beacons.iterator().next();
//                    if(beacon.getDistance()<11) {
                        Log.i("BEacon", "didRangeBeaconsInRegion: " + beacon.getId1().toString());
                        //  EventBus.getDefault().post(new BeaconEvent(beacons.iterator().next()));
                        beaconId = beacon.getId1().toString();
                        if (!requestSent) {
                            new Thread(networkThread).start();
                            requestSent = true;
                        }
//                    }
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
        }
    }


}
