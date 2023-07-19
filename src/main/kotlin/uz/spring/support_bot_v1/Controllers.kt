package uz.spring.support_bot_v1

import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
@ControllerAdvice
class ExceptionHandlers(
    private val errorMessageResource: ResourceBundleMessageSource
) {

    @ExceptionHandler(DemoException::class)
    fun handleException(exception: DemoException): ResponseEntity<*> {
        return when(exception) {
            is UserNotFoundException -> ResponseEntity.badRequest()
                .body(exception.getErrorMessage(errorMessageResource, exception.id))
            is UserAlreadyExistsException -> ResponseEntity.badRequest()
                .body(exception.getErrorMessage(errorMessageResource, exception.id))
            is TimeTableNotFoundException -> ResponseEntity.badRequest()
                .body(exception.getErrorMessage(errorMessageResource, exception.id))
            is OperatorNotFoundException -> ResponseEntity.badRequest()
                .body(exception.getErrorMessage(errorMessageResource, exception.id))
            is MessageNotFoundException -> ResponseEntity.badRequest()
                .body(exception.getErrorMessage(errorMessageResource, exception.id))
            is LanguageNotFoundException -> ResponseEntity.badRequest()
                .body(exception.getErrorMessage(errorMessageResource, exception.id))
        }
    }
}


@RestController
@RequestMapping("api/v1/operator")
class OperatorController(
    private val userService: UserService
) {
    @PostMapping
    fun create(@RequestParam chatId: Long) = userService.addOperator(chatId)

    @GetMapping
    fun getAll(pageable: Pageable) = userService.getAll(pageable)

    @GetMapping
    fun getAllOperators() = userService.getOperators()

    @GetMapping("{chatId}")
    fun getOneOperator(@PathVariable chatId: Long) = userService.getOperatorsByChatId(chatId)

    @DeleteMapping("{chatId}")
    fun delete(@PathVariable chatId: Long) = userService.deleteOperator(chatId)
}


@RestController
@RequestMapping("api/v1/time-table")
class TimeTableController(
    private val timeTableService: TimeTableService
) {

    @GetMapping
    fun getAll(pageable: Pageable) = timeTableService.getAll(pageable)

    @GetMapping("{timeTableId}")
    fun getOne(@PathVariable timeTableId: Long) = timeTableService.findById(timeTableId)
}


@RestController
@RequestMapping("api/v1/language")
class LanguageController(
    private val languageService: LanguageService,
    private val operatorLanguageService: OperatorLanguageService
) {

    @PostMapping
    fun create(@RequestBody languageDto: LanguageDto) = languageService.createLanguage(languageDto)

    @GetMapping
    fun getAll(pageable: Pageable) = languageService.getAll(pageable)

    @GetMapping("{id}")
    fun get(@PathVariable id: Long) = languageService.getOneLanguage(id)

    @PutMapping("{id}")
    fun update(@PathVariable id: Long, @RequestBody languageDto: LanguageDto) = languageService.updateLanguage(id, languageDto)

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long) = languageService.delete(id)

    @PostMapping
    fun createOperatorLanguage(@RequestBody operatorLanguageDto: OperatorLanguageDto) = operatorLanguageService.create(operatorLanguageDto)

}
