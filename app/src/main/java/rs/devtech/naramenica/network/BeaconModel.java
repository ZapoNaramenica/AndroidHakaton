package rs.devtech.naramenica.network;

/**
 * Created by Nikola on 11/5/2017.
 */

public class BeaconModel {
    int id;
    String vehicle_name;
    String unique_id;
    String trace;
    String instructions;

    public int getId() {
        return id;
    }

    public String getVehicleName() {
        return vehicle_name;
    }

    public String getUniqueId() {
        return unique_id;
    }

    public String getTrace() {
        return trace;
    }

    public String getInstructions() {
        return instructions;
    }
}
