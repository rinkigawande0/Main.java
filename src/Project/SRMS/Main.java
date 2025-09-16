package Project.SRMS;

// Main.java
import java.util.*;
import java.io.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        StudentManager sm = new StudentManager();
        // try loading from default csv (optional)
        try { sm.loadFromCSV("students.csv"); System.out.println("Loaded students.csv"); } catch(Exception e) { /* ignore if not present */ }

        while (true) {
            System.out.println("\n--- Student Management System ---");
            System.out.println("1. Add Student");
            System.out.println("2. View Student by ID");
            System.out.println("3. Update Student");
            System.out.println("4. Delete Student");
            System.out.println("5. List All Students");
            System.out.println("6. Search by Name");
            System.out.println("7. Sort by CGPA (desc)");
            System.out.println("8. Save to CSV");
            System.out.println("0. Exit");
            System.out.print("Choice: ");
            String ch = sc.nextLine().trim();
            try {
                switch (ch) {
                    case "1": addInteractive(sc, sm); break;
                    case "2": viewById(sc, sm); break;
                    case "3": updateInteractive(sc, sm); break;
                    case "4": deleteInteractive(sc, sm); break;
                    case "5": listAll(sm); break;
                    case "6": searchByName(sc, sm); break;
                    case "7": sm.sortByCGPADesc(); System.out.println("Sorted by CGPA (desc)."); break;
                    case "8":
                        sm.saveToCSV("students.csv");
                        System.out.println("Saved to students.csv");
                        break;
                    case "0":
                        sm.saveToCSV("students.csv");
                        System.out.println("Saved and exiting.");
                        sc.close();
                        return;
                    default: System.out.println("Invalid choice.");
                }
            } catch(Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    static void addInteractive(Scanner sc, StudentManager sm) {
        System.out.print("Enter ID: "); String id = sc.nextLine().trim();
        System.out.print("Name: "); String name = sc.nextLine().trim();
        System.out.print("Age: "); int age = Integer.parseInt(sc.nextLine().trim());
        System.out.print("Course: "); String course = sc.nextLine().trim();
        System.out.print("CGPA: "); double cgpa = Double.parseDouble(sc.nextLine().trim());
        Student s = new Student(id, name, age, course, cgpa);
        boolean ok = sm.addStudent(s);
        System.out.println(ok ? "Student added." : "Student with this ID already exists.");
    }

    static void viewById(Scanner sc, StudentManager sm) {
        System.out.print("Enter ID: "); String id = sc.nextLine().trim();
        Student s = sm.getStudentById(id);
        System.out.println(s == null ? "Not found." : s);
    }

    static void updateInteractive(Scanner sc, StudentManager sm) {
        System.out.print("Enter ID to update: "); String id = sc.nextLine().trim();
        Student old = sm.getStudentById(id);
        if (old == null) { System.out.println("No such student."); return; }
        System.out.println("Leave blank to keep old value.");
        System.out.print("Name ("+old.getName()+"): "); String name = sc.nextLine().trim();
        System.out.print("Age ("+old.getAge()+"): "); String ageS = sc.nextLine().trim();
        System.out.print("Course ("+old.getCourse()+"): "); String course = sc.nextLine().trim();
        System.out.print("CGPA ("+old.getCgpa()+"): "); String cgpaS = sc.nextLine().trim();

        String newName = name.isEmpty() ? old.getName() : name;
        int newAge = ageS.isEmpty() ? old.getAge() : Integer.parseInt(ageS);
        String newCourse = course.isEmpty() ? old.getCourse() : course;
        double newCgpa = cgpaS.isEmpty() ? old.getCgpa() : Double.parseDouble(cgpaS);

        Student updated = new Student(id, newName, newAge, newCourse, newCgpa);
        sm.updateStudent(id, updated);
        System.out.println("Updated.");
    }

    static void deleteInteractive(Scanner sc, StudentManager sm) {
        System.out.print("Enter ID to delete: "); String id = sc.nextLine().trim();
        boolean ok = sm.deleteStudent(id);
        System.out.println(ok ? "Deleted." : "Not found.");
    }

    static void listAll(StudentManager sm) {
        List<Student> all = sm.listAll();
        if (all.isEmpty()) { System.out.println("No students."); return; }
        for (Student s : all) System.out.println(s);
    }

    static void searchByName(Scanner sc, StudentManager sm) {
        System.out.print("Enter name keyword: "); String q = sc.nextLine().trim();
        List<Student> res = sm.searchByName(q);
        if (res.isEmpty()) System.out.println("No matches.");
        else for (Student s : res) System.out.println(s);
    }
}

class Student {
    private String id;
    private String name;
    private int age;
    private String course;
    private double cgpa;

    public Student(String id, String name, int age, String course, double cgpa) {
        this.id = id; this.name = name; this.age = age; this.course = course; this.cgpa = cgpa;
    }
    public String getId(){ return id; }
    public String getName(){ return name; }
    public int getAge(){ return age; }
    public String getCourse(){ return course; }
    public double getCgpa(){ return cgpa; }

    public String toCSV(){ return String.join(",", id, escape(name), String.valueOf(age), escape(course), String.valueOf(cgpa)); }
    private static String escape(String s){ return s.replace(",", " "); }

    public static Student fromCSV(String line) {
        String[] p = line.split(",", 5);
        return new Student(p[0], p[1], Integer.parseInt(p[2]), p[3], Double.parseDouble(p[4]));
    }

    @Override public String toString() {
        return String.format("ID:%s | Name:%s | Age:%d | Course:%s | CGPA:%.2f", id, name, age, course, cgpa);
    }
}

class StudentManager {
    private Map<String, Student> studentMap = new HashMap<>();
    private List<Student> studentList = new ArrayList<>();

    public boolean addStudent(Student s) {
        if (studentMap.containsKey(s.getId())) return false;
        studentMap.put(s.getId(), s);
        studentList.add(s);
        return true;
    }
    public Student getStudentById(String id) { return studentMap.get(id); }

    public boolean updateStudent(String id, Student updated) {
        if (!studentMap.containsKey(id)) return false;
        studentMap.put(id, updated);
        for (int i = 0; i < studentList.size(); i++) {
            if (studentList.get(i).getId().equals(id)) { studentList.set(i, updated); break; }
        }
        return true;
    }

    public boolean deleteStudent(String id) {
        if (!studentMap.containsKey(id)) return false;
        studentMap.remove(id);
        studentList.removeIf(s -> s.getId().equals(id));
        return true;
    }

    public List<Student> listAll() { return new ArrayList<>(studentList); }

    public List<Student> searchByName(String q) {
        q = q.toLowerCase();
        List<Student> r = new ArrayList<>();
        for (Student s : studentList) if (s.getName().toLowerCase().contains(q)) r.add(s);
        return r;
    }

    public void sortByCGPADesc() {
        studentList.sort((a,b) -> Double.compare(b.getCgpa(), a.getCgpa()));
    }

    public void saveToCSV(String filename) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            for (Student s : studentList) bw.write(s.toCSV() + "\n");
        }
    }

    public void loadFromCSV(String filename) throws IOException {
        studentMap.clear(); studentList.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Student s = Student.fromCSV(line);
                studentMap.put(s.getId(), s);
                studentList.add(s);
            }
        }
    }
}
