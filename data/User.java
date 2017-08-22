package hu.ait.missbeauty.data;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chaelimseo on 5/20/17.
 */


// [START blog_user_class]
@IgnoreExtraProperties
public class User {

    public String username;
    public String email;
    public String description;
    public HashMap<String, Post> myPostList; //key, Post
    public Map<String, Product> myFavorite; //key, Favorite List

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    @Exclude
    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("email", email);
        result.put("description", description);
        result.put("favorites", myFavorite);

        return result;
    }
}
// [END blog_user_class]