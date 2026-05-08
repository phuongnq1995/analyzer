CREATE TABLE IF NOT EXISTS conversionCurvePercentages (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sId                 BIGINT NOT NULL REFERENCES shop(id) ON DELETE CASCADE,
    name                VARCHAR(255),
    period              VARCHAR(100),
    orderPercentage     float,
    revenuePercentage   float
);


CREATE TABLE IF NOT EXISTS userImport (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sId                 BIGINT NOT NULL REFERENCES shop(id) ON DELETE CASCADE,
    name                VARCHAR(255),
    dataDate            DATE,
    createdTime   		TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);


CREATE TABLE IF NOT EXISTS evaluateEfficiency (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sId                 BIGINT NOT NULL REFERENCES shop(id) ON DELETE CASCADE,
    evaluateDate   		DATE,
    errorStatus         VARCHAR(10000),
    createdTime   		TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_evaluateEfficiency_sid_evaluateDate ON evaluateEfficiency (sId, evaluateDate);


CREATE TABLE IF NOT EXISTS evaluateCampaignEfficiency (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sId                  BIGINT NOT NULL REFERENCES shop(id) ON DELETE CASCADE,
    evaluateEfficiencyId BIGINT NOT NULL REFERENCES evaluateEfficiency(id) ON DELETE CASCADE,
    name                 VARCHAR(255),
    createdTime   		 TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
    efficiencyLevel      VARCHAR(100),
    briefStatusSummary   VARCHAR(2000),
    recommendedActions   VARCHAR(2000)
);

CREATE INDEX idx_evaluateCampaignEfficiency_sid_evaluateEfficiencyId ON evaluateCampaignEfficiency (sId, evaluateEfficiencyId);
