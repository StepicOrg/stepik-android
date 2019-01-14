package org.stepic.droid.features.stories.repository

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.features.stories.model.ViewedStoryTemplate
import org.stepic.droid.storage.dao.IDao
import org.stepic.droid.web.Api
import org.stepic.droid.web.model.story_templates.StoryTemplatesResponse
import org.stepik.android.model.StoryTemplate
import javax.inject.Inject

@AppSingleton
class StoryTemplatesRepositoryImpl
@Inject
constructor(
        private val api: Api,
        private val viewedStoryTemplateDao: IDao<ViewedStoryTemplate>
) : StoryTemplatesRepository {
    companion object {
        const val STORY_TEMPLATES_VERSION = 1
    }

    override fun getStoryTemplates(): Single<List<StoryTemplate>> =
        getStoryTemplatesByPage(1)
            .toList()
            .map { responses ->
                responses.asSequence()
                    .flatMap { it.storyTemplates.asSequence() }
                    .filter { template -> template.version <= STORY_TEMPLATES_VERSION }
                    .sortedBy(StoryTemplate::position)
                    .toList()
            }

    private fun getStoryTemplatesByPage(page: Int): Observable<StoryTemplatesResponse> =
        api.getStoryTemplates(page)
            .concatMap {
                val templatesObservable = Observable.just(it)
                if (it.meta.hasNext) {
                    templatesObservable.concatWith(getStoryTemplatesByPage(it.meta.page + 1))
                } else {
                    templatesObservable
                }
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