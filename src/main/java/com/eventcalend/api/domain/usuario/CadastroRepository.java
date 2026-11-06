package com.eventcalend.api.domain.usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CadastroRepository extends JpaRepository<Cadastro, Long> {
    Optional<Cadastro> findByLoguin(String loguin);

    boolean existsByLoguinAndIdNot(String loguin, Long id);

    @Query("""
            SELECT c FROM Cadastro c
            WHERE LOWER(c.loguin) LIKE LOWER(CONCAT('%', :termo, '%'))
               OR LOWER(COALESCE(c.nome, '')) LIKE LOWER(CONCAT('%', :termo, '%'))
            ORDER BY c.nome ASC, c.loguin ASC
            """)
    List<Cadastro> buscarPorNomeOuEmail(@Param("termo") String termo);
}
