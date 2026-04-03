package com.veritas.model;

public record CheckResult(
        String label,
        String status,   // "pass" | "warn" | "fail"
        String detail
) {
    public boolean isFail()  { return "fail".equals(status); }
    public boolean isWarn()  { return "warn".equals(status); }
    public boolean isPass()  { return "pass".equals(status); }
}