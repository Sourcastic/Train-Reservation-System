package com.example.trainreservationsystem.services;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.trainreservationsystem.models.Route;
import com.example.trainreservationsystem.models.RouteSegment;
import com.example.trainreservationsystem.models.Schedule;
import com.example.trainreservationsystem.repositories.RouteRepository;
import com.example.trainreservationsystem.repositories.ScheduleRepository;
import com.example.trainreservationsystem.repositories.TrainRepository;

public class TrainService {
  private final TrainRepository repository;
  private final RouteRepository routeRepository;
  private final ScheduleRepository scheduleRepository;

  public TrainService(TrainRepository repository) {
    this.repository = repository;
    this.routeRepository = new RouteRepository();
    this.scheduleRepository = new ScheduleRepository();
  }

  /**
   * Search for schedule instances from source to destination.
   * Algorithm:
   * 1. Find all routes that go from source to destination
   * 2. Get all schedules for those routes
   * 3. Find the next occurrence for each unique schedule
   * 4. Sort by earliest occurrence time
   * 5. If < 10 results, add future occurrences to fill up to 10
   */
  public List<Schedule> searchSchedules(String source, String destination, LocalDate date, LocalTime time) {
    try {
      // Step 1: Find all routes that contain a path
      List<Route> allRoutes = routeRepository.getAllRoutes();
      List<Route> validRoutes = findRoutesContainingPath(allRoutes, source, destination);

      if (validRoutes.isEmpty()) {
        System.out.println("[DEBUG] No routes found from " + source + " to " + destination);
        return new ArrayList<>();
      }

      // Step 2: Get all schedules for valid routes
      List<Schedule> allSchedules = scheduleRepository.getAllSchedules();
      List<Schedule> validSchedules = new ArrayList<>();

      for (Schedule schedule : allSchedules) {
        for (Route validRoute : validRoutes) {
          if (schedule.getRoute() != null && schedule.getRoute().getId() == validRoute.getId()) {
            validSchedules.add(schedule);
            break;
          }
        }
      }

      if (validSchedules.isEmpty()) {
        System.out.println("[DEBUG] No schedules found for valid routes");
        return new ArrayList<>();
      }

      // Step 3: For each unique schedule, find its next occurrence
      List<Schedule> firstOccurrences = new ArrayList<>();

      for (Schedule schedule : validSchedules) {
        List<LocalDate> occurrences = findNextOccurrences(schedule, date, time, 1);
        if (!occurrences.isEmpty()) {
          Schedule instance = createScheduleInstance(schedule, occurrences.get(0));
          firstOccurrences.add(instance);
        }
      }

      // Step 4: Sort by date and time (earliest first)
      Collections.sort(firstOccurrences, new Comparator<Schedule>() {
        @Override
        public int compare(Schedule a, Schedule b) {
          int dateCompare = a.getDepartureDate().compareTo(b.getDepartureDate());
          if (dateCompare != 0)
            return dateCompare;
          return a.getDepartureTime().compareTo(b.getDepartureTime());
        }
      });

      // Step 5: If we have < 10 results, add more occurrences
      if (firstOccurrences.size() < 10) {
        List<Schedule> allInstances = new ArrayList<>(firstOccurrences);

        for (Schedule schedule : validSchedules) {
          if (allInstances.size() >= 10)
            break;

          // Find additional occurrences (starting from 2nd occurrence)
          List<LocalDate> occurrences = findNextOccurrences(schedule, date, time, 10);
          for (int i = 1; i < occurrences.size() && allInstances.size() < 10; i++) {
            Schedule instance = createScheduleInstance(schedule, occurrences.get(i));
            allInstances.add(instance);
          }
        }

        // Re-sort after adding more
        Collections.sort(allInstances, new Comparator<Schedule>() {
          @Override
          public int compare(Schedule a, Schedule b) {
            int dateCompare = a.getDepartureDate().compareTo(b.getDepartureDate());
            if (dateCompare != 0)
              return dateCompare;
            return a.getDepartureTime().compareTo(b.getDepartureTime());
          }
        });

        // Limit to 10
        if (allInstances.size() > 10) {
          allInstances = allInstances.subList(0, 10);
        }

        System.out.println(
            "[DEBUG] Found " + allInstances.size() + " schedule instances (" + firstOccurrences.size() + " unique)");
        return allInstances;
      }

      // Limit to 10 unique schedules
      if (firstOccurrences.size() > 10) {
        firstOccurrences = firstOccurrences.subList(0, 10);
      }

      System.out.println("[DEBUG] Found " + firstOccurrences.size() + " unique schedule instances");
      return firstOccurrences;

    } catch (Exception e) {
      System.err.println("Error in searchSchedules: " + e.getMessage());
      e.printStackTrace();
      return new ArrayList<>();
    }
  }

