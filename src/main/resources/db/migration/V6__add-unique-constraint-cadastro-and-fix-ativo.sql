update usuarios set ativo = 1 where ativo is null;
alter table usuarios modify column ativo tinyint not null default 1;

delete c1 from cadastro c1
inner join cadastro c2
where c1.id > c2.id and c1.loguin = c2.loguin;

alter table cadastro add constraint uk_cadastro_loguin unique (loguin);
