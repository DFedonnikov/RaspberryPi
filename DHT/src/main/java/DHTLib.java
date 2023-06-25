public class DHTLib {
    public native int readDHT11(int pin);
    public native double getHumidity();
    public native double getTemperature();
}
