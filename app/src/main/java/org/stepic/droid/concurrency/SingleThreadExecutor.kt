package org.stepic.droid.concurrency

import java.util.concurrent.ExecutorService

class SingleThreadExecutor(private val singleExecutor: ExecutorService) : ExecutorService by singleExecutor
