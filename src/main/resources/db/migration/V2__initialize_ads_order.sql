CREATE TABLE post (
  id                  VARCHAR(250) PRIMARY KEY,
  name                VARCHAR(2500)
);

CREATE TABLE orders (
    id                              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sId                             BIGINT NOT NULL REFERENCES shop(id) ON DELETE CASCADE,
    orderId                         VARCHAR(50),
    orderStatus                     VARCHAR(100),
    checkoutId                      VARCHAR(50),
    orderTime                       TIMESTAMP WITHOUT TIME ZONE,
    completionTime                  TIMESTAMP WITHOUT TIME ZONE,
    clickTime                       TIMESTAMP WITHOUT TIME ZONE,
    shopName                        VARCHAR(255),
    shopId                          VARCHAR(50),
    shopType                        VARCHAR(100),
    itemId                          VARCHAR(50),
    itemName                        VARCHAR(500),
    modelId                         VARCHAR(50),
    productType                     VARCHAR(100),
    promotionId                     VARCHAR(100),
    globalCatL1                     VARCHAR(255),
    globalCatL2                     VARCHAR(255),
    globalCatL3                     VARCHAR(255),
    salePrice                       DECIMAL(18,2),
    quantity                        INT,
    affiliateCommissionType         VARCHAR(100),
    campaignPartner                 VARCHAR(255),
    orderValue                      DECIMAL(18,2),
    refundAmount                    DECIMAL(18,2),
    commissionRateOnProduct         DECIMAL(10,2),
    commissionOnProduct             DECIMAL(18,2),
    sellerCommissionRateOnProduct   DECIMAL(10,2),
    xtraCommissionOnProduct         DECIMAL(18,2),
    totalProductCommission          DECIMAL(18,2),
    commissionFromOrder             DECIMAL(18,2),
    commissionFromSeller            DECIMAL(18,2),
    totalOrderCommission            DECIMAL(18,2),
    mcnName                         VARCHAR(255),
    mcnContractCode                 VARCHAR(100),
    mcnManagementRate               DECIMAL(10,2),
    mcnManagementFee                DECIMAL(18,2),
    agreedAffiliateMarketingCommissionRate DECIMAL(10,2),
    netAffiliateMarketingCommission DECIMAL(18,2),
    linkedProductStatus             VARCHAR(100),
    productNotes                    VARCHAR(500),
    attributeType                   VARCHAR(255),
    buyerStatus                     VARCHAR(255),
    subId1                          VARCHAR(255),
    subId2                          VARCHAR(255),
    subId3                          VARCHAR(255),
    subId4                          VARCHAR(255),
    subId5                          VARCHAR(255),
    channel                         VARCHAR(100)
);

CREATE TABLE ads (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sId                 BIGINT NOT NULL REFERENCES shop(id) ON DELETE CASCADE,
    campaignName        VARCHAR(255),
    adGroupName         VARCHAR(255),
    date                DATE,
    adName              VARCHAR(255),
    campaignId          VARCHAR(50),
    deliveryStatus      VARCHAR(50),
    deliveryLevel       VARCHAR(50),
    reach               INT,
    impressions         INT,
    frequency           DECIMAL(10,4),
    attributionSetting  VARCHAR(255),
    resultType          VARCHAR(255),
    results             INT,
    amountSpent         DECIMAL(18,2)
);


CREATE INDEX idx_orders_sid_clickTime_name ON orders (sId, clickTime, subId1);
CREATE INDEX idx_orders_sid_orderTime_name ON orders (sId, orderTime, subId1);
CREATE INDEX idx_ads_sid_date_name ON ads (sId, date, adName);



CREATE MATERIALIZED VIEW mv_orders_by_click_date AS
SELECT sId AS sId,
   (clickTime::date) AS date,
   subId1 AS name,
   COUNT(DISTINCT orderId) AS orders,
   COALESCE(SUM(totalProductCommission),0) AS commission
FROM orders
GROUP BY sId, date, name
ORDER BY sId, date, name
WITH DATA;

CREATE MATERIALIZED VIEW mv_orders_by_order_date AS
SELECT sId AS sId,
   (orderTime::date) AS date,
   subId1 AS name,
   COUNT(DISTINCT orderId) AS orders,
   COALESCE(SUM(totalProductCommission),0) AS commission
FROM orders
GROUP BY sId, date, name
ORDER BY sId, date, name
WITH DATA;

CREATE MATERIALIZED VIEW mv_ads_date AS
SELECT sId as sId,
    (date::date) AS date,
    lower(campaignName) AS name,
    COALESCE(SUM(results),0) AS results,
    COALESCE(SUM(amountSpent),0) AS spent
FROM ads
GROUP BY sId, date, name
order by sId, date, name
WITH DATA;


CREATE INDEX idx_mv_ads_date_1 ON mv_ads_date (sId, date);
CREATE INDEX idx_mv_orders_by_order_date_1 ON mv_orders_by_order_date (sId, date);
CREATE INDEX idx_mv_orders_by_click_date_1 ON mv_orders_by_click_date (sId, date);


CREATE TABLE orderLink (
  id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  sId             BIGINT NOT NULL REFERENCES shop(id) ON DELETE CASCADE,
  subId           VARCHAR(255),
  UNIQUE (sId, subId)
);


CREATE TABLE campaign (
  id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  sId             BIGINT NOT NULL REFERENCES shop(id) ON DELETE CASCADE,
  name            VARCHAR(500),
  unmapped        BOOLEAN DEFAULT FALSE,
  normalizedName  VARCHAR(255),
  orderLinkId     BIGINT REFERENCES orderLink(id) ON DELETE CASCADE DEFAULT NULL,
  UNIQUE (sId, name)
);

CREATE TABLE recommendation (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sId                 BIGINT NOT NULL REFERENCES shop(id) ON DELETE CASCADE,
    status              INT,
    requestTime         TIMESTAMP WITHOUT TIME ZONE,
    createdAt           TIMESTAMP WITHOUT TIME ZONE,
    finishedTime        TIMESTAMP WITHOUT TIME ZONE,
    content             VARCHAR(2000)
);

CREATE TABLE recommendation_campaign (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    recommendation_id   BIGINT NOT NULL REFERENCES recommendation(id) ON DELETE CASCADE,
    campaignName        VARCHAR(255),
    efficiencyLevel     INT,
    action              VARCHAR(255),
    advise              VARCHAR(500)
);
