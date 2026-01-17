package ve.com.savam.data.Model

import com.google.gson.annotations.SerializedName
import ve.com.savam.data.models.TelegramInfo
import ve.com.savam.data.models.WhatsAppInfo

data class Numeros( // Considera renombrar este archivo/clase a "DataResponse.kt" en el futuro para mayor claridad
    @SerializedName("whatsapp")
    val whatsapp: WhatsAppInfo, // <-- Aquí le decimos que espere un objeto de tipo WhatsAppInfo

    @SerializedName("telegram")
    val telegram: TelegramInfo, // <-- Y aquí uno de tipo TelegramInfo

    @SerializedName("fecha_consulta")
    val fecha_consulta: String
)