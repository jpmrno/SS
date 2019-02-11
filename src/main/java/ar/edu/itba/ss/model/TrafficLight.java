package ar.edu.itba.ss.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class TrafficLight {

  private final Map<Status, Integer> statusTimes;
  private Status currentStatus;
  private int time;

  public static TrafficLight ALWAYS_GREEN = new TrafficLight(1,0,0, Status.GREEN);
  public static TrafficLight ALWAYS_RED = new TrafficLight(0,0,1, Status.RED);

  public TrafficLight(final int greenTime, final int yellowTime, final int redTime, final Status currentStatus) {
    this.statusTimes = new HashMap<>();
    this.statusTimes.put(Status.GREEN, greenTime);
    this.statusTimes.put(Status.YELLOW_TO_RED, yellowTime);
    this.statusTimes.put(Status.RED, redTime);
    this.statusTimes.put(Status.YELLOW_TO_GREEN, yellowTime);
    this.currentStatus = Objects.requireNonNull(currentStatus);
  }

  public void timeLapse() {
    time++;
    if (time >= statusTimes.get(currentStatus)) {
      currentStatus = currentStatus.nextStatus();
      time = 0;

      if(statusTimes.get(currentStatus) == 0) {
        timeLapse();
      }
    }


  }

  public Status currentStatus() {
    return currentStatus;
  }

  public enum Status {
    // Order matters
    GREEN(100),
    YELLOW_TO_RED(1),
    RED(0),
    YELLOW_TO_GREEN(1);

    private final int additionalDistance;

    Status(final int additionalDistance) {
      this.additionalDistance = additionalDistance;
    }

    public int additionalDistance() {
      return additionalDistance;
    }

    public Status nextStatus() {
      final int i = ordinal();
      final Status[] statuses = Status.values();
      if (i == statuses.length - 1) {
        return statuses[0];
      }
      return statuses[i + 1];
    }
  }
}
