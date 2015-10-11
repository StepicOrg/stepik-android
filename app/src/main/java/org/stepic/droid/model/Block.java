package org.stepic.droid.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Block implements Parcelable, Serializable {
    private String name;
    private String text;
    private Video video;
    //more fields look at stepic.org/api/steps/14671

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }

    public Video getVideo() {
        return video;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.text);
        dest.writeParcelable(this.video, flags);
    }

    public Block() {
    }

    protected Block(Parcel in) {
        this.name = in.readString();
        this.text = in.readString();
        this.video = in.readParcelable(Video.class.getClassLoader());
    }

    public static final Creator<Block> CREATOR = new Creator<Block>() {
        public Block createFromParcel(Parcel source) {
            return new Block(source);
        }

        public Block[] newArray(int size) {
            return new Block[size];
        }
    };
}
