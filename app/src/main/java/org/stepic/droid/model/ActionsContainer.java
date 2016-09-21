package org.stepic.droid.model;

import android.os.Parcel;
import android.os.Parcelable;

//// TODO: 10.08.16 merge to class "Actions"
public class ActionsContainer implements Parcelable {
    private String do_review;
    private String edit_instructions;

    public String getDo_review() {
        return do_review;
    }

    public String getEdit_instructions() {
        return edit_instructions;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.do_review);
        dest.writeString(this.edit_instructions);
    }

    public ActionsContainer() {
    }

    protected ActionsContainer(Parcel in) {
        this.do_review = in.readString();
        this.edit_instructions = in.readString();
    }

    public static final Parcelable.Creator<ActionsContainer> CREATOR = new Parcelable.Creator<ActionsContainer>() {
        public ActionsContainer createFromParcel(Parcel source) {
            return new ActionsContainer(source);
        }

        public ActionsContainer[] newArray(int size) {
            return new ActionsContainer[size];
        }
    };

    public void setDo_review(String do_review) {
        this.do_review = do_review;
    }
}
