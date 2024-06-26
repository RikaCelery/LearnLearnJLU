package ui.vm

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import network.Http
import network.model.CourseInfo
import network.model.TermInfo
import service.ILearnTech
import service.Persist

data class AppUI(
    val isLoginInProgress: Boolean = false,
    val isLoading: Boolean = false,
    val currentTerm: TermInfo? = null,
    val courseNameFilter: String = "",
    val username: String = Persist.dbload("username") ?: "",
    val password: String = Persist.dbload("password") ?: "",
    val page: Int = 0,
    val showStreamsSelectPanel: Boolean = false

)


class AppVM : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob() + CoroutineExceptionHandler { coroutineContext, throwable ->
        throwable.printStackTrace()
    })
    private var bar: Job? = null

    private val _uiState = MutableStateFlow(AppUI())
    val uiState = _uiState.asStateFlow()

    private val _terms = MutableStateFlow(listOf<TermInfo>())
    val terms = _terms.asStateFlow()

    private val _videos = mutableStateMapOf<TermInfo, List<CourseInfo>>()
    val courses
        get() = _uiState.value.currentTerm?.let { term ->
            _videos[term]?.map { it.courseName }?.toSet()?.toList() ?: listOf()
        } ?: listOf()


    val videos
        get() = _uiState.value.currentTerm?.let { term ->
            _videos[term]?.let { videos ->
                videos.filter { it.courseName.contains(_uiState.value.courseNameFilter, true)||_uiState.value.courseNameFilter.isEmpty() }.map {
                    CourseVideoCardVM(
                        it, term.year, term.num
                    )
                }
            } ?: listOf()
        } ?: listOf()

    val loginLog = mutableStateListOf<String>()
    val snackbarHostState = SnackbarHostState()

    fun snack(message: String, actionLabel: String? = null, withDismissAction: Boolean = false) {
        bar?.cancel()
        bar = scope.launch {
            snackbarHostState.showSnackbar(message, actionLabel, withDismissAction, SnackbarDuration.Short)
        }
    }


    fun refreshTerms(callback: () -> Unit = {}) {
        if (_uiState.value.isLoading) {
            return
        }
        scope.launch {
            try {
                _uiState.update { current ->
                    current.copy(isLoading = true)
                }
                _terms.update {
                    ILearnTech.terms(Http.client)
                }
                if (_uiState.value.currentTerm == null) {
                    _uiState.update {
                        it.copy(currentTerm = _terms.value.first())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uiState.update { current ->
                    current.copy(isLoading = false)
                }
            }
            callback()
        }
    }

    fun refreshVideos() {
        scope.launch {
            try {
                _uiState.update { current ->
                    current.copy(isLoading = true)
                }

                terms.value.map { currentTerm ->
                    launch {
                        if (currentTerm == _terms.value.first()) {
                            val videos =
                                ILearnTech.liveAndRecordingsByTerm(Http.client, currentTerm.year, currentTerm.num)
                            if (videos.isNotEmpty()) {
                                _videos[currentTerm] = videos
                            }
                        } else {
                            val lessons = ILearnTech.lessons(Http.client, currentTerm.year, currentTerm.num)
                            val videos = lessons.map {
                                async {
                                    ILearnTech.liveAndRecordingsByLesson(
                                        Http.client,
                                        currentTerm.id,
                                        it.classroomId
                                    )
                                }
                            }.awaitAll().flatten()
                            if (videos.isNotEmpty()) {
                                _videos[currentTerm] = videos
                            }
                        }
                    }
                }.joinAll()


            } finally {
                _uiState.update { current ->
                    current.copy(isLoading = false)
                }
            }
        }
    }

    fun casLogin(callback: () -> Unit = {}) {
        if (_uiState.value.isLoginInProgress) {
            return
        }
        scope.launch {
            try {
                snack("Login Started.")
                _uiState.update { current ->
                    current.copy(isLoginInProgress = true)
                }
                if (_uiState.value.username.isEmpty() || _uiState.value.password.isEmpty()) {
                    snack("Username or Password is empty.")
                    return@launch
                }
                ILearnTech.login(Http.client, _uiState.value.username.trim(), _uiState.value.password.trim()) {
                    loginLog.add(0, it)
                    if (loginLog.size > 50) loginLog.removeLast()
                }
                snack("Login Succeed.")
                callback()
            } catch (e: ILearnTech.LoginException) {
                snack("Login Failed.")
                delay(3000)
            } finally {
                _uiState.update { current ->
                    current.copy(isLoginInProgress = false)
                }
            }
        }
    }

    fun setTerm(item: TermInfo) {
        _uiState.update {
            it.copy(currentTerm = item)
        }
    }

    fun setUsername(username: String) {
        _uiState.update {
            it.copy(username = username)
        }
        Persist.dbsave("username", username)

    }

    fun setPassword(password: String) {
        _uiState.update {
            it.copy(password = password)
        }
        Persist.dbsave("password", password)
    }

    fun requireSelectStream() {
        _uiState.update {
            it.copy(showStreamsSelectPanel = true)
        }
    }

    fun dismissSelectStream() {
        _uiState.update {
            it.copy(showStreamsSelectPanel = false)
        }
    }

    fun setCourseNameFilter(filter: String) {
        _uiState.update {
            it.copy(courseNameFilter = filter)
        }
    }

}