  /**
   * Create a schedule instance with a specific date.
   */
  private Schedule createScheduleInstance(Schedule schedule, LocalDate occurrenceDate) {
    Schedule instance = new Schedule(
        schedule.getId(),
        schedule.getRoute(),
        occurrenceDate,
        schedule.getDepartureTime(),
        schedule.getArrivalTime(),
        schedule.getPrice(),
        schedule.getCapacity());
    instance.setDaysOfWeek(schedule.getDaysOfWeek());
    instance.setSeats(schedule.getSeats());
    return instance;
  }

  /**
   * Find the next occurrences of a schedule starting from the given date.
   * Respects the schedule's days_of_week and time filter.
   */
  private List<LocalDate> findNextOccurrences(Schedule schedule, LocalDate startDate, LocalTime minTime,
      int maxOccurrences) {
    List<LocalDate> occurrences = new ArrayList<>();

    if (schedule.getDaysOfWeek() == null || schedule.getDaysOfWeek().isEmpty()) {
      // If no days specified, assume it runs every day
      for (int i = 0; i < maxOccurrences; i++) {
        LocalDate candidateDate = startDate.plusDays(i);
        if (i == 0 && schedule.getDepartureTime().isBefore(minTime)) {
          continue; // Skip first day if time is before filter
        }
        occurrences.add(candidateDate);
      }
      return occurrences;
    }

    // Find occurrences based on days_of_week
    LocalDate currentDate = startDate;
    int weeksSearched = 0;
    int maxWeeks = 10; // Search up to 10 weeks ahead

    while (occurrences.size() < maxOccurrences && weeksSearched < maxWeeks) {
      java.time.DayOfWeek javaDayOfWeek = currentDate.getDayOfWeek();
      Schedule.DayOfWeek scheduleDayOfWeek = convertToScheduleDayOfWeek(javaDayOfWeek);

      if (schedule.getDaysOfWeek().contains(scheduleDayOfWeek)) {
        // Check time filter only for the first occurrence on or after start date
        if (currentDate.equals(startDate)) {
          if (!schedule.getDepartureTime().isBefore(minTime)) {
            occurrences.add(currentDate);
          }
        } else {
          occurrences.add(currentDate);
        }
      }

      currentDate = currentDate.plusDays(1);
      if (currentDate.getDayOfWeek() == java.time.DayOfWeek.MONDAY && !currentDate.equals(startDate.plusDays(1))) {
        weeksSearched++;
      }
    }

    return occurrences;
  }

  /**
   * Backward compatibility - calls new method with midnight time
   */
  public List<Schedule> searchSchedules(String source, String destination, LocalDate date) {
    return searchSchedules(source, destination, date, LocalTime.of(0, 0));
  }

  /**
   * Find all routes that contain a path from source to destination.
   * A route contains a path if its segments connect source to destination.
   */
  private List<Route> findRoutesContainingPath(List<Route> allRoutes, String source, String destination) {
    List<Route> validRoutes = new ArrayList<>();

    for (Route route : allRoutes) {
      if (routeConnects(route, source, destination)) {
        validRoutes.add(route);
      }
    }

    return validRoutes;
  }

