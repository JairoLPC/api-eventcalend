-- Limpa os eventos fictícios de teste inseridos inicialmente para deixar a agenda limpa para produção
DELETE FROM evento_compartilhamentos;
DELETE FROM eventos WHERE titulo IN (
    'Casamento Helena & Lucas',
    'Ensaio editorial Aurora',
    'Aniversário da Elisa',
    'Confraternização Orbe'
);
