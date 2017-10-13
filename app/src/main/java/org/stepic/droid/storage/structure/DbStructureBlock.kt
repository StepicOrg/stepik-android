package org.stepic.droid.storage.structure

object DbStructureBlock {

    const val BLOCKS = "blocks"

    object Column {
        const val STEP_ID = "step_id"
        const val NAME = "name"
        const val TEXT = "block_text"

        const val EXTERNAL_THUMBNAIL = "optional_thumbnail"
        const val EXTERNAL_VIDEO_ID = "optional_video_id"
        const val EXTERNAL_VIDEO_DURATION = "optional_video_duration"


        //we won't use some properties for getting the code option from database
        //and we can store the rest of the object as a string
        const val CODE_OPTIONS = "code_options"
    }

}