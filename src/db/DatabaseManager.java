package db;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Course;
import models.Student;
import models.StudyGroup;

public class DatabaseManager implements GroupManager {
    
    private static final String URL = "jdbc:sqlite:study_group.db";
    private static DatabaseManager instance = null;

    private DatabaseManager() {}

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
        return conn;
    }

    public static void initializeDatabase() {
        String createGroupsTable = "CREATE TABLE IF NOT EXISTS study_groups ("
                + " id integer PRIMARY KEY AUTOINCREMENT,"
                + " name text NOT NULL,"
                + " course text NOT NULL,"
                + " capacity integer NOT NULL"
                + ");";

        String createStudentsTable = "CREATE TABLE IF NOT EXISTS students ("
                + " id text PRIMARY KEY,"
                + " name text NOT NULL,"
                + " email text NOT NULL,"
                + " major text,"
                + " year integer,"
                + " group_id integer,"
                + " FOREIGN KEY(group_id) REFERENCES study_groups(id)"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createGroupsTable);
            stmt.execute(createStudentsTable);
        } catch (SQLException e) {
            System.out.println("Error init DB: " + e.getMessage());
        }

        // Auto-seed sample data on first launch if empty
        getInstance().seedSampleData();
    }

    @Override
    public void seedSampleData() {
        // If database is empty, insert realistic sample groups and students
        if (getAllGroups().isEmpty()) {
            System.out.println("Seeding initial sample data...");
            createStudyGroup("Algorithms Masterclass", "CS101", 5);
            createStudyGroup("Database System Design", "CS202", 4);
            createStudyGroup("Java Advanced OOP", "SWE301", 3);
            createStudyGroup("Web UI/UX Enthusiasts", "SWE105", 6);

            addStudent("S101", "Alex Smith", "alex@university.edu", "Computer Science", 2);
            addStudent("S102", "Sarah Johnson", "sarah@university.edu", "Software Eng", 3);
            addStudent("S103", "David Lee", "david@university.edu", "Computer Science", 1);
            addStudent("S104", "Emily Davis", "emily@university.edu", "Information Tech", 4);
            addStudent("S105", "Michael Brown", "michael@university.edu", "Software Eng", 2);

            // Assign sample students to groups
            List<StudyGroup> groups = getAllGroups();
            if (!groups.isEmpty()) {
                joinGroup("S101", groups.get(0).getGroupId());
                joinGroup("S102", groups.get(0).getGroupId());
                joinGroup("S103", groups.get(1).getGroupId());
                joinGroup("S104", groups.get(2).getGroupId());
            }
        }
    }

    @Override
    public boolean exportReportToFile(String filePath) {
        List<StudyGroup> groups = getAllGroups();
        List<Student> students = getAllStudents();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("====================================================\n");
            writer.write("    STUDY GROUP FORMATION SYSTEM - SUMMARY REPORT    \n");
            writer.write("====================================================\n\n");

            writer.write("SYSTEM OVERVIEW:\n");
            writer.write(" - Total Active Groups: " + groups.size() + "\n");
            writer.write(" - Total Registered Students: " + students.size() + "\n\n");

            writer.write("----------------------------------------------------\n");
            writer.write("STUDY GROUPS DIRECTORY & ROSTERS:\n");
            writer.write("----------------------------------------------------\n\n");

            for (StudyGroup g : groups) {
                writer.write("Group ID    : " + g.getGroupId() + "\n");
                writer.write("Group Name  : " + g.getGroupName() + "\n");
                writer.write("Course Code : " + g.getCourse().getCourseCode() + "\n");
                
                List<Student> members = getMembersOfGroup(g.getGroupId());
                writer.write("Capacity    : " + members.size() + " / " + g.getMaxCapacity() + " Members\n");
                writer.write("Member Roster:\n");

                if (members.isEmpty()) {
                    writer.write("   (No members enrolled yet)\n");
                } else {
                    for (Student m : members) {
                        writer.write("   - [" + m.getId() + "] " + m.getName() + " (" + m.getMajor() + ", Year " + m.getYear() + ") - " + m.getEmail() + "\n");
                    }
                }
                writer.write("\n----------------------------------------------------\n\n");
            }

            writer.write("End of Report.\n");
            return true;
        } catch (IOException e) {
            System.out.println("File export error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean addStudent(String id, String name, String email, String major, int year) {
        if (id == null || name == null || id.trim().isEmpty() || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID and Name cannot be empty.");
        }
        String sql = "INSERT INTO students(id, name, email, major, year) VALUES(?, ?, ?, ?, ?)";
        try (Connection conn = connect();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, name);
            pstmt.setString(3, email);
            pstmt.setString(4, major);
            pstmt.setInt(5, year);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Error adding student: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean createStudyGroup(String name, String course, int capacity) {
        if (name == null || course == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Group Name and Course Code cannot be empty.");
        }
        String sql = "INSERT INTO study_groups(name, course, capacity) VALUES(?, ?, ?)";
        try (Connection conn = connect();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, course);
            pstmt.setInt(3, capacity);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Error creating group: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteStudyGroup(int id) {
        String updateStudents = "UPDATE students SET group_id = NULL WHERE group_id = ?";
        String deleteGroup = "DELETE FROM study_groups WHERE id = ?";
        try (Connection conn = connect();
             java.sql.PreparedStatement pstmt1 = conn.prepareStatement(updateStudents);
             java.sql.PreparedStatement pstmt2 = conn.prepareStatement(deleteGroup)) {
            pstmt1.setInt(1, id);
            pstmt1.executeUpdate();
            
            pstmt2.setInt(1, id);
            return pstmt2.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting group: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean joinGroup(String studentId, int groupId) {
        if (studentId == null || studentId.trim().isEmpty()) return false;

        int currentMembers = getMemberCount(groupId);
        int capacity = getGroupCapacity(groupId);
        if (currentMembers >= capacity && capacity > 0) {
            System.out.println("Group is full. Cannot join.");
            return false;
        }
        
        String sql = "UPDATE students SET group_id = ? WHERE id = ?";
        try (Connection conn = connect();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, groupId);
            pstmt.setString(2, studentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error joining group: " + e.getMessage());
            return false;
        }
    }

    private int getGroupCapacity(int groupId) {
        String sql = "SELECT capacity FROM study_groups WHERE id = ?";
        try (Connection conn = connect();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, groupId);
            java.sql.ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("capacity");
            }
        } catch (SQLException e) {
            System.out.println("Error fetching capacity: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public int getMemberCount(int groupId) {
        String sql = "SELECT COUNT(*) FROM students WHERE group_id = ?";
        try (Connection conn = connect();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, groupId);
            java.sql.ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Error counting members: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public List<Student> getMembersOfGroup(int groupId) {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students WHERE group_id = ?";
        try (Connection conn = connect();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, groupId);
            java.sql.ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Student(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("major"),
                    rs.getInt("year")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching group members: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<StudyGroup> getGroupsForStudent(String studentId) {
        List<StudyGroup> list = new ArrayList<>();
        String sql = "SELECT g.* FROM study_groups g JOIN students s ON g.id = s.group_id WHERE s.id = ?";
        try (Connection conn = connect();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            java.sql.ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Course c = new Course(rs.getString("course"), rs.getString("course"));
                list.add(new StudyGroup(
                    rs.getInt("id"),
                    rs.getString("name"),
                    c,
                    rs.getInt("capacity")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching student groups: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<StudyGroup> getAllGroups() {
        List<StudyGroup> list = new ArrayList<>();
        String sql = "SELECT * FROM study_groups";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Course c = new Course(rs.getString("course"), rs.getString("course"));
                StudyGroup g = new StudyGroup(
                    rs.getInt("id"),
                    rs.getString("name"),
                    c,
                    rs.getInt("capacity")
                );
                
                List<Student> members = getMembersOfGroup(g.getGroupId());
                for (Student m : members) {
                    g.addMember(m);
                }
                list.add(g);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching groups: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Student s = new Student(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("major"),
                    rs.getInt("year")
                );
                list.add(s);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching students: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Map<String, Object> getSystemStatistics() {
        Map<String, Object> stats = new HashMap<>();
        String sqlGroups = "SELECT COUNT(*), AVG(capacity) FROM study_groups";
        String sqlStudents = "SELECT COUNT(*) FROM students";
        String sqlPopularCourse = "SELECT course, COUNT(*) as cnt FROM study_groups GROUP BY course ORDER BY cnt DESC LIMIT 1";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            
            java.sql.ResultSet rsG = stmt.executeQuery(sqlGroups);
            if (rsG.next()) {
                stats.put("totalGroups", rsG.getInt(1));
                stats.put("avgCapacity", Math.round(rsG.getDouble(2) * 10.0) / 10.0);
            }
            
            java.sql.ResultSet rsS = stmt.executeQuery(sqlStudents);
            if (rsS.next()) {
                stats.put("totalStudents", rsS.getInt(1));
            }

            java.sql.ResultSet rsP = stmt.executeQuery(sqlPopularCourse);
            if (rsP.next()) {
                stats.put("popularCourse", rsP.getString("course"));
            } else {
                stats.put("popularCourse", "None");
            }
        } catch (SQLException e) {
            System.out.println("Error fetching stats: " + e.getMessage());
        }
        return stats;
    }
}
