create table eventos (
    id bigint not null auto_increment,
    titulo varchar(150) not null,
    tipo varchar(60) not null default 'Evento',
    data_evento date not null,
    hora_inicio varchar(10) not null,
    hora_fim varchar(10) not null,
    logradouro varchar(150) not null,
    numero varchar(20),
    complemento varchar(100),
    cep varchar(15),
    status varchar(30) not null default 'Confirmado',
    status_pagamento varchar(30) not null default 'PENDENTE',
    valor decimal(10, 2) not null default 0.00,
    observacoes text,
    ativo tinyint not null default 1,

    primary key (id)
);

create table evento_compartilhamentos (
    id bigint not null auto_increment,
    evento_id bigint not null,
    usuario_id bigint not null,
    tipo_informacao varchar(30) not null default 'COMPLETO',
    data_compartilhamento datetime not null default current_timestamp,

    primary key (id),
    constraint fk_compartilhamento_evento foreign key (evento_id) references eventos (id),
    constraint fk_compartilhamento_usuario foreign key (usuario_id) references usuarios (id)
);

insert into eventos (titulo, tipo, data_evento, hora_inicio, hora_fim, logradouro, numero, complemento, cep, status, status_pagamento, valor, observacoes, ativo) values
('Casamento Helena & Lucas', 'Casamento', '2025-05-08', '16:00', '23:30', 'Villa Verde, Jardins', '120', 'Espaço Principal', '01415-000', 'Confirmado', 'PAGO', 8400.00, 'Cerimônia ao ar livre seguida de recepção.', 1),
('Ensaio editorial Aurora', 'Editorial', '2025-05-12', '09:30', '13:00', 'Estúdio Horizonte', '45', 'Sala 3', '01310-100', 'Pendente', 'ADIANTAMENTO', 2150.00, 'Levar iluminação auxiliar e cronograma da equipe.', 1),
('Aniversário da Elisa', 'Aniversário', '2025-05-19', '15:00', '20:00', 'Casa Tangará', '880', 'Jardim', '04538-132', 'Confirmado', 'PENDENTE', 3800.00, 'Decoração floral temática e DJ confirmado.', 1),
('Confraternização Orbe', 'Corporativo', '2025-05-26', '18:30', '22:00', 'Casa Bossa', '500', 'Cobertura', '05424-000', 'Confirmado', 'PAGO', 6200.00, 'Evento corporativo para 120 convidados.', 1);
