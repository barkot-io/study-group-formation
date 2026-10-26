package models;

public class Admin extends User {
    
    public Admin(String id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public String getRole() {
        return "Admin";
    }

    @Override
    public String toString() {
        return super.toString() + " - System Administrator";
    }
}
