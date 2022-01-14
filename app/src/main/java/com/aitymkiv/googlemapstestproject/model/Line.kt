package com.aitymkiv.googlemapstestproject.model

import com.google.gson.annotations.SerializedName

data class Line(
    @SerializedName("geometry")
    var pointsGeometry: LinesGeometry? = null
)
