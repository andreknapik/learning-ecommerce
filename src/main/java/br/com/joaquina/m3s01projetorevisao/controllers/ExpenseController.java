package br.com.joaquina.m3s01projetorevisao.controllers;

import br.com.joaquina.m3s01projetorevisao.entities.Expense;
import br.com.joaquina.m3s01projetorevisao.services.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.ResponseEntity.*;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    @Autowired
    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<String> createExpense(@RequestBody Expense expense) {
        // Validações e lógica de negócio
        if (expense.getDataVencimento().before(new Date())) {
            return badRequest().body("A data de vencimento não pode ser anterior à data atual.");
        }

        // Define os valores padrão
        expense.setStatus("Pendente");
        expense.setDataPagamento(null);

        // Salva a despesa no banco de dados
        try {
            Expense createdExpense = expenseService.createExpense(expense);
            return ok("Despesa lançada com sucesso. ID: " + createdExpense.getId());
        } catch (Exception e) {
            return status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao lançar a despesa.");
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<String> updateExpense(@PathVariable int id, @RequestBody Expense expense) {
        // Verificar se a despesa existe
        Expense existingExpense = expenseService.getExpenseById(id);
        if (existingExpense == null) {
            return ResponseEntity.notFound().build();
        }

        // Verificar se a despesa já está paga
        if (existingExpense.getStatus().equals("Pago")) {
            return ResponseEntity.badRequest().body("Não é possível alterar uma despesa já paga.");
        }

        // Atribuir os valores permitidos para atualização
        existingExpense.setCredor(expense.getCredor());
        existingExpense.setDataVencimento(expense.getDataVencimento());
        existingExpense.setValor(expense.getValor());
        existingExpense.setDescricao(expense.getDescricao());

        // Salvar a despesa atualizada no banco de dados
        try {
            Expense updatedExpense = expenseService.updateExpense(existingExpense);
            return ResponseEntity.ok("Despesa atualizada com sucesso. ID: " + updatedExpense.getId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao atualizar a despesa.");
        }
    }

    @GetMapping
    public ResponseEntity<List<Expense>> getAllExpenses() {
        List<Expense> expenses = expenseService.getAllExpenses();
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Expense>> getExpensesByStatus(@PathVariable String status) {
        List<Expense> expenses = expenseService.getExpensesByStatus(status);
        return ResponseEntity.ok(expenses);
    }

    @PutMapping("/pay/{id}")
    public ResponseEntity<String> payExpense(@PathVariable int id) {
        Optional<Expense> expenseOptional = Optional.ofNullable(expenseService.getExpenseById(id));
        if (expenseOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Expense expense = expenseOptional.get();
        if ("Pago".equals(expense.getStatus())) {
            return ResponseEntity.badRequest().body("A despesa já está paga.");
        }

        expense.setDataPagamento(new Date());
        expense.setStatus("Pago");

        try {
            expenseService.updateExpense(expense);
            return ResponseEntity.ok("Despesa paga com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao pagar a despesa.");
        }
    }

    @PutMapping("/refund/{id}")
    public ResponseEntity<String> refundExpense(@PathVariable int id) {
        Optional<Expense> expenseOptional = Optional.ofNullable(expenseService.getExpenseById(id));
        if (expenseOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Expense expense = expenseOptional.get();
        if ("Pendente".equals(expense.getStatus())) {
            return ResponseEntity.badRequest().body("A despesa já está pendente.");
        }

        expense.setDataPagamento(null);
        expense.setStatus("Pendente");

        try {
            expenseService.updateExpense(expense);
            return ResponseEntity.ok("Despesa estornada com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao estornar a despesa.");
        }
    }


}
