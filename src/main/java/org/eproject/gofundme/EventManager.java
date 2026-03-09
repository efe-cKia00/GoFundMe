package org.eproject.gofundme;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EventManager {

    // Master list of all fundraising events, shared across all ClientHandler threads.
    private final List<FundraisingEvent> events = new ArrayList<>();

    // Creates a new event via the Fundraising class and adds it to the master events list.
    public synchronized void createEvent(String name, double targetAmount, LocalDate deadline) {
        events.add(new FundraisingEvent(name, targetAmount, deadline));
    }

    // Returns all current events (deadline has not passed), sorted, in ascending order, based on each element's (event object) deadline property.
    public synchronized List<FundraisingEvent> getCurrentEvents() {
        List<FundraisingEvent> current = new ArrayList<>();
        for (FundraisingEvent event : events) {
            if (event.isCurrent()) {
                current.add(event);
            }
        }
        current.sort(Comparator.comparing(FundraisingEvent::getDeadline));
        return current;
    }

    // Returns all past events (deadline has passed), sorted, in ascending order, based on each element's (event object) deadline property.
    public synchronized List<FundraisingEvent> getPastEvents() {
        List<FundraisingEvent> past = new ArrayList<>();
        for (FundraisingEvent event : events) {
            if (!event.isCurrent()) {
                past.add(event);
            }
        }
        past.sort(Comparator.comparing(FundraisingEvent::getDeadline));
        return past;
    }

    // Donates to a current event by its index in the current events list.
    // Returns a result message to be relayed back to the client.
    public synchronized String donate(int index, double amount) {
        List<FundraisingEvent> current = getCurrentEvents();
        if (index < 0 || index >= current.size()) {
            return "ERROR: Invalid event index.";
        }
        if (amount <= 0) {
            return "ERROR: Donation amount must be greater than zero.";
        }
        FundraisingEvent event = current.get(index);
        event.donate(amount);
        return String.format("SUCCESS: $%.2f donated to \"%s\". Total raised: $%.2f",
                amount, event.getName(), event.getRaisedAmount());
    }
}
