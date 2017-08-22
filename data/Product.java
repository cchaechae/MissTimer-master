package hu.ait.missbeauty.data;

/**
 * Created by ChenChen on 5/19/17.
 */

public class Product {

    private String uid;
    private String name;
    private String expDate;
    private String opnDate;
    private boolean openStatus;
    private String memo;
    private String imageURL;
    private int isExpired;
    private String spinnerAutoDate;



    public Product(){

    }

    public Product(String uid, String name, String expDate, String opnDate, boolean openStatus,
                   String memo, int isExpired,String spinnerAutoDate){

        this.uid = uid;
        this.name = name;
        this.expDate = expDate;
        this.opnDate = opnDate;
        this.openStatus = openStatus;
        this.memo = memo;
        this.isExpired = isExpired;
        this.spinnerAutoDate = spinnerAutoDate;


    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public String getOpnDate() {
        return opnDate;
    }

    public void setOpnDate(String opnDate) {
        this.opnDate = opnDate;
    }

    public boolean isOpenStatus() {
        return openStatus;
    }

    public void setOpenStatus(boolean openStatus) {
        this.openStatus = openStatus;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public int getIsExpired() {
        return isExpired;
    }

    public void setIsExpired(int isExpired) {
        this.isExpired = isExpired;
    }
    public String getSpinnerAutoDate() {
        return spinnerAutoDate;
    }

    public void setSpinnerAutoDate(String spinnerAutoDate) {
        this.spinnerAutoDate = spinnerAutoDate;
    }
}
