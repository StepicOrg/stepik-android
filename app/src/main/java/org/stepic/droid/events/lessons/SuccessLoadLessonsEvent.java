package org.stepic.droid.events.lessons;

import org.stepic.droid.model.Section;
import org.stepic.droid.model.Unit;
import org.stepic.droid.web.LessonStepicResponse;

import java.util.List;

import retrofit.Response;
import retrofit.Retrofit;

public class SuccessLoadLessonsEvent {
    Section mSection;
    private final Response<LessonStepicResponse> response;
    private final Retrofit retrofit;
    private List<Unit> units;

    public SuccessLoadLessonsEvent(Section section, Response<LessonStepicResponse> response, Retrofit retrofit, List<Unit> units) {
        mSection = section;
        this.response = response;
        this.retrofit = retrofit;
        this.units = units;
    }

    public List<Unit> getUnits() {
        return units;
    }

    public Section getSection() {
        return mSection;
    }

    public Response<LessonStepicResponse> getResponse() {
        return response;
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }
}
