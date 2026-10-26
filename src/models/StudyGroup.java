package models;

import java.util.ArrayList;
import java.util.List;

public class StudyGroup {
    private int groupId;
    private String groupName;
    private Course course;
    private int maxCapacity;
    private List<Student> members;

    public StudyGroup(int groupId, String groupName, Course course, int maxCapacity) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.course = course;
        this.maxCapacity = maxCapacity;
        this.members = new ArrayList<>(); // Using ArrayList as required
    }

    public int getGroupId() { return groupId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }

    public List<Student> getMembers() { return members; }

    public boolean addMember(Student student) {
        if (members.size() < maxCapacity && !members.contains(student)) {
            members.add(student);
            return true;
        }
        return false;
    }

    public boolean removeMember(Student student) {
        return members.remove(student);
    }
    
    public int getAvailableSeats() {
        return maxCapacity - members.size();
    }

    @Override
    public String toString() {
        return groupName + " (" + course.getCourseCode() + ") - Seats: " + getAvailableSeats() + "/" + maxCapacity;
    }
}
