package com.example.techstore.service.impl;

import org.slf4j.Logger;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

final class OperationAudit {
    private OperationAudit() {}

    static void info(Logger log, String message, Object... values) {
        Object[] safeValues = values.clone();
        for (int i = 0; i < safeValues.length; i++) {
            if (safeValues[i] instanceof String text) {
                safeValues[i] = text.replaceAll("[\\p{Cntrl}\\p{Zl}\\p{Zp}]", "_");
            }
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            log.info(message, safeValues);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                log.info(message, safeValues);
            }
        });
    }
}
