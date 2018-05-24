package ro.pub.cs.systems.eim.practicaltest02.model;

public class TimeInformation {

   public String hour;
   public String minutes;

    public TimeInformation(String hour, String minutes) {
        this.hour = hour;
        this.minutes = minutes;
    }

    @Override
    public String toString() {
        return hour + ':' + minutes;
    }

}
