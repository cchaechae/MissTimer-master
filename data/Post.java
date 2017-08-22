package hu.ait.missbeauty.data;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ChenChen on 5/20/17.
 */

public class Post {
    private String uid;
    private String author;
    private String name; //title
    private String caption; //body
    private String imageUrl;
    private String key;

    public Post(){
    }

    public Post(String uid, String author, String name, String caption) {
        this.uid = uid;
        this.author = author;
        this.name = name;
        this.caption = caption;
    }


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Exclude
    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("name", name);
        result.put("caption", caption);
        result.put("imageUrl", imageUrl);

        return result;
    }
}
