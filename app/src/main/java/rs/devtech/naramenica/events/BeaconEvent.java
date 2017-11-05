package rs.devtech.naramenica.events;


import org.altbeacon.beacon.Beacon;

public class BeaconEvent {

    private Beacon beacon;

    public BeaconEvent(Beacon beacon) {
        this.beacon = beacon;
    }

    public Beacon getBeacon() {
        return beacon;
    }
}
