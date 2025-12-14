CREATE TABLE visitor_daily_traffic (
    -- Primary Key (UUIDTable equivalent)
    id UUID PRIMARY KEY,
    -- VisitorDailyTraffic columns
    ip_address VARCHAR(45) NOT NULL,
    access_date DATE NOT NULL,
    access_count INTEGER NOT NULL DEFAULT 1,
    route_path VARCHAR(255) NOT NULL,
    -- Composite Unique Index: Enforces one row per IP, per day, per route
    CONSTRAINT unique_visitor_daily_traffic
        UNIQUE (ip_address, access_date, route_path)
);