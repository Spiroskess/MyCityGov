package gr.hua.dit.mycitygov.config;

import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class MunicipalServiceConverter implements Converter<String, MunicipalService> {

    @Override
    public MunicipalService convert(String source) {
        if (source == null) return null;

        String raw = source.trim();
        if (raw.isEmpty()) {
            throw new IllegalArgumentException("service is blank");
        }

        // Accept both the enum name (e.g. KEP) and the Greek label (e.g. ΚΕΠ)
        for (MunicipalService s : MunicipalService.values()) {
            if (s.name().equalsIgnoreCase(raw)) return s;
            if (s.label().equalsIgnoreCase(raw)) return s;
        }

        // Some clients may send values like "TECHNICAL SERVICE"
        String normalized = raw.toUpperCase().replace(' ', '_');

        try {
            return MunicipalService.valueOf(normalized);
        } catch (Exception ignored) {
            // fall-through
        }

        throw new IllegalArgumentException("Unknown service: " + source);
    }
}
