alter table cadastro add column nome varchar(100) null;
alter table cadastro add column telefone varchar(20) null;
alter table cadastro add column profissao varchar(100) null default 'Produtor(a) de eventos';
