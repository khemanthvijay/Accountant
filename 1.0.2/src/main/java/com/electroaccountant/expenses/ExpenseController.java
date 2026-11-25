package com.electroaccountant.expenses;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api") public class ExpenseController {private final ExpenseService service;private final ExpenseTypeService types;public ExpenseController(ExpenseService service,ExpenseTypeService types){this.service=service;this.types=types;}@GetMapping("/expenses") List<ExpenseResponse> all(){return service.all();}@PostMapping("/expenses") @ResponseStatus(HttpStatus.CREATED) ExpenseResponse create(@Valid @RequestBody CreateExpenseRequest request){return service.create(request);}@GetMapping("/expense-types") List<ExpenseTypeResponse> types(){return types.all();}@PostMapping("/expense-types") @ResponseStatus(HttpStatus.CREATED) ExpenseTypeResponse createType(@Valid @RequestBody CreateExpenseTypeRequest request){return types.create(request);}}
