package gitlet;

// TODO: any imports you need here

import java.text.SimpleDateFormat;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.ArrayList;
import java.io.Serializable;
import java.util.TreeMap;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author EnzoGuang
 */
public class Commit implements Serializable, Dumpable {
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
    private TreeMap<String, String> fileBlob = new TreeMap<>();
    private ArrayList<String> parentId = new ArrayList<>();
    private String commitId;

    public Commit(String message) {
        this.message = message;
        Date date = new Date(0);
        SimpleDateFormat format = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy Z");
        this.timestamp = format.format(date);
        this.commitId = Utils.sha1(this.toString());
    }

    public Commit(String message, String parentId, TreeMap<String, String> file) {
        this.message = message;
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy Z");
        this.timestamp = format.format(date);
        this.fileBlob = file;
        this.parentId.add(this.parentId.size(), parentId);
        this.commitId = Utils.sha1(this.toString());
    }
    /* TODO: fill in the rest of this class. */

    @Override
    public String toString() {
        return message + timestamp + fileBlob + parentId;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public TreeMap<String, String> getFileBlob() {
        return fileBlob;
    }

    public ArrayList<String> getParentId() {
        return parentId;
    }

    public String getCommitId() {
        return commitId;
    }

    @Override
    public void dump() {
        System.out.println("\n-----------");
        System.out.println("message: " + message + "\n" + "timestamp: " + timestamp
                + "\n" + "fileBlob: " + fileBlob + "\n" + "parentId: " + parentId
                + "\n" + "commitId: " + commitId);
    }
}
