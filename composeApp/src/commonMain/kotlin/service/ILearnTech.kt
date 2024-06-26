package service

import Consts
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.date.*
import network.model.*
import org.jsoup.Jsoup
import util.crypto.strEnc
import util.serialization.JSON
import util.serialization.String
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


object ILearnTech {
    var logined = false

    class LoginException(message: String?, cause: Throwable? = null) : Exception(message, cause)

    private val Number.pad2: String
        get() = toString().padStart(2, '0')
    @Suppress("Unused")
    private val Number.pad3: String
        get() = toString().padStart(3, '0')
    @Suppress("Unused")
    private val Number.pad4: String
        get() = toString().padStart(4, '0')

    fun msg(msg: String): String {
        val date = GMTDate()
        return ("${date.hours.pad2}:${date.minutes.pad2}:${date.seconds.pad2} $msg")
    }

    /**
     * @throws LoginException when login failed
     */
    @OptIn(ExperimentalEncodingApi::class)
    suspend fun login(client: HttpClient, username: String, password: String, logging: (String) -> Unit = {}) {
        try {
            logging(msg("CAS Login Get lt"))

            val casResp = client.get(Consts.CAS_URL) {
                parameter("service", "https://jwcidentity.jlu.edu.cn/iplat-pass-jlu/thirdLogin/jlu/login")
            }
            logging(msg("CAS Login Status Code: ${casResp.status}"))
            require(casResp.status.isSuccess())
            val casHtml = Jsoup.parse(casResp.bodyAsText())
            val casEvent = casHtml.selectFirst("#loginForm > input:nth-child(10)")?.attr("value")
            val casExecution = casHtml.selectFirst("#loginForm > input:nth-child(9)")?.attr("value")
            val casNonce = casHtml.selectFirst("#lt")?.attr("value")
            if (casNonce == null || casEvent == null || casExecution == null) {
                logging(msg("Already Logged in"))
                return
            }
            logging(msg("CAS Login lt Got"))

            logging(msg("CAS Login Post Username and Password"))
            val casTicketResp = client.prepareForm("https://cas.jlu.edu.cn/tpass/login", formParameters = parameters {
                append("rsa", strEnc(username + password + casNonce, "1", "2", "3"))
                append("ul", username.length.toString())
                append("pl", password.length.toString())
                append("sl", "0")
                append("lt", casNonce)
                append("execution", casExecution)
                append("_eventId", casEvent)
            }) {
                parameter("service", "https://jwcidentity.jlu.edu.cn/iplat-pass-jlu/thirdLogin/jlu/login")
            }.execute()

            val htmlTicket = Jsoup.parse(casTicketResp.bodyAsText())
            val casUsername = htmlTicket.selectFirst("#username")?.attr("value")
            val casPassword = htmlTicket.selectFirst("#password")?.attr("value")
            require(casUsername != null && casPassword != null) { ;"CAS Username or Password is null" }
            logging(msg("CAS Login Done"))


            logging(msg("ilearntec CAS Login Get lt"))
            val ts0 = System.currentTimeMillis()
            val ilearnGetNonceResp0 = client.get("https://ilearn.jlu.edu.cn/cas-server/login") {
                parameter("service", "https://ilearntec.jlu.edu.cn/")
                parameter("get-lt", "true")
                parameter("callback", "jsonpcallback")
                parameter("n", ts0 + 1)
                parameter("_", ts0)
            }
            require(ilearnGetNonceResp0.status.isSuccess()) { "Get CAS Nonce Failed" }
            val ilearnCasReturn0 = JSON.parseToJsonElement(
                ilearnGetNonceResp0.bodyAsText().substring(14, ilearnGetNonceResp0.bodyAsText().length - 2)
            )
            logging(msg("ilearntec CAS Login lt got"))

            logging(msg("ilearntec CAS Login Post Username and Password"))
            val ts = System.currentTimeMillis()
            val ilearnGetNonceResp = client.get("https://ilearn.jlu.edu.cn/cas-server/login") {
                parameter("service", "https://ilearntec.jlu.edu.cn/")
                parameter("username", casUsername)
                parameter("password", Base64.encode(casPassword.toByteArray()))
                parameter("callback", "logincallback")
                parameter("lt", ilearnCasReturn0.String("lt"))
                parameter("execution", ilearnCasReturn0.String("execution"))
                parameter("n", ts + 1)
                parameter("isajax", "true")
                parameter("isframe", "true")
                parameter("_eventId", "submit")
                parameter("_", ts)
            }
            require(ilearnGetNonceResp.status.isSuccess()) { "Ilraen CAS Login Failed" }
            val ilearnCasReturn = JSON.parseToJsonElement(
                ilearnGetNonceResp.bodyAsText().substring(14, ilearnGetNonceResp.bodyAsText().length - 4)
            )
            logging(msg("ilearntec CAS Login Done, Ticket Got."))

            logging(msg("ilearntec CAS Login By Ticket"))
            client.get("https://ilearn.jlu.edu.cn") {
                parameter("ssoservice", "https://ilearntec.jlu.edu.cn/")
                parameter("ticket", ilearnCasReturn.String("ticket"))
            }
            logging(msg("ilearntec CAS Ticket Login Done"))

            logging(msg("ilearntec CAS Course Center Login"))
            client.get("https://ilearn.jlu.edu.cn/cas-server/login") {
                parameter("service", "https://ilearntec.jlu.edu.cn/coursecenter/main/index")
            }
            logging(msg("ilearntec CAS Course Center Login Done"))

            logging(msg("ilearntec CAS Refresh JSESSIONID"))
            /**
             * need this to refresh JSESSIONID for /resource-center/
             */
            client.get("https://ilearnres.jlu.edu.cn/resource-center/user/index")
            logging(msg("ilearntec CAS Refresh JSESSIONID Done"))
        } catch (e: Exception) {
            logging(msg("Login Failed, message:"+e.message))
            e.printStackTrace()
            throw LoginException("Login Failed", e)
        }

    }

