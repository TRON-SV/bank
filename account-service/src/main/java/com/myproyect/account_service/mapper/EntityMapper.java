package com.myproyect.account_service.mapper;

public interface EntityMapper<E, D> {
    //E = Tipo de entidad que queremos convertir.
    //D = Tipo de DTO que obtendremos como resultado

    D toDto(E entity);
}
