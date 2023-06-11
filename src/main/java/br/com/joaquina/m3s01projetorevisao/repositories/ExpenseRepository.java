package br.com.joaquina.m3s01projetorevisao.repositories;

import br.com.joaquina.m3s01projetorevisao.entities.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Integer> {

    List<Expense> findByStatus(String status);

}