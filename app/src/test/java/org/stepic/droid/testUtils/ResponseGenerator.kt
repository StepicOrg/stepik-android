package org.stepic.droid.testUtils

import org.mockito.Mockito.mock
import org.mockito.stubbing.OngoingStubbing
import retrofit2.Call
import retrofit2.Response
import org.mockito.Mockito.`when` as on

@JvmOverloads
fun <T> useMockInsteadCall(callOngoingStubbing: OngoingStubbing<Call<T>>, responseBodyMock: T, isSuccess: Boolean = true) {
    val call: Call<T> = mock<Call<*>>(Call::class.java) as Call<T>
    val retrofitResponse = mock<Response<*>>(Response::class.java) as Response<T>

    callOngoingStubbing.thenReturn(call)
    on(call.execute()).thenReturn(retrofitResponse)
    on(retrofitResponse.body()).thenReturn(responseBodyMock)
    on(retrofitResponse.isSuccessful).thenReturn(isSuccess)
}