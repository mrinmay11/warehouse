package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.warehouse.api.beans.Warehouse;
import jakarta.ws.rs.WebApplicationException;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WarehouseResourceTest {

  WarehouseResourceImpl warehouseResource;
  WarehouseRepository warehouseRepository;
  LocationGateway locationGateway;

  @BeforeEach
  void setUp() throws Exception {
    warehouseResource = new WarehouseResourceImpl();
    warehouseRepository = mock(WarehouseRepository.class);
    locationGateway = mock(LocationGateway.class);

    injectField(warehouseResource, "warehouseRepository", warehouseRepository);
    injectField(warehouseResource, "locationGateway", locationGateway);
  }

  private void injectField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  @Test
  void testCreateNewWarehouse_Success() {
    Warehouse data = new Warehouse();
    data.setBusinessUnitCode("BU-001");
    data.setLocation("LOC-1");
    data.setCapacity(100);
    data.setStock(10);

    Location location = new Location("LOC-1", 5, 500);
    when(locationGateway.resolveByIdentifier("LOC-1")).thenReturn(location);
    when(warehouseRepository.findByBusinessUnitCode("BU-001")).thenReturn(null);
    when(warehouseRepository.countActiveByLocation("LOC-1")).thenReturn(0L);
    when(warehouseRepository.sumCapacityByLocation("LOC-1")).thenReturn(0L);

    Warehouse result = warehouseResource.createANewWarehouseUnit(data);
    assertNotNull(result);
    assertEquals("BU-001", result.getBusinessUnitCode());
    verify(warehouseRepository).create(any());
  }

  @Test
  void testCreateNewWarehouse_LocationFull() {
    Warehouse data = new Warehouse();
    data.setBusinessUnitCode("BU-002");
    data.setLocation("LOC-2");
    data.setCapacity(100);
    data.setStock(10);

    Location location = new Location("LOC-2", 1, 500);
    when(locationGateway.resolveByIdentifier("LOC-2")).thenReturn(location);
    when(warehouseRepository.findByBusinessUnitCode("BU-002")).thenReturn(null);
    when(warehouseRepository.countActiveByLocation("LOC-2")).thenReturn(1L); // Max reached

    assertThrows(WebApplicationException.class, () -> warehouseResource.createANewWarehouseUnit(data));
  }
}
