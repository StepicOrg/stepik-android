package org.stepik.android.view.onboarding.resolver

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.configuration.RemoteConfig
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.onboarding.analytic.OnboardingParseErrorAnalyticEvent
import org.stepik.android.view.onboarding.model.OnboardingGoal
import javax.inject.Inject

class OnboardingRemoteConfigResolver
@Inject
constructor(
    private val analytic: Analytic,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val firebaseRemoteConfig: FirebaseRemoteConfig,
    private val gson: Gson
) {
    fun buildOnboardingGoals(): List<OnboardingGoal> {
        val onboardingGoalsString = firebaseRemoteConfig.getString(RemoteConfig.PERSONALIZED_ONBOARDING_COURSE_LISTS)

        return try {
            gson.fromJson(onboardingGoalsString, TypeToken.getParameterized(ArrayList::class.java, OnboardingGoal::class.java).type)
        } catch (e: Exception) {
            analytic.report(OnboardingParseErrorAnalyticEvent(onboardingGoalsString, sharedPreferenceHelper.wasSplashSeen()))
            gson.fromJson(ONBOARDING_GOALS_FALLBACK, TypeToken.getParameterized(ArrayList::class.java, OnboardingGoal::class.java).type)
        }
    }

    companion object {
        private const val ONBOARDING_GOALS_FALLBACK = "[\n" +
                "  {\n" +
                "    \"title\": \"Узнать что-то новое\",\n" +
                "    \"icon\": \"\uD83D\uDD0D\",\n" +
                "    \"course_lists\": [\n" +
                "      {\n" +
                "        \"id\": 136,\n" +
                "        \"title\": \"Творчество\",\n" +
                "        \"icon\": \"\uD83C\uDFA8\",\n" +
                "        \"is_featured\": false\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": 51,\n" +
                "        \"title\": \"Гуманитарные науки\",\n" +
                "        \"icon\": \"\uD83D\uDCDC\",\n" +
                "        \"is_featured\": false\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": 117,\n" +
                "        \"title\": \"Математика\",\n" +
                "        \"icon\": \"\uD83C\uDFB2\",\n" +
                "        \"is_featured\": false\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": 3,\n" +
                "        \"title\": \"Умный досуг\",\n" +
                "        \"icon\": \"\uD83C\uDFAE\",\n" +
                "        \"is_featured\": false\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": 60,\n" +
                "        \"title\": \"Как работает мир вокруг нас\",\n" +
                "        \"icon\": \"\uD83C\uDF06\",\n" +
                "        \"is_featured\": false\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": 59,\n" +
                "        \"title\": \"Прокачать навыки\",\n" +
                "        \"icon\": \"\uD83D\uDCAA\",\n" +
                "        \"is_featured\": false\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"title\": \"Подготовиться к экзаменам\",\n" +
                "    \"icon\": \"\uD83D\uDCDA\",\n" +
                "    \"course_lists\": [\n" +
                "      {\n" +
                "        \"id\": 86,\n" +
                "        \"title\": \"Подготовка к ЕГЭ\",\n" +
                "        \"icon\": \"\uD83D\uDCD6\",\n" +
                "        \"is_featured\": true\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": 65,\n" +
                "        \"title\": \"Русский язык\",\n" +
                "        \"icon\": \"✍️\",\n" +
                "        \"is_featured\": false\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": 29,\n" +
                "        \"title\": \"Математика\",\n" +
                "        \"icon\": \"\uD83D\uDCD0\",\n" +
                "        \"is_featured\": false\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": 36,\n" +
                "        \"title\": \"Английский язык\",\n" +
                "        \"icon\": \"\uD83C\uDDEC\uD83C\uDDE7\",\n" +
                "        \"is_featured\": false\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": 2,\n" +
                "        \"title\": \"Информатика\",\n" +
                "        \"icon\": \"\uD83D\uDDA5\",\n" +
                "        \"is_featured\": false\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"title\": \"Развивать свою карьеру\",\n" +
                "    \"icon\": \"\uD83D\uDCBC\",\n" +
                "    \"course_lists\": [\n" +
                "      {\n" +
                "        \"id\": 12,\n" +
                "        \"title\": \"Программирование для начинающих\",\n" +
                "        \"icon\": \"\uD83D\uDCBB\",\n" +
                "        \"is_featured\": false\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": 61,\n" +
                "        \"title\": \"Программирование для опытных\",\n" +
                "        \"icon\": \"\uD83E\uDD16\",\n" +
                "        \"is_featured\": false\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": 4,\n" +
                "        \"title\": \"Аналитика данных\",\n" +
                "        \"icon\": \"\uD83D\uDCCA\",\n" +
                "        \"is_featured\": false\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": 47,\n" +
                "        \"title\": \"Языки и общение\",\n" +
                "        \"icon\": \"\uD83C\uDF0F\",\n" +
                "        \"is_featured\": false\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": 33,\n" +
                "        \"title\": \"Маркетинг\",\n" +
                "        \"icon\": \"\uD83C\uDFAF\",\n" +
                "        \"is_featured\": false\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"title\": \"Создавать свои курсы\",\n" +
                "    \"icon\": \"\uD83C\uDF93\",\n" +
                "    \"course_lists\": [\n" +
                "      {\n" +
                "        \"id\": 135,\n" +
                "        \"title\": \"Создавать свои курсы\",\n" +
                "        \"icon\": \"\uD83C\uDF93\",\n" +
                "        \"is_featured\": false\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "]"
    }
}