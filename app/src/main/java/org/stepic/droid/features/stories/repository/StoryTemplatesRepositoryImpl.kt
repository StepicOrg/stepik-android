package org.stepic.droid.features.stories.repository

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.features.stories.model.ViewedStoryTemplate
import org.stepic.droid.storage.dao.IDao
import org.stepik.android.data.stories.source.StoryTemplatesRemoteDataSource
import org.stepik.android.model.StoryTemplate
import ru.nobird.android.domain.rx.first
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

    private val storyTemplatesMapper = Function { storyTemplates: List<StoryTemplate> ->
        storyTemplates
            .filter { template -> template.version <= STORY_TEMPLATES_VERSION }
    }

    override fun getStoryTemplate(id: Long): Single<StoryTemplate> =
        storyTemplatesRemoteRemoteDataSource
            .getStoryTemplates(listOf(id))
            .first()

    override fun getStoryTemplates(ids: List<Long>): Single<List<StoryTemplate>> {
        if (ids.isEmpty()) return Single.just(emptyList())

        return storyTemplatesRemoteRemoteDataSource
            .getStoryTemplates(ids)
            .map(storyTemplatesMapper)
            .onErrorReturnItem(emptyList())
    }

    override fun getStoryTemplates(lang: String): Single<List<StoryTemplate>> =
        storyTemplatesRemoteRemoteDataSource
            .getStoryTemplates(lang)
            .map(storyTemplatesMapper)

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