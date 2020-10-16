package br.com.cep.dao;

import br.com.cep.model.Cidade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CidadeRepository extends JpaRepository<Cidade, String> {}

