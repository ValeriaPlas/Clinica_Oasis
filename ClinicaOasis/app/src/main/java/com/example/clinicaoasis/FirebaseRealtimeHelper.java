package com.example.clinicaoasis;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseRealtimeHelper {

    private DatabaseReference usersRef;

    public FirebaseRealtimeHelper() {
        usersRef = FirebaseDatabase.getInstance().getReference("Usuarios");
    }

    public void addUser(String username, String password, String imageName) {
        User user = new User(username, password, imageName);
        usersRef.child(username).setValue(user);
    }

    public static class User {
        public String username;
        public String password;
        public String imageName;

        public User() {}

        public User(String username, String password, String imageName) {
            this.username = username;
            this.password = password;
            this.imageName = imageName;
        }
    }
}
