package gr.hua.dit.noc.core.impl;

import gr.hua.dit.noc.core.LookupService;
import gr.hua.dit.noc.core.model.LookupResult;
import gr.hua.dit.noc.core.model.PersonType;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-Memory implementation of {@link LookupService}.
 *
 * @author Dimitris Gkoulis
 */
@Service
public class InMemoryLookupServiceImpl implements LookupService {

    private final Map<String, PersonType> inMemoryDatabase;

    public InMemoryLookupServiceImpl() {
        this.inMemoryDatabase = new ConcurrentHashMap<>();
    }

    @PostConstruct
    public void populateInitialData() {
        this.inMemoryDatabase.put("it2023001", PersonType.STUDENT);
        this.inMemoryDatabase.put("it2023002", PersonType.STUDENT);
        this.inMemoryDatabase.put("t0001", PersonType.TEACHER);
        this.inMemoryDatabase.put("t0002", PersonType.TEACHER);
        this.inMemoryDatabase.put("s0001", PersonType.STAFF);
        this.inMemoryDatabase.put("s0002", PersonType.STAFF);
    }

    @Override
    public LookupResult lookupByHuaId(final String huaId) {
        if (huaId == null) throw new NullPointerException("huaId cannot be null");
        if (huaId.isBlank()) throw new IllegalArgumentException("huaId cannot be blank");
        final String normalizedHuaId = huaId.strip().toLowerCase();
        final PersonType type = this.inMemoryDatabase.get(normalizedHuaId);
        if (type == null) {
            return LookupResult.empty(huaId);
        } else {
            return new LookupResult(huaId, true, normalizedHuaId, type);
        }
    }
}
