ALTER TABLE analyses
    ADD COLUMN risk_explanation TEXT,
    ADD COLUMN complexity_risk DOUBLE PRECISION,
    ADD COLUMN coupling_risk DOUBLE PRECISION,
    ADD COLUMN cohesion_risk DOUBLE PRECISION,
    ADD COLUMN size_risk DOUBLE PRECISION,
    ADD COLUMN code_smell_risk DOUBLE PRECISION,
    ADD COLUMN dependency_risk DOUBLE PRECISION,
    ADD COLUMN architecture_risk DOUBLE PRECISION,
    ADD COLUMN churn_risk DOUBLE PRECISION,
    ADD COLUMN hotspot_risk DOUBLE PRECISION;