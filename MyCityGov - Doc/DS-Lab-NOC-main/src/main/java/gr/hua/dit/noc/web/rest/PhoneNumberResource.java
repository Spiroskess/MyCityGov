package gr.hua.dit.noc.web.rest;

import gr.hua.dit.noc.core.PhoneNumberService;
import gr.hua.dit.noc.core.model.PhoneNumberValidationResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API Resource class for managing phone numbers.
 *
 * @author Dimitris Gkoulis
 */
@RestController
@RequestMapping("/api/v1/phone-numbers")
public class PhoneNumberResource {

    private final PhoneNumberService phoneNumberService;

    public PhoneNumberResource(final PhoneNumberService phoneNumberService) {
        if (phoneNumberService == null) throw new NullPointerException();
        this.phoneNumberService = phoneNumberService;
    }

    @GetMapping("/{phoneNumber}/validations")
    public ResponseEntity<PhoneNumberValidationResult> phoneNumberValidation(@PathVariable String phoneNumber) {
        final PhoneNumberValidationResult phoneNumberValidationResult
                = this.phoneNumberService.validatePhoneNumber(phoneNumber);
        return ResponseEntity.ok(phoneNumberValidationResult);
    }
}