  /**
   * Check if a route connects source to destination via its segments.
   * The route must have segments that go from source (or include source) to
   * destination (or include destination).
   */
  private boolean routeConnects(Route route, String source, String destination) {
    List<RouteSegment> segments = route.getSegments();
    if (segments == null || segments.isEmpty()) {
      return false;
    }

    // Build a map of which stops this route reaches
    Set<String> reachableStops = new HashSet<>();

    for (RouteSegment segment : segments) {
      reachableStops.add(segment.getFromStop().getName().toLowerCase());
      reachableStops.add(segment.getToStop().getName().toLowerCase());
    }

    // Check if both source and destination are in this route
    boolean hasSource = reachableStops.contains(source.toLowerCase());
    boolean hasDestination = reachableStops.contains(destination.toLowerCase());

    if (!hasSource || !hasDestination) {
      return false;
    }

    // Now verify that source comes before destination in the route
    return sourceBeforeDestination(segments, source, destination);
  }

  /**
   * Verify that source appears before destination in the route segments.
   */
  private boolean sourceBeforeDestination(List<RouteSegment> segments, String source, String destination) {
    int sourceIndex = -1;
    int destIndex = -1;

    for (int i = 0; i < segments.size(); i++) {
      RouteSegment segment = segments.get(i);
      String fromStop = segment.getFromStop().getName().toLowerCase();
      String toStop = segment.getToStop().getName().toLowerCase();

      if (sourceIndex == -1 && (fromStop.equals(source.toLowerCase()) || toStop.equals(source.toLowerCase()))) {
        sourceIndex = i;
      }
      if (destIndex == -1 && (fromStop.equals(destination.toLowerCase()) || toStop.equals(destination.toLowerCase()))) {
        destIndex = i;
      }
    }

    return sourceIndex != -1 && destIndex != -1 && sourceIndex <= destIndex;
  }

  /**
   * Check if a schedule is valid for the search criteria.
   */
  private boolean isValidSchedule(Schedule schedule, List<Route> validRoutes, LocalDate date, LocalTime time) {
    // Check if schedule is for one of the valid routes
    boolean routeMatches = false;
    for (Route validRoute : validRoutes) {
      if (schedule.getRoute() != null && schedule.getRoute().getId() == validRoute.getId()) {
        routeMatches = true;
        break;
      }
    }

    if (!routeMatches) {
      return false;
    }

    // Check if schedule runs on this day of the week
    if (schedule.getDaysOfWeek() != null && !schedule.getDaysOfWeek().isEmpty()) {
      java.time.DayOfWeek javaDayOfWeek = date.getDayOfWeek();
      Schedule.DayOfWeek scheduleDayOfWeek = convertToScheduleDayOfWeek(javaDayOfWeek);

      if (!schedule.getDaysOfWeek().contains(scheduleDayOfWeek)) {
        return false; // Schedule doesn't run on this day
      }
    }

    // Check if departure time is at or after the specified time
    if (schedule.getDepartureTime().isBefore(time)) {
      return false;
    }

    return true;
  }

  /**
   * Convert Java DayOfWeek to Schedule.DayOfWeek enum.
   */
  private Schedule.DayOfWeek convertToScheduleDayOfWeek(java.time.DayOfWeek javaDayOfWeek) {
    switch (javaDayOfWeek) {
      case MONDAY:
        return Schedule.DayOfWeek.MONDAY;
      case TUESDAY:
        return Schedule.DayOfWeek.TUESDAY;
      case WEDNESDAY:
        return Schedule.DayOfWeek.WEDNESDAY;
      case THURSDAY:
        return Schedule.DayOfWeek.THURSDAY;
      case FRIDAY:
        return Schedule.DayOfWeek.FRIDAY;
      case SATURDAY:
        return Schedule.DayOfWeek.SATURDAY;
      case SUNDAY:
        return Schedule.DayOfWeek.SUNDAY;
      default:
        return Schedule.DayOfWeek.MONDAY;
    }
  }

  public Schedule getSchedule(int id) {
    return repository.getScheduleById(id);
  }
}
