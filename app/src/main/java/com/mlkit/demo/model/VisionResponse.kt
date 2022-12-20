package com.mlkit.demo.model

import com.squareup.moshi.Json

data class VisionResponse(
    @Json(name = "events")
    var events: List<Event>?,
    @Json(name = "filtered_detections")
    var filteredDetections: FilteredDetections?,
    @Json(name = "images")
    var images: List<Image>?
){
    data class FilteredDetections(
        @Json(name = "face_detections")
        var faceDetections: List<Any?>?
    )

    data class Event(
        @Json(name = "body_attributes")
        var bodyAttributes: Any?,
        @Json(name = "event_id")
        var eventId: String?,
        @Json(name = "external_id")
        var externalId: String?,
        @Json(name = "matches")
        var matches: List<Any>?,
        @Json(name = "source")
        var source: Any?,
        @Json(name = "tags")
        var tags: List<Any>?,
        @Json(name = "track_id")
        var trackId: Any?,
        @Json(name = "url")
        var url: String?,
        @Json(name = "user_data")
        var userData: String?
    )

    data class Image(
        @Json(name = "error")
        var error: Error?,
        @Json(name = "filename")
        var filename: String?,
        @Json(name = "status")
        var status: Int?
    )
}