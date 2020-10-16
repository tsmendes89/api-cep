package br.com.cep.dao;

import br.com.cep.model.Cep;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CepRepository extends JpaRepository<Cep, String> {}