package com.aitymkiv.googlemapstestproject.model

import com.google.gson.annotations.SerializedName

data class LinesGeometry(
    @SerializedName("coordinates")
    var coordinates: List<List<Double>> = emptyList()
)