    suspend fun terms(client: HttpClient): List<TermInfo> {
        val response = client.post(Consts.QUERY_ALL_TERM).body<JsonResponse<ListResponse<TermInfo>>>()
        require(response.status == 1) { "Query Term Failed"+response.message }
        requireNotNull(response.data) { "Query Term Failed, 'data' Is Null" }
        return response.data.dataList
    }

    suspend fun lessons(client: HttpClient, termYear: String, term: String): List<LessonInfo> {
        val response = client.get(Consts.QUERY_TERM_LESSONS) {
            parameter("termYear", termYear)
            parameter("term", term)
        }.body<JsonResponse<ListResponse<LessonInfo>>>()
        require(response.status == 1) { "Query Lessons Failed" }
        requireNotNull(response.data) { "Query Lessons Failed, 'data' Is Null" }
        return response.data.dataList
    }

    suspend fun liveAndRecordingsByTerm(client: HttpClient, termYear: String, term: String): List<CourseInfo> {
        val response = client.get(Consts.QUERY_LIVE_AND_RECORD) {
            parameter("roomType", 0)
            parameter("identity", 2)
            parameter("termYear", termYear)
            parameter("term", term)
        }.body<JsonResponse<ListResponse<CourseInfo>>>()
        require(response.status == 1) { "Query Videos Failed" }
        requireNotNull(response.data) { "Query Videos Failed, 'data' Is Null" }
        return response.data.dataList
    }

    suspend fun liveAndRecordingsByLesson(client: HttpClient, termId: String, classroomId: String): List<CourseInfo> {
        val response = client.get(Consts.QUERY_LIVE_AND_RECORD) {
            parameter("roomType", 0)
            parameter("identity", 2)
            parameter("submitStatus", 0)
            parameter("termId", termId)
            parameter("teachClassId", classroomId)
        }.body<JsonResponse<ListResponse<CourseInfo>>>()

        require(response.status == 1) { "Query Videos Failed" }
        requireNotNull(response.data) { "Query Videos Failed, 'data' Is Null" }
        return response.data.dataList
    }


    suspend fun queryDownloadInfo(client: HttpClient, resourceId: String): CourseVideoInfo {
        val response = client.get(Consts.QUERY_VIDEO_INFO) {
            parameter("resourceId", resourceId)
        }.body<JsonResponse<CourseVideoInfo>>()
        require(response.status == 1) { "Query Download Info Failed"+response.toString() }
        requireNotNull(response.data) { "Query Download Info Failed, 'data' Is Null" }
        return response.data
    }
}