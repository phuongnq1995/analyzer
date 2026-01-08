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
    createdTime   		  TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);


CREATE TABLE IF NOT EXISTS evaluateCampaignEfficiency (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sId                 BIGINT NOT NULL REFERENCES shop(id) ON DELETE CASCADE,
    name                VARCHAR(255),
    efficiencyLevel     VARCHAR(100),
    briefStatusSummary  VARCHAR(500),
    recommendedActions  VARCHAR(1000),
    evaluateDate   		  DATE,
    createdTime   		  TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);
