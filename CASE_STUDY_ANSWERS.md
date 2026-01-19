# Case Study Scenarios - Answers

## Scenario 1: Cost Allocation and Tracking
**Challenges**:
- **Shared Resources**: Allocating costs for shared resources (like a central sorting facility or IT infrastructure) to specific warehouses or stores can be arbitrary and inaccurate.
- **Variable vs Fixed Costs**: Distinguishing and tracking variable costs (labor per unit) versus fixed costs (rent) in a dynamic environment.
- **Granularity**: Deciding the level of detail (per sku, per order, per shipment). Too much detail increases overhead; too little hides inefficiencies.

**Considerations**:
- Implement **Activity-Based Costing (ABC)** to assign costs based on actual consumption of resources.
- Use **tagging/metadata** on all financial transactions to link them to specific Business Units (Warehouses/Stores).

## Scenario 2: Cost Optimization Strategies
**Strategies**:
- **Inventory Optimization**: Reduce holding costs by ensuring optimal stock levels (JIT).
- **Automation**: Deploy automated storage and retrieval systems (AS/RS) to reduce labor costs and errors.
- **Energy Efficiency**: Smart lighting and climate control in warehouses.

**Implementation**:
- **Identify**: Audit current operations to find bottlenecks and high-cost areas.
- **Prioritize**: Focus on quick wins (high impact, low effort) first, then long-term structural changes.
- **Implement**: Pilot changes in one warehouse before rolling out globally.

## Scenario 3: Integration with Financial Systems
**Importance**:
- **Accuracy**: Eliminates manual data entry errors.
- **Timeliness**: Enables real-time visibility into financial health, allowing for quicker corrective actions.

**Strategy**:
- **Event-Driven Architecture**: Publish events (e.g., `WarehouseCreated`, `StockUpdated`) that the financial system subscribes to.
- **Idempotency**: Ensure financial transactions are processed exactly once.
- **Reconciliation**: Automated nightly jobs to verify data consistency between Operational and Financial systems.

## Scenario 4: Budgeting and Forecasting
**Importance**:
- predictive capability allows for better resource allocation (hiring temp labor for peak seasons).

**Design**:
- **Historical Data**: The system must store long-term history of costs and operational metrics.
- **Seasonality**: Models must account for seasonal spikes (Black Friday, Christmas).
- **Scenario Planning**: Allow the system to simulate "what-if" scenarios (e.g., "what if rent increases by 10%?").

## Scenario 5: Cost Control in Warehouse Replacement
**Aspects**:
- **Historical Continuity**: Although the physical location allows changes, the "Business Unit" abstract concept might need to persist cost history for Year-over-Year analysis.
- **Transition Costs**: Moving stock, decommissioning old equipment, and setting up new ones incur one-time costs that shouldn't skew the operational baseline.

**Strategy**:
- Link the new Warehouse to the old implementation via a traceability link (e.g., `replacedWarehouseId`) to aggregate reports.
- Tag transition costs separately from operational costs.
