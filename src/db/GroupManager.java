package db;

import models.StudyGroup;
import models.Student;
import java.util.List;
import java.util.Map;

public interface GroupManager {
    boolean addStudent(String id, String name, String email, String major, int year);
    boolean createStudyGroup(String name, String course, int capacity);
    boolean deleteStudyGroup(int id);
    boolean joinGroup(String studentId, int groupId);
    List<StudyGroup> getAllGroups();
    List<Student> getAllStudents();
    List<Student> getMembersOfGroup(int groupId);
    List<StudyGroup> getGroupsForStudent(String studentId);
    int getMemberCount(int groupId);
    Map<String, Object> getSystemStatistics();
    
    // File Handling & Data Seeding
    boolean exportReportToFile(String filePath);
    void seedSampleData();
}
