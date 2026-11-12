package org.example;

public class Service {
    private String serviceId;
    private String type; // "flight" or "train"
    private String company;
    private String departure;
    private String arrival;
    private String departureDate;
    private String arrivalDate;
    private double minPrice;
    private String saleDateDeadline;

    public Service(String serviceId, String type, String company,
                   String departure, String arrival,
                   String departureDate, String arrivalDate,
                   double minPrice, String saleDateDeadline) {
        this.serviceId = serviceId;
        this.type = type;
        this.company = company;
        this.departure = departure;
        this.arrival = arrival;
        this.departureDate = departureDate;
        this.arrivalDate = arrivalDate;
        this.minPrice = minPrice;
        this.saleDateDeadline = saleDateDeadline;
    }

    public String getServiceId() { return serviceId; }
    public String getType() { return type; }
    public String getCompany() { return company; }
    public String getDeparture() { return departure; }
    public String getArrival() { return arrival; }
    public String getDepartureDate() { return departureDate; }
    public String getArrivalDate() { return arrivalDate; }
    public double getMinPrice() { return minPrice; }
    public String getSaleDateDeadline() { return saleDateDeadline; }

    @Override
    public String toString() {
        return String.format("[%s] %s %s->%s (%s) | min: EUR %.2f | deadline: %s",
                type, company, departure, arrival, departureDate, minPrice, saleDateDeadline);
    }
}
