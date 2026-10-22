package com.myproyect.account_service.mapper;

/**
 * Define el contrato para transformar una entidad en un DTO.
 *
 * @param <E> tipo de la entidad de origen
 * @param <D> tipo del DTO de destino
 */
public interface EntityMapper<E, D> {
    //E = Tipo de entidad que queremos convertir.
    //D = Tipo de DTO que obtendremos como resultado

    D toDto(E entity);
}
