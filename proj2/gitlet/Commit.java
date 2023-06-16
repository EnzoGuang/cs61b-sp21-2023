package gitlet;

// TODO: any imports you need here

import java.util.Date; // TODO: You'll likely use this in this class
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.io.Serializable;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author EnzoGuang
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private final String message;
    private String timestamp;
    private LinkedHashMap<String, String> fileBlob = new LinkedHashMap<>();
    private ArrayList<String> parentId = new ArrayList<>();
    private String commitId;

    public Commit(String message) {
        this.message = message;
        this.timestamp = new Date(0).toString();
        this.commitId = Utils.sha1(this.toString());
    }

    public Commit(String message, String parentId) {
        this.message = message;
        this.timestamp = new Date().toString();
        this.parentId.add(this.parentId.size(), parentId);
        this.commitId = Utils.sha1(this.toString());
    }
    /* TODO: fill in the rest of this class. */

    @Override
    public String toString() {
        return message + timestamp + fileBlob + parentId ;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public LinkedHashMap<String, String> getFileBlob() {
        return fileBlob;
    }

    public ArrayList<String> getParentId() {
        return parentId;
    }

    public String getCommitId() {
        return commitId;
    }
}
