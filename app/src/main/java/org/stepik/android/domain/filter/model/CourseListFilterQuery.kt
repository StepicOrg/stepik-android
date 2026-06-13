package org.stepik.android.domain.filter.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import org.stepik.android.domain.filter_search.Difficulty
import org.stepik.android.domain.filter_search.Language
import org.stepik.android.domain.filter_search.Type
import ru.nobird.app.core.model.mapOfNotNull
import java.io.Serializable

@Parcelize
data class CourseListFilterQuery(
    @SerializedName("course_list")
    val courseList: Long? = null,
    @SerializedName("language")
    val language: List<Language>? = listOf(Language.Any),
    @SerializedName("with_certificate")
    val withCertificate: Boolean? = false,
    @SerializedName("difficulty")
    val difficulty: List<Difficulty>? = emptyList(),
    @SerializedName("learning_type")
    val type: List<Type>? = emptyList(),
    @SerializedName("price_gte")
    val priceGte: String? = null, // Start price
    @SerializedName("price_lte")
    val priceLte: String? = null, // End price
    @SerializedName("promo_price_gte")
    val promoPriceGte: Boolean? = false
) : Parcelable, Serializable {
    companion object {
        private const val LANGUAGE = "language"
        private const val WITH_CERTIFICATE = "with_certificate"
        private const val COURSE_LISTS = "course_lists[]"
        private const val IS_SPECIALIZATION = "is_specialization"
        private const val PRICE_GTE = "price__gte"
        private const val PRICE_LTE = "price__lte"
        private const val PROMO_PRICE_GTE = "promo_price__gte"
    }

    fun toMap(): Map<String, Any> =
        mapOfNotNull(
            COURSE_LISTS to courseList,
            LANGUAGE to language?.let { mapLanguage(it) },
            WITH_CERTIFICATE to withCertificate.takeIf { it == true },
            IS_SPECIALIZATION to type?.let { mapSpecialization(it) },
            PRICE_GTE to priceGte,
            PRICE_LTE to priceLte,
            PROMO_PRICE_GTE to promoPriceGte?.let { mapPromo(it) }
        )

    fun mapDifficulty(): List<String>? =
        if (difficulty.isNullOrEmpty()) {
            null
        } else {
            difficulty.map(Difficulty::queryName)
        }

    private fun mapLanguage(languages: List<Language>): String? {
        if (languages.contains(Language.Any) || languages.isEmpty() || languages.size == 2) {
            return null
        }
        return languages.first().langCode
    }

    private fun mapSpecialization(type: List<Type>): Boolean? {
        if (type.isEmpty() || type.size == 2) {
            return null
        }
        if (type.contains(Type.Course)) {
            return false
        }
        if (type.contains(Type.Program)) {
            return true
        }

        return null
    }

    private fun mapPromo(withDiscount: Boolean): String? =
        if (withDiscount) {
            "1"
        } else {
            null
        }

    // Manual override of equals and hashCode in order to ignore "courseList" property
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CourseListFilterQuery

        if (language != other.language) return false
        if (withCertificate != other.withCertificate) return false
        if (difficulty != other.difficulty) return false
        if (type != other.type) return false
        if (priceGte != other.priceGte) return false
        if (priceLte != other.priceLte) return false
        return promoPriceGte == other.promoPriceGte
    }

    override fun hashCode(): Int {
        var result = language?.hashCode() ?: 0
        result = 31 * result + (withCertificate?.hashCode() ?: 0)
        result = 31 * result + (difficulty?.hashCode() ?: 0)
        result = 31 * result + (type?.hashCode() ?: 0)
        result = 31 * result + (priceGte?.hashCode() ?: 0)
        result = 31 * result + (priceLte?.hashCode() ?: 0)
        result = 31 * result + (promoPriceGte?.hashCode() ?: 0)
        return result
    }
}