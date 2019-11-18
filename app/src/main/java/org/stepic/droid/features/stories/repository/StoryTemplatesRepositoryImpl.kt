package org.stepic.droid.features.stories.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.features.stories.model.ViewedStoryTemplate
import org.stepic.droid.storage.dao.IDao
import org.stepik.android.data.stories.source.StoryTemplatesRemoteDataSource
import org.stepik.android.model.StoryTemplate
import javax.inject.Inject

@AppSingleton
class StoryTemplatesRepositoryImpl
@Inject
constructor(
    private val storyTemplatesRemoteRemoteDataSource: StoryTemplatesRemoteDataSource,
    private val viewedStoryTemplateDao: IDao<ViewedStoryTemplate>
) : StoryTemplatesRepository {
    companion object {
        const val STORY_TEMPLATES_VERSION = 1
    }

    override fun getStoryTemplates(lang: String): Single<List<StoryTemplate>> =
        storyTemplatesRemoteRemoteDataSource.getStoryTemplates(lang)
            .map { storyTemplates ->
                storyTemplates.asSequence()
                    .filter { template -> template.version <= STORY_TEMPLATES_VERSION }
                    .sortedBy(StoryTemplate::position)
                    .toList()
            }

    override fun getViewedStoriesIds(): Single<Set<Long>> =
        Single.fromCallable {
            viewedStoryTemplateDao
                .getAll()
                .asSequence()
                .map { it.storyTemplateId }
                .toSet()
        }

    override fun markStoryAsViewed(storyTemplateId: Long): Completable =
        Completable.fromAction {
            viewedStoryTemplateDao.insertOrReplace(ViewedStoryTemplate(storyTemplateId))
        }
}