package com.fulfilment.application.monolith.warehouses.mappers;

import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "cdi")
public interface WareHouseMapper {

    DbWarehouse fromModelToEntity(Warehouse model);

    Warehouse fromEntityToModel(DbWarehouse entity);

    Warehouse fromRequestToModel(com.warehouse.api.beans.Warehouse jsonRequest);

    com.warehouse.api.beans.Warehouse fromModelToResponse(Warehouse jsonResponse);

    void updateModel(Warehouse sourceModel, @MappingTarget Warehouse targetModel);

    void updateEntity(Warehouse sourceModel, @MappingTarget DbWarehouse targetEntity);
}