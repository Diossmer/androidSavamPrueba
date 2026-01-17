package ve.com.movilnet.data.Response

import ve.com.savam.data.models.Suscriptor

data class SuscriptorResponse(
    val status: String?,
    val message: String?,
    val data: Suscriptor?
)
