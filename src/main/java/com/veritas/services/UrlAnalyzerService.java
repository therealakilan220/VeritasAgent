package com.veritas.service;

import com.veritas.model.AnalysisResult;
import com.veritas.model.CheckResult;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class UrlAnalyzerService {

    // Weights: fail = 2 points, warn = 1 point
    private static final int FAIL_WEIGHT = 2;
    private static final int WARN_WEIGHT = 1;
    private static final int MAX_SCORE   = 20; // 10 checks × max 2 pts each

    private static final Pattern SUSPICIOUS_KEYWORDS = Pattern.compile(
            "(?i)(login|verify|bank|secure|update|confirm|account|password|signin|webscr|ebayisapi|phish)",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern IP_ADDRESS = Pattern.compile(
            "^(\\d{1,3}\\.){3}\\d{1,3}$"
    );
    private static final Pattern URL_SHORTENERS = Pattern.compile(
            "(?i)^(bit\\.ly|tinyurl\\.com|t\\.co|goo\\.gl|ow\\.ly|short\\.link|cutt\\.ly|rb\\.gy|is\\.gd|tiny\\.cc)$"
    );
    private static final Pattern SUSPICIOUS_TLDS = Pattern.compile(
            "(?i)\\.(tk|ml|ga|cf|gq|xyz|top|click|loan|work|party|racing|win|download)$"
    );
    private static final Pattern ENCODED_CHARS = Pattern.compile("%[0-9a-fA-F]{2}");

    public AnalysisResult analyze(String url) {
        List<CheckResult> checks = new ArrayList<>();
        int riskScore = 0;

        String host = extractHost(url);

        // 1. HTTPS
        CheckResult httpsCheck = checkHttps(url);
        checks.add(httpsCheck);
        riskScore += weight(httpsCheck);

        // 2. @ symbol
        CheckResult atCheck = checkAtSymbol(url);
        checks.add(atCheck);
        riskScore += weight(atCheck);

        // 3. Suspicious keywords
        CheckResult keywordCheck = checkSuspiciousKeywords(url);
        checks.add(keywordCheck);
        riskScore += weight(keywordCheck);

        // 4. URL length
        CheckResult lengthCheck = checkUrlLength(url);
        checks.add(lengthCheck);
        riskScore += weight(lengthCheck);

        // 5. Raw IP address
        CheckResult ipCheck = checkRawIp(host);
        checks.add(ipCheck);
        riskScore += weight(ipCheck);

        // 6. Subdomain depth
        CheckResult subdomainCheck = checkSubdomains(host);
        checks.add(subdomainCheck);
        riskScore += weight(subdomainCheck);

        // 7. URL shortener
        CheckResult shortenerCheck = checkUrlShortener(host);
        checks.add(shortenerCheck);
        riskScore += weight(shortenerCheck);

        // 8. Suspicious TLD
        CheckResult tldCheck = checkSuspiciousTld(host);
        checks.add(tldCheck);
        riskScore += weight(tldCheck);

        // 9. Excessive URL encoding
        CheckResult encodingCheck = checkUrlEncoding(url);
        checks.add(encodingCheck);
        riskScore += weight(encodingCheck);

        // 10. Hyphen abuse in domain
        CheckResult hyphenCheck = checkHyphenAbuse(host);
        checks.add(hyphenCheck);
        riskScore += weight(hyphenCheck);

        return new AnalysisResult(url, checks, riskScore, MAX_SCORE);
    }

    // ─── Individual checks ────────────────────────────────────────────────────

    private CheckResult checkHttps(String url) {
        if (url.startsWith("https://")) {
            return pass("HTTPS Encryption", "Connection is encrypted via TLS");
        }
        return fail("HTTPS Encryption", "No encryption — data can be intercepted in transit");
    }

    private CheckResult checkAtSymbol(String url) {
        if (url.contains("@")) {
            return fail("No @ Symbol in URL", "@ symbol tricks browsers into ignoring the real host");
        }
        return pass("No @ Symbol in URL", "No @ symbol detected");
    }

    private CheckResult checkSuspiciousKeywords(String url) {
        if (SUSPICIOUS_KEYWORDS.matcher(url).find()) {
            return warn("Suspicious Keywords", "URL contains phishing-associated terms");
        }
        return pass("Suspicious Keywords", "No suspicious keywords found");
    }

    private CheckResult checkUrlLength(String url) {
        int len = url.length();
        if (len > 100) {
            return fail("URL Length", "Very long URL (" + len + " chars) — often used to hide malicious paths");
        }
        if (len > 75) {
            return warn("URL Length", "Long URL (" + len + " chars) — slightly above normal range");
        }
        return pass("URL Length", "URL length is normal (" + len + " chars)");
    }

    private CheckResult checkRawIp(String host) {
        if (IP_ADDRESS.matcher(host).matches()) {
            return fail("Domain Check", "Raw IP address used instead of a domain name");
        }
        return pass("Domain Check", "Proper domain name used");
    }

    private CheckResult checkSubdomains(String host) {
        String[] parts = host.split("\\.");
        if (parts.length > 5) {
            return fail("Subdomain Depth", "Excessive subdomain nesting (" + parts.length + " levels) — phishing tactic");
        }
        if (parts.length > 3) {
            return warn("Subdomain Depth", "Deeper-than-average subdomain structure (" + parts.length + " parts)");
        }
        return pass("Subdomain Depth", "Subdomain structure looks normal");
    }

    private CheckResult checkUrlShortener(String host) {
        if (URL_SHORTENERS.matcher(host).matches()) {
            return warn("URL Shortener", "Shortened URLs conceal the true destination");
        }
        return pass("URL Shortener", "Not a known URL shortener service");
    }

    private CheckResult checkSuspiciousTld(String host) {
        if (SUSPICIOUS_TLDS.matcher(host).find()) {
            return warn("Domain TLD", "Top-level domain is commonly associated with spam and phishing");
        }
        return pass("Domain TLD", "Top-level domain appears legitimate");
    }

    private CheckResult checkUrlEncoding(String url) {
        long count = ENCODED_CHARS.matcher(url).results().count();
        if (count > 8) {
            return fail("URL Encoding", "Heavy URL encoding (" + count + " encoded chars) likely disguises malicious content");
        }
        if (count > 4) {
            return warn("URL Encoding", "Moderate URL encoding detected (" + count + " encoded chars)");
        }
        return pass("URL Encoding", "URL encoding is within normal range");
    }

    private CheckResult checkHyphenAbuse(String host) {
        // Extract the primary domain segment (second-to-last label)
        String[] parts = host.split("\\.");
        String domain = parts.length >= 2 ? parts[parts.length - 2] : host;
        long hyphens = domain.chars().filter(c -> c == '-').count();
        if (hyphens >= 3) {
            return warn("Domain Hyphens", "Domain contains many hyphens — common in fake brand spoofs");
        }
        return pass("Domain Hyphens", "Domain hyphen usage looks normal");
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private String extractHost(String url) {
        try {
            return new URI(url).getHost().toLowerCase();
        } catch (URISyntaxException | NullPointerException e) {
            // Fallback: strip protocol and path manually
            String h = url.replaceFirst("(?i)https?://", "").split("[/?#]")[0];
            return h.toLowerCase();
        }
    }

    private int weight(CheckResult c) {
        return switch (c.status()) {
            case "fail" -> FAIL_WEIGHT;
            case "warn" -> WARN_WEIGHT;
            default     -> 0;
        };
    }

    private static CheckResult pass(String label, String detail) {
        return new CheckResult(label, "pass", detail);
    }

    private static CheckResult warn(String label, String detail) {
        return new CheckResult(label, "warn", detail);
    }

    private static CheckResult fail(String label, String detail) {
        return new CheckResult(label, "fail", detail);
    }
}