package com.jonathaxs.gymnutshell.backup

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Backup no Google Drive guardando um único arquivo na pasta oculta `appDataFolder`.
 *
 * Autoriza o escopo `drive.appdata` pela Authorization API (token OAuth, com escolha de conta +
 * consentimento nativos) e fala com o Drive por REST cru (HttpURLConnection + org.json), sem puxar a
 * biblioteca pesada `google-api-services-drive`. O JSON em si vem do BackupService (`:core`) — este
 * cliente só cuida do transporte.
 */
class DriveBackupClient(private val context: Context) {

    private companion object {
        const val FILE_NAME = "gymnutshell-backup.json"
        const val SCOPE = "https://www.googleapis.com/auth/drive.appdata"
        const val FILES = "https://www.googleapis.com/drive/v3/files"
        const val UPLOAD = "https://www.googleapis.com/upload/drive/v3/files"
        const val TIMEOUT_MS = 30_000
    }

    // MARK: - Autorização

    /** Pede o token do escopo. Pode devolver o token direto ou uma resolução (PendingIntent) a lançar na UI. */
    suspend fun authorize(): AuthorizationResult {
        val request = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(SCOPE)))
            .build()
        return Identity.getAuthorizationClient(context).authorize(request).await()
    }

    /** Extrai o resultado (com o token) do Intent devolvido pela resolução da autorização. */
    fun resultFromIntent(intent: Intent): AuthorizationResult =
        Identity.getAuthorizationClient(context).getAuthorizationResultFromIntent(intent)

    // MARK: - Transporte

    /** Sobe o JSON: cria o arquivo se ainda não existe, ou substitui o conteúdo do que existe. */
    suspend fun upload(token: String, json: String) = withContext(Dispatchers.IO) {
        val id = findFileId(token)
        if (id != null) updateContent(token, id, json) else createFile(token, json)
    }

    /** Baixa o JSON do backup, ou `null` se ainda não há backup no Drive. */
    suspend fun download(token: String): String? = withContext(Dispatchers.IO) {
        val id = findFileId(token) ?: return@withContext null
        open("$FILES/$id?alt=media", token).readSuccessBody()
    }

    // MARK: - REST

    /** Busca o id do arquivo único no appDataFolder pelo nome (ou null). */
    private fun findFileId(token: String): String? {
        val q = URLEncoder.encode("name = '$FILE_NAME'", "UTF-8")
        val body = open("$FILES?spaces=appDataFolder&q=$q&fields=files(id)", token).readSuccessBody()
            ?: return null
        val files = JSONObject(body).optJSONArray("files") ?: return null
        return if (files.length() > 0) files.getJSONObject(0).getString("id") else null
    }

    /** Cria o arquivo no appDataFolder via upload multipart (metadata + conteúdo numa requisição só). */
    private fun createFile(token: String, json: String) {
        val boundary = "gymnutshell-${System.currentTimeMillis()}"
        val metadata = JSONObject()
            .put("name", FILE_NAME)
            .put("parents", JSONArray().put("appDataFolder"))
        val body = buildString {
            append("--$boundary\r\n")
            append("Content-Type: application/json; charset=UTF-8\r\n\r\n")
            append(metadata).append("\r\n")
            append("--$boundary\r\n")
            append("Content-Type: application/json\r\n\r\n")
            append(json).append("\r\n")
            append("--$boundary--")
        }
        open("$UPLOAD?uploadType=multipart", token, "POST").apply {
            setRequestProperty("Content-Type", "multipart/related; boundary=$boundary")
            writeBody(body)
            checkSuccess()
        }
    }

    /**
     * Substitui o conteúdo do arquivo. O update do Drive é PATCH, que o HttpURLConnection não suporta;
     * usamos POST + header `X-HTTP-Method-Override: PATCH` (o Google aceita).
     */
    private fun updateContent(token: String, id: String, json: String) {
        open("$UPLOAD/$id?uploadType=media", token, "POST").apply {
            setRequestProperty("X-HTTP-Method-Override", "PATCH")
            setRequestProperty("Content-Type", "application/json")
            writeBody(json)
            checkSuccess()
        }
    }

    private fun open(url: String, token: String, method: String = "GET"): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            setRequestProperty("Authorization", "Bearer $token")
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
        }

    private fun HttpURLConnection.writeBody(body: String) {
        doOutput = true
        outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
    }

    /** Corpo da resposta em caso de sucesso (2xx), ou null caso contrário. */
    private fun HttpURLConnection.readSuccessBody(): String? =
        if (responseCode in 200..299) inputStream.bufferedReader().use { it.readText() }
        else { errorStream?.close(); null }

    /** Falha com a mensagem de erro do Drive quando a resposta não é 2xx. */
    private fun HttpURLConnection.checkSuccess() {
        if (responseCode !in 200..299) {
            val err = errorStream?.bufferedReader()?.use { it.readText() }
            error("Drive HTTP $responseCode: $err")
        }
    }
}
