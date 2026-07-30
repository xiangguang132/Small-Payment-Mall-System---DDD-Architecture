package cn.bugstack.domain.materialstock.service;

import java.math.BigDecimal;

public interface IMaterialStockService {

    void inbound(Long materialId, String storageAddress, BigDecimal inboundQty, String reason);

    void lock(Long id, Integer quantity);}
