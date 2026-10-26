package models;

public class Student extends User {
    private String major;
    private int year;

    public Student(String id, String name, String email, String major, int year) {
        super(id, name, email); // Calling the superclass constructor
        this.major = major;
        this.year = year;
    }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    // Demonstrating polymorphism (Method Overriding)
    @Override
    public String getRole() {
        return "Student";
    }

    @Override
    public String toString() {
        return super.toString() + " - " + major + " (Year " + year + ")";
    }
}
