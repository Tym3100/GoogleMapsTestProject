package com.aitymkiv.googlemapstestproject.model

import com.google.gson.annotations.SerializedName

data class PointsGeometry(
    @SerializedName("coordinates")
    var coordinates: ArrayList<Double>
)