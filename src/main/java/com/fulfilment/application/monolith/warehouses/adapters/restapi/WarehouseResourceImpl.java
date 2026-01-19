package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject private WarehouseRepository warehouseRepository;
  @Inject private com.fulfilment.application.monolith.location.LocationGateway locationGateway;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseRepository.getAll().stream().map(this::toWarehouseResponse).toList();
  }

  @Override
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    if (warehouseRepository.findByBusinessUnitCode(data.getBusinessUnitCode()) != null) {
        throw new jakarta.ws.rs.WebApplicationException("Warehouse with this Business Unit Code already exists.", 409);
    }

    var location = locationGateway.resolveByIdentifier(data.getLocation());
    if (location == null) {
        throw new jakarta.ws.rs.WebApplicationException("Location not found.", 404);
    }

    long currentCount = warehouseRepository.countActiveByLocation(data.getLocation());
    if (currentCount >= location.maxNumberOfWarehouses) {
        throw new jakarta.ws.rs.WebApplicationException("Max number of warehouses reached for this location.", 422);
    }

    long currentCapacity = warehouseRepository.sumCapacityByLocation(data.getLocation());
    if (currentCapacity + data.getCapacity() > location.maxCapacity) {
        throw new jakarta.ws.rs.WebApplicationException("Max capacity reached for this location.", 422);
    }

    if (data.getStock() > data.getCapacity()) {
        throw new jakarta.ws.rs.WebApplicationException("Stock cannot exceed capacity.", 422);
    }

    var domainWarehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    domainWarehouse.businessUnitCode = data.getBusinessUnitCode();
    domainWarehouse.location = data.getLocation();
    domainWarehouse.capacity = data.getCapacity();
    domainWarehouse.stock = data.getStock();
    domainWarehouse.createdAt = java.time.LocalDateTime.now();

    warehouseRepository.create(domainWarehouse);

    return data;
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    var w = warehouseRepository.findByBusinessUnitCode(id);
    if (w == null) {
        throw new jakarta.ws.rs.WebApplicationException("Warehouse not found.", 404);
    }
    return toWarehouseResponse(w);
  }

  @Override
  public void archiveAWarehouseUnitByID(String id) {
    var w = warehouseRepository.findByBusinessUnitCode(id);
    if (w == null) {
        throw new jakarta.ws.rs.WebApplicationException("Warehouse not found.", 404);
    }
    w.archivedAt = java.time.LocalDateTime.now();
    warehouseRepository.remove(w);
  }

  @Override
  public Warehouse replaceTheCurrentActiveWarehouse(
      String businessUnitCode, @NotNull Warehouse data) {
    
    var oldWarehouse = warehouseRepository.findByBusinessUnitCode(businessUnitCode);
    if (oldWarehouse == null) {
        throw new jakarta.ws.rs.WebApplicationException("Warehouse not found.", 404);
    }

    // Archive old
    oldWarehouse.archivedAt = java.time.LocalDateTime.now();
    warehouseRepository.remove(oldWarehouse);

    // Validate new
    // New capacity must be able to accommodate old stock? Or new stock?
    // Requirement: "Ensure the new warehouse's capacity can accommodate the stock from the warehouse being replaced."
    if (data.getCapacity() < oldWarehouse.stock) {
        throw new jakarta.ws.rs.WebApplicationException("New capacity cannot accommodate old stock.", 422);
    }

    // Requirement: "Confirm that the stock of the new warehouse matches the stock of the previous warehouse."
    // Assuming data.getStock() should be ignored or validated against old.
    // If we are strictly "moving" stock, the new stock should equal old stock.
    if (data.getStock() != null && !data.getStock().equals(oldWarehouse.stock)) {
          throw new jakarta.ws.rs.WebApplicationException("New warehouse stock must match the replaced warehouse stock.", 422);
    }
    // ensure data has the stock set
    data.setStock(oldWarehouse.stock);


    // Create new
    // Recalculate location capacity? The old one is archived, so it frees up capacity?
    // If we archive first, then create, the check logic in create() might work if it filters out archived.
    // However, create() checks if BU code exists. We are reusing BU code.
    // So create() will throw 409 if we just call it.
    // We need to bypass create()'s BU code check or handle this specifically.
    // Since create() is just a wrapper, allow me to duplicate logic or create helper.
    // But data passed to replace() might have different location? Assumed same location usually for replacement?
    // "creating a new Warehouse in the same area" -> same location.
    
    var domainWarehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    domainWarehouse.businessUnitCode = businessUnitCode; // Reusing BU code
    domainWarehouse.location = data.getLocation(); // Assuming new location or same? "same area" could be same location ID.
    domainWarehouse.capacity = data.getCapacity();
    domainWarehouse.stock = data.getStock();
    domainWarehouse.createdAt = java.time.LocalDateTime.now();
    
    // Check location validity
    var location = locationGateway.resolveByIdentifier(data.getLocation());
    if (location == null) {
         throw new jakarta.ws.rs.WebApplicationException("Location not found.", 404);
    }

    // Capacity check logic considering the swap
    // Since we archived old one, sumCapacityByLocation should exclude it.
    // So if I call sumCapacityByLocation now, it won't include old one.
    long currentCapacity = warehouseRepository.sumCapacityByLocation(data.getLocation());
    if (currentCapacity + data.getCapacity() > location.maxCapacity) {
        throw new jakarta.ws.rs.WebApplicationException("Max capacity reached for this location.", 422);
    }
    
    warehouseRepository.create(domainWarehouse);

    return data;
  }

  private Warehouse toWarehouseResponse(
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    var response = new Warehouse();
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);

    return response;
  }
}
