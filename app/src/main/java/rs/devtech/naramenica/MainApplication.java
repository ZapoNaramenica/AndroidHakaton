package rs.devtech.naramenica;

import android.app.Application;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collection;

import rs.devtech.naramenica.events.BeaconEvent;
import rs.devtech.naramenica.ui.MainActivity;

public class MainApplication extends Application   {
    private static final String TAG = ".MyApplicationName";
    private RegionBootstrap regionBootstrap;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "App started up");

    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onBeaconEvent(BeaconEvent event) {
        Log.i("BGRND", event.getBeacon().getParserIdentifier());
    }
}
