package org.phuongnq.analyzer.dto.shop;

import java.util.Collection;
import java.util.List;

public record OrderLinkDto(Long id, String name, Collection<CampaignDto> campaigns) {
}
