package gr.hua.dit.noc.web.rest;

import gr.hua.dit.noc.core.LookupService;
import gr.hua.dit.noc.core.model.LookupResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API Resource class for managing lookups.
 *
 * @author Dimitris Gkoulis
 */
@RestController
@RequestMapping("/api/v1/lookups")
public class LookupResource {

    private final LookupService lookupService;

    public LookupResource(final LookupService lookupService) {
        if (lookupService == null) throw new NullPointerException();
        this.lookupService = lookupService;
    }

    @GetMapping("/{huaId}")
    public ResponseEntity<LookupResult> lookups(@PathVariable String huaId) {
        final LookupResult lookupResult = this.lookupService.lookupByHuaId(huaId);
        return ResponseEntity.ok(lookupResult);
    }
}
