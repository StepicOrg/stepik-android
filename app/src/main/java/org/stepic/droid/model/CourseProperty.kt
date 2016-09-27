package org.stepic.droid.model

import org.stepic.droid.util.resolvers.CoursePropertyResolver

data class CourseProperty(val title: String, val text: String, val coursePropertyType: CoursePropertyResolver.Type)