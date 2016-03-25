package org.stepic.droid.events.joining_course

import retrofit.Response

class FailJoinEvent(val response: Response<Void>? = null)