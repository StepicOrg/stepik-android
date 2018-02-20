package org.stepic.droid.web.model.desk

class DeskRequestWrapper(val request: DeskRequest) {
    constructor(name: String, email: String, subject: String, comment: String) : this(DeskRequest(DeskRequester(name, email), subject, DeskComment(comment)))
}