package org.stepic.droid.code.highlight

import org.stepic.droid.code.highlight.prettify.PrettifyParser
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.di.AppSingleton
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

@AppSingleton
class ParserContainer
@Inject
constructor(threadPoolExecutor: ThreadPoolExecutor,
            mainHandler: MainHandler) {

    var prettifyParser: PrettifyParser? = null
        private set

    init {
        threadPoolExecutor.execute {
            val parser = PrettifyParser()
            mainHandler.post {
                prettifyParser = parser
            }
        }
    }

}
