package org.stepic.droid.storage.repositories.lesson

import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.storage.repositories.Repository
import org.stepik.android.data.lesson.source.LessonRemoteDataSource
import org.stepik.android.model.Lesson
import javax.inject.Inject

class LessonRepositoryImpl
@Inject constructor(
        private val databaseFacade: DatabaseFacade,
        private val lessonRemoteDataSource: LessonRemoteDataSource
) : Repository<Lesson> {

    override fun getObject(key: Long): Lesson? {
        var lesson = databaseFacade.getLessonById(key)
        if (lesson == null) {
            lesson =
                    try {
                        lessonRemoteDataSource.getLessons(key).execute()
                                ?.body()
                                ?.lessons
                                ?.firstOrNull()
                                ?.also(databaseFacade::addLesson)
                    } catch (exception: Exception) {
                        null
                    }
        }
        return lesson
    }

    override fun getObjects(keys: LongArray): Iterable<Lesson> =
           databaseFacade.getLessonsByIds(keys).takeIf { it.size == keys.size } ?:
                   try {
                       lessonRemoteDataSource.getLessons(*keys).execute().body()?.lessons?.also { it.forEach(databaseFacade::addLesson) } ?: emptyList()
                   } catch (_: Exception) {
                       emptyList<Lesson>()
                   }

}

