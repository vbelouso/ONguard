package com.redhat.ecosystemappeng.service.nvd;

public interface NvdService {
    byte[] findByCve(String cve);
}
