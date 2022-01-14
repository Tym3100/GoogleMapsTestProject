package com.aitymkiv.googlemapstestproject.model

import com.google.gson.annotations.SerializedName


data class Point(
    @SerializedName("geometry")
    var pointsGeometry: PointsGeometry? = null
)


