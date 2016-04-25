package org.stepic.droid.web


import retrofit.Call
import retrofit.http.GET

interface CommonService {
    @GET
    fun updatingInfo(): Call<UpdateResponse>
}
