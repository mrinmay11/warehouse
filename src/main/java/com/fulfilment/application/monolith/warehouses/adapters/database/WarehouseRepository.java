package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return this.listAll().stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    DbWarehouse dbWarehouse = new DbWarehouse();
    dbWarehouse.businessUnitCode = warehouse.businessUnitCode;
    dbWarehouse.location = warehouse.location;
    dbWarehouse.capacity = warehouse.capacity;
    dbWarehouse.stock = warehouse.stock;
    dbWarehouse.createdAt = warehouse.createdAt;
    dbWarehouse.archivedAt = warehouse.archivedAt;
    this.persist(dbWarehouse);
  }

  @Override
  public void update(Warehouse warehouse) {
      DbWarehouse entity = find("businessUnitCode = ?1 and archivedAt is null", warehouse.businessUnitCode).firstResult();
    if (entity != null) {
        entity.location = warehouse.location;
        entity.capacity = warehouse.capacity;
        entity.stock = warehouse.stock;
        entity.archivedAt = warehouse.archivedAt; // key for archiving
    }
  }

  @Override
  public void remove(Warehouse warehouse) {
      update(warehouse); 
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse entity = find("businessUnitCode = ?1 and archivedAt is null", buCode).firstResult();
    return entity != null ? entity.toWarehouse() : null;
  }

  public long countActiveByLocation(String location) {
      return count("location = ?1 and archivedAt is null", location);
  }

  public long sumCapacityByLocation(String location) {
      return find("location = ?1 and archivedAt is null", location)
             .stream()
             .mapToLong(w -> ((DbWarehouse) w).capacity)
             .sum();
  }
}
