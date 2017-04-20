package org.stepic.droid.web;

import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface StepikDeskEmptyAuthService {

    @Multipart
    @POST("/en/support/tickets")
    Call<Void> sendFeedback(@Part(value = "helpdesk_ticket[subject]") String requestSubject,
                            @Part(value = "helpdesk_ticket[email]") String email,
                            @Part(value = "meta[user_agent]") String systemInfo,
                            @Part(value = "helpdesk_ticket[ticket_body_attributes][description_html]") String description);
}
