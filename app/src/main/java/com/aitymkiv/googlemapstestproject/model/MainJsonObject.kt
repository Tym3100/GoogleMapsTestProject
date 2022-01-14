package com.aitymkiv.googlemapstestproject.model

import com.google.gson.annotations.SerializedName

data class MainJsonObject(
    @SerializedName("Points")
    var points: List<Point> = emptyList(),
    @SerializedName("Lines")
    var lines: List<Line> = emptyList()

)
