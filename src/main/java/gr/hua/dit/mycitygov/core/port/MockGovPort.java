package gr.hua.dit.mycitygov.core.port;

import gr.hua.dit.mycitygov.core.port.impl.dto.CitizenIdentityDto;
import gr.hua.dit.mycitygov.core.port.impl.dto.ProviderStatusDto;

public interface MockGovPort {
    ProviderStatusDto status();
    String issueUserToken(String afm, String amka, String lastName);
    CitizenIdentityDto validateUserToken(String userToken);
}
