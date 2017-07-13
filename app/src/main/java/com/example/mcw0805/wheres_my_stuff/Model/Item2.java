package com.example.mcw0805.wheres_my_stuff.Model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.DatabaseReference;

import java.util.Date;


public class Item2 implements Parcelable {
    protected String name;
    protected String description;
    protected long date;
    protected double longitude;
    protected double latitude;
    protected ItemCategory category;
    protected String uid;
    protected boolean isOpen;

    public Item2() {}

    public Item2(String name, String description, long date, double longitude,
                 double latitude, ItemCategory category, String uid, boolean isOpen) {
        this.name = name;
        this.description = description;
        this.date = date;
        this.longitude = longitude;
        this.latitude = latitude;
        this.category = category;
        this.uid = uid;
        this.isOpen = isOpen;
    }

    public Item2(String name, String description, long date, double longitude,
                 double latitude, ItemCategory category, String uid) {
        this.name = name;
        this.description = description;
        this.date = date;
        this.longitude = longitude;
        this.latitude = latitude;
        this.category = category;
        this.uid = uid;
        this.isOpen = true;
    }

    protected Item2(Parcel in) {
        name = in.readString();
        description = in.readString();
        date = in.readLong();
        latitude = in.readDouble();
        longitude = in.readDouble();
        category = ItemCategory.valueOf(in.readString().toString());
        uid = in.readString();
        isOpen = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(description);
        dest.writeLong(date);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(category.getType());
        dest.writeString(uid);
        dest.writeByte((byte) (isOpen ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Item2> CREATOR = new Creator<Item2>() {
        @Override
        public Item2 createFromParcel(Parcel in) {
            return new Item2(in);
        }

        @Override
        public Item2[] newArray(int size) {
            return new Item2[size];
        }
    };

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean getIsOpen()
    {
        return isOpen;
    }

    public void setIsOpen(boolean open) {
        this.isOpen = open;
    }

    public String getName() {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public ItemCategory getCategory() {
        return category;
    }

    public void setCategory(ItemCategory category) {
        this.category = category;
    }

    public String getStatusString() {
        return isOpen ? "open" : "closed";
    }

    public void writeToDatabase(DatabaseReference childRef) {

        DatabaseReference dateChild = childRef.child("date");
        dateChild.setValue(getDate());

        DatabaseReference nameChild = childRef.child("name");
        nameChild.setValue(getName());

        DatabaseReference descriptionChild = childRef.child("description");
        descriptionChild.setValue(getDescription());

        DatabaseReference latitudeChild = childRef.child("latitude");
        latitudeChild.setValue(getLatitude());

        DatabaseReference longitudeChild = childRef.child("longitude");
        longitudeChild.setValue(getLongitude());

        DatabaseReference categoryChild = childRef.child("category");
        categoryChild.setValue(getCategory());

        DatabaseReference uidChild = childRef.child("uid");
        uidChild.setValue(getUid());

        DatabaseReference isOpenChild = childRef.child("isOpen");
        isOpenChild.setValue(getIsOpen());
        //childRef.setValue(this);

    }


}
