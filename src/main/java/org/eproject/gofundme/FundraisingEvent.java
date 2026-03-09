package org.eproject.gofundme;

import java.time.LocalDate;

public class FundraisingEvent {

    //Initialized all class fields as private.
    private final String name;
    private final double targetAmount;
    private double raisedAmount;
    private final LocalDate deadline;

    //Constructor that creates a new event.
    public FundraisingEvent(String name, double targetAmount, LocalDate deadline) {
        this.name = name;
        this.targetAmount = targetAmount;
        this.raisedAmount = 0.0;
        this.deadline = deadline;
    }

    //Field getters and methods.
    public String getName() {
        return name;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public double getRaisedAmount() {
        return raisedAmount;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    //returns true if today is on or before the deadline
    public boolean isCurrent() {
        return !LocalDate.now().isAfter(deadline);
    }

    public void donate(double amount) {
        raisedAmount += amount;
    }

    //Overrides the toString() method by the String class supertype. This gives a Human-readable summary for listing events.
    @Override
    public String toString() {
        return String.format("%-30s | Target: $%10.2f | Raised: $%10.2f | Deadline: %s",
                name, targetAmount, raisedAmount, deadline);
    }
}
