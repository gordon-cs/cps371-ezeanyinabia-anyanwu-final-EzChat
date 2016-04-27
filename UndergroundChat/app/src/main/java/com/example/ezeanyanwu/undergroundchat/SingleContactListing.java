package com.example.ezeanyanwu.undergroundchat;

import android.util.Log;

import java.util.Objects;

/**
 * Created by ezeanyanwu on 27/04/2016.
 */
public class SingleContactListing
{
    public String contactName;
    public String presence;
    public SingleContactListing(String contactName, String presence)
    {
        this.contactName = contactName;
        this.presence = presence;
    }
    @Override
    public int hashCode()
    {
        int result = 17;
        int c = contactName.hashCode();
        result = 37 * result + c;
        return result;
    }
    @Override
    public boolean equals(Object other){
        if (other == null)
        {
            Log.d("HERE1:", "YOOO");
            return false;
        }
        if (other == this)
        {
            Log.d("HERE2:", "YOOO");
            return true;
        }
        if (!(other instanceof SingleContactListing))
        {
            Log.d("HERE3:", "YOOO");
            return false;
        }
        SingleContactListing otherMyClass = (SingleContactListing) other;

        if(this.contactName.equals(otherMyClass.contactName))
        {
            Log.d("HERE4:", "YOOO");
            return true;
        }
        else
        {
            Log.d("HERE5:", this.contactName + " " + otherMyClass.contactName);
            return false;
        }
    }
}