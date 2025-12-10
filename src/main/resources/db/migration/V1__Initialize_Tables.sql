CREATE TABLE post (
  id                  VARCHAR(250) PRIMARY KEY,
  name                VARCHAR(2500)
);

CREATE TABLE click (
  id               VARCHAR(50)         PRIMARY KEY,
  click_time       TIMESTAMP WITHOUT TIME ZONE,
  area_zone        VARCHAR(125),
  sub_ids          VARCHAR(250),
  channel          VARCHAR(250)
);

CREATE TABLE affiliate_orders (
    orderId                         VARCHAR(50) PRIMARY KEY,
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
    id                  VARCHAR(255) PRIMARY KEY,
    campaignName        VARCHAR(255),
    adGroupName         VARCHAR(255),
    adName              VARCHAR(255),
    date                DATE,
    campaignId          VARCHAR(50),
    deliveryStatus      VARCHAR(50),
    deliveryLevel       VARCHAR(50),
    reach               INT,
    impressions         INT,
    frequency           DECIMAL(10,4),
    attributionSetting  VARCHAR(255),
    resultType          VARCHAR(255),
    results             INT(18,4),
    amountSpent         DECIMAL(18,2)
);

CREATE VIEW campaignDay AS
SELECT (date::date) AS dt, lower(campaignName),
COALESCE(SUM(results),0) AS results,
COALESCE(SUM(amountSpent),0) AS spent
FROM ads
GROUP BY dt, campaignName
order by dt, campaignName;

CREATE VIEW offerDay AS
SELECT (clickTime::date) AS dt, subId1, COUNT(DISTINCT orderId) AS orders,
COALESCE(SUM(netAffiliateMarketingCommission),0) AS commission,
COALESCE(SUM(orderValue),0) AS revenue
FROM affiliate_orders
GROUP BY subId1, dt
order by dt, subId1;
