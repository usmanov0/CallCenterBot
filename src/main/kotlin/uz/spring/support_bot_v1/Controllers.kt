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
class ExceptionHandlers {

    @ExceptionHandler(DemoException::class)
    fun handleException(exception: DemoException): ResponseEntity<*> {
        return when(exception) {
            is UserNotFoundException -> ResponseEntity.badRequest()
                .body(exception.getErrorMessage())
            is UserAlreadyExistsException -> ResponseEntity.badRequest()
                .body(exception.getErrorMessage())
            is TimeTableNotFoundException -> ResponseEntity.badRequest()
                .body(exception.getErrorMessage())
            is OperatorNotFoundException -> ResponseEntity.badRequest()
                .body(exception.getErrorMessage())
            is MessageNotFoundException -> ResponseEntity.badRequest()
                .body(exception.getErrorMessage())
            is LanguageNotFoundException -> ResponseEntity.badRequest()
                .body(exception.getErrorMessage())

            is LanguageExistsException -> ResponseEntity.badRequest()
                .body(exception.getErrorMessage())
        }
    }
}


@RestController
@RequestMapping("api/v1/operators")
class OperatorController(
    private val userService: UserService
) {
    @PostMapping
    fun create(@RequestParam chatId: Long) = userService.addOperator(chatId)

    @GetMapping
    fun getAllOperators() = userService.getOperators()

    @GetMapping("{chatId}")
    fun getOneOperator(@PathVariable chatId: Long) = userService.getOperatorsByChatId(chatId)

    @DeleteMapping("{chatId}")
    fun delete(@PathVariable chatId: Long) = userService.deleteOperator(chatId)
}



@RestController
@RequestMapping("api/v1/language")
class LanguageController(
    private val languageService: LanguageService
) {

    @PostMapping
    fun create(@RequestParam name: String) =
        languageService.createLanguage(name)

    @GetMapping
    fun getAll(pageable: Pageable) = languageService.getAll(pageable)

    @GetMapping("{id}")
    fun get(@PathVariable id: Long) = languageService.getOneLanguage(id)

    @PutMapping("{id}")
    fun update(@PathVariable id: Long, @RequestBody languageDto: LanguageDto) = languageService.updateLanguage(id, languageDto)

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long) = languageService.delete(id)

}

@RestController
@RequestMapping("api/v1/operator-language")
class OperatorLanguageController(
    private val operatorLanguageService: OperatorLanguageService
) {
    @PostMapping
    fun createOperatorLanguage(@RequestBody operatorLanguageDto: OperatorLanguageDto) = operatorLanguageService.create(operatorLanguageDto)
}
