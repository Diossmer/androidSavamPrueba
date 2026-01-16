package ve.com.savam.data.models

import com.google.gson.annotations.SerializedName


data class TelegramInfo(
    @SerializedName("tiene_telegram")
    val tiene_telegram: Boolean,

    @SerializedName("mensaje")
    val mensaje: String
)
