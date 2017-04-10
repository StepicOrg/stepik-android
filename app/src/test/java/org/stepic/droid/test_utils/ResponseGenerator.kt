package org.stepic.droid.test_utils

import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.stubbing.OngoingStubbing
import retrofit2.Call
import retrofit2.Response

@JvmOverloads
fun <T> useMockInsteadCall(callOngoingStubbing: OngoingStubbing<Call<T>>, responseBodyMock: T, isSuccess: Boolean = true) {
    val call: Call<T> = mock<Call<*>>(Call::class.java) as Call<T>
    val retrofitResponse = mock<Response<*>>(Response::class.java) as Response<T>

    callOngoingStubbing.thenReturn(call)
    Mockito.`when`(call.execute()).thenReturn(retrofitResponse)
    Mockito.`when`(retrofitResponse.body()).thenReturn(responseBodyMock)
    Mockito.`when`(retrofitResponse.isSuccessful).thenReturn(isSuccess)
